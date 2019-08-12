package services

import java.nio.file.{Files, Path, Paths}

import akka.stream.Materializer
import akka.stream.scaladsl.FileIO
import cats.data.EitherT
import cats.instances.future._
import cats.instances.list._
import cats.syntax.traverse._
import com.sksamuel.scrimage.Image
import com.typesafe.scalalogging.LazyLogging
import javax.inject._
import models.AppProtocol.{Dimension, TempFile}
import play.api.Configuration
import play.api.libs.Files.TemporaryFile
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData.FilePart
import utils.FileUtils._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class ImageService @Inject()(val configuration: Configuration,
                             val ws: WSClient)
                            (implicit val mat: Materializer,
                             implicit val ec: ExecutionContext)
  extends LazyLogging {

  val appConf = configuration.get[Configuration]("application")
  val tempFilesFolder = appConf.get[String]("temp-files-path")

  val TempFilesPath = Paths.get(tempFilesFolder)

  Files.createDirectories(TempFilesPath)

  def download(url: String): Future[Either[String, Path]] = {
    ws.url(url).withMethod("GET").stream().flatMap { response =>
      if (response.status == 200) {
        val tempFilePath = Files.createTempFile("multipartBody", "tempFile")
        response.bodyAsSource.runWith(FileIO.toPath(tempFilePath)).map { ioResult =>
          if (ioResult.wasSuccessful) {
            Right(tempFilePath)
          } else {
            logger.error(s"Error occurred during downloading file.", ioResult.getError)
            Left(s"Error occurred during downloading file.")
          }
        }
      } else {
        logger.error(s"Couldn't download file. ResponseStatus: ${response.status}")
        Future.successful(Left(s"Couldn't download file. ResponseStatus: ${response.status}"))
      }
    }.recover { case error =>
      logger.error(s"Couldn't connect to image url.", error)
      Left(s"Couldn't connect to image url.")
    }
  }

  def validate(files: List[FilePart[TemporaryFile]], dimension: Dimension): Future[Either[String, List[String]]] = {
    files.find(file => !file.isImage || file.isTooLarge) match {
      case Some(file) =>
        logger.warn(file.errorMessage)
        Future.successful(Left(file.errorMessage))
      case None =>
        val tempFiles = files.map(file => TempFile(Some(file.filename), file.contentType, getBytes(file.ref.path)))
        process(tempFiles, dimension)
    }
  }

  def process(tempFiles: List[TempFile], dimension: Dimension): Future[Either[String, List[String]]] = {
    tempFiles.map { tempFile =>
      logger.info(s"Processing file: ${tempFile.fileName}. ContentType: ${tempFile.contentType}. Size: ${getSize(tempFile.content.length)}.")
      scaleAndSave(tempFile.content, dimension).value
    }.sequence.map(accumulateResults)
  }

  private def scaleAndSave(bytes: Array[Byte], dimension: Dimension): EitherT[Future, String, String] = {
    for {
      image <- EitherT(scale(bytes, dimension))
      path <- EitherT(save(image))
    } yield path
  }

  private def scale(bytes: Array[Byte], dimension: Dimension): Future[Either[String, Image]] = {
    Future {
      Right(Image(bytes).scaleTo(dimension.width, dimension.height))
    }.recover { case error =>
      logger.error(s"Error occurred during scaling image.", error)
      Left("Error occurred during scaling image.")
    }
  }

  private def save(image: Image): Future[Either[String, String]] = {
    Future {
      val fileName = s"${Random.alphanumeric.take(10).mkString}.png"
      image.output(TempFilesPath.resolve(fileName))
      Right(fileName)
    }.recover { case error =>
      logger.error(s"Error occurred during saving image.", error)
      Left("Error occurred during saving image.")
    }
  }

  private def accumulateResults(results: List[Either[String, String]]): Either[String, List[String]] = {
    results.foldRight(Right(Nil): Either[String, List[String]]) { (item, acc) =>
      for {
        paths <- acc.right
        currentPath <- item.right
      } yield currentPath +: paths
    }
  }

}
