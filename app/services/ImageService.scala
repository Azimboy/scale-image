package services

import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.sksamuel.scrimage.Image
import com.typesafe.scalalogging.LazyLogging
import javax.inject._

import scala.util.{Random, Try}

@Singleton
class ImageService extends LazyLogging {

  def processFiles(fileBytes: Seq[Array[Byte]], width: Int, height: Int): Either[String, Seq[String]] = {
    accumulateResults(fileBytes.map { bytes =>
      Try(save(Image(bytes).scaleTo(width, height))).toEither match {
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

  def getBytes(path: Path): Array[Byte] = {
    Files.readAllBytes(path)
  }

  def getBytes(path: String): Array[Byte] = {
    Files.readAllBytes(Paths.get(s"public/$path"))
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
