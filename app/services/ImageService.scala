package services

import java.io.File
import java.nio.file.{Files, Path}

import akka.stream.Materializer
import akka.stream.scaladsl.FileIO
import com.sksamuel.scrimage.Image
import com.typesafe.scalalogging.LazyLogging
import javax.inject._
import models.AppProtocol.TempFile
import play.api.libs.Files.TemporaryFile
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData
import utils.FileUtils._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Try}

@Singleton
class ImageService @Inject()(val ws: WSClient)
                            (implicit val mat: Materializer,
                             implicit val ec: ExecutionContext)
  extends LazyLogging {

  def validate(files: Seq[MultipartFormData.FilePart[TemporaryFile]], width: Int, height: Int): Either[String, Seq[String]] = {
    files.find(file => !file.isImage || file.isTooLarge) match {
      case Some(file) =>
        logger.warn(file.errorMessage)
        Left(file.errorMessage)
      case None =>
        val tempFiles = files.map(file => TempFile(Some(file.filename), file.contentType, getBytes(file.ref.path)))
        process(tempFiles, width, height)
    }
  }

  def process(tempFiles: Seq[TempFile], width: Int, height: Int): Either[String, Seq[String]] = {
    accumulateResults(tempFiles.map { tempFile =>
      logger.info(s"Processing file: ${tempFile.fileName}. ContentType: ${tempFile.contentType}. Size: ${getSize(tempFile.content.length)}.")
      Try(save(Image(tempFile.content).scaleTo(width, height))).toEither match {
        case Right(path) =>
          Right(path)
        case Left(error) =>
          logger.error(s"Error occurred during resizing image.", error)
          Left(s"Error occurred during resizing file. Please upload another image file.")
      }
    })
  }

  def save(image: Image): String = {
    val fileName = Random.alphanumeric.take(8).mkString
    val filePath = s"images/temp/$fileName.png"
    image.output(new File(s"public/$filePath"))
    filePath
  }

  def download(url: String): Future[Either[String, Path]] = {
    ws.url(url).withMethod("GET").stream().flatMap { response =>
      if (response.status == 200) {
        val tempFilePath = Files.createTempFile("multipartBody1111", "tempFile")
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

  private def accumulateResults(results: Seq[Either[String, String]]): Either[String, Seq[String]] = {
    results.foldRight(Right(Nil): Either[String, Seq[String]]) { (item, acc) =>
      for {
        paths <- acc.right
        currentPath <- item.right
      } yield currentPath +: paths
    }
  }

}
