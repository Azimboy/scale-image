package utils

import java.nio.file.{Files, Path, Paths}

import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

object FileUtils {

  private val MaxFileSize = 10485760 // 10 MB
  private val MB = 1048576
  private val KB = 1024

  private val BadChars = """\/|\.\.|\?|\*|:|\\""".r // / .. ? * : \
  private val ImageFormats: Set[String] = Set("png", "jpg", "jpeg")

  implicit class FileChecker(filePart: FilePart[TemporaryFile]) {

    def extension = filePart.contentType.flatMap(_.split("/").lastOption).getOrElse("")

    def isImage: Boolean = {
      val nameInLowerCase = filePart.filename.toLowerCase
      ImageFormats.exists(format => nameInLowerCase.endsWith(format) || extension.endsWith(format))
    }

    def isTooLarge: Boolean = filePart.ref.length() > MaxFileSize

    def errorMessage: String = {
      if (!isImage) {
        s"Uploaded file (${filePart.filename}) is not a valid image. Only JPG and PNG files are allowed."
      } else if (isTooLarge) {
        s"Uploaded file (${filePart.filename}) is too large. File size should not be larger than ${MaxFileSize / MB} MB."
      } else {
        ""
      }
    }

  }

  def getBytes(path: Path): Array[Byte] = {
    Files.readAllBytes(path)
  }

  def getBytes(path: String): Array[Byte] = {
    Files.readAllBytes(Paths.get(s"public/$path"))
  }

  def getSize(fileLength: Long): String = {
    if (fileLength > MB) {
      s"${fileLength / MB} MB"
    } else if (fileLength > KB) {
      s"${fileLength / KB} KB"
    } else {
      s"$fileLength B"
    }
  }

  def isCorrectFileName(name: String): Boolean = {
    BadChars.findFirstIn(name).isEmpty
  }

}
