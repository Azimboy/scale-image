package services

import java.io.File

import com.sksamuel.scrimage.Image
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

import scala.util.{Random, Try}

object ImageService extends LazyLogging {

  val ImageFormats: Set[String] = Set(".png", ".jpg", ".jpeg")

  def validate(fileName: String): Either[String, Unit] = {
    val nameInLowerCase = fileName.toLowerCase

    Either.cond(
      ImageFormats.exists(format => nameInLowerCase.endsWith(format)), (),
      s"Uploaded file ($fileName) is not a valid image. Only JPG and PNG files are allowed."
    )
  }

  def resize(file: FilePart[TemporaryFile], width: Int, height: Int): Either[String, Image] = {
    Try(Image.fromPath(file.ref.path).scaleTo(width, height)).toEither match {
      case Right(image) =>
        Right(image)
      case Left(error) =>
        logger.error(s"Error occurred during resizing image (${file.filename}.", error)
        Left(s"Error occurred during resizing file (${file.filename}). Please upload another image file.")
    }
  }

  def save(image: Image): String = {
    val fileName = Random.alphanumeric.take(8).mkString
    val filePath = s"images/temp/$fileName.png"
    image.output(new File(s"public/$filePath"))
    filePath
  }

}
