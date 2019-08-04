package utils

import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

object FileUtils {

  private val MaxFileSize = 15728640 // 15 MB
  private val MB = 1048576
  private val KB = 1024

  private val ImageFormats: Set[String] = Set("png", "jpg", "jpeg")

  implicit class FileChecker(filePart: FilePart[TemporaryFile]) {
    private val fileLength = filePart.ref.length

    def sizeInfo: String = {
      if (fileLength > MB) {
        s"${fileLength / MB} mB"
      } else if (fileLength > KB) {
        s"${fileLength / KB} kB"
      } else {
        s"$fileLength B"
      }
    }

    def extension = filePart.contentType.flatMap(_.split("/").lastOption).getOrElse("")

    def isImage: Boolean = {
      val nameInLowerCase = filePart.filename.toLowerCase
      ImageFormats.exists(format => nameInLowerCase.endsWith(format) || extension.endsWith(format))
    }

    def isTooLarge: Boolean = fileLength > MaxFileSize

    def errorMessage: String = {
      if (!isImage) {
        s"Uploaded file (${filePart.filename}) is not a valid image. Only JPG and PNG files are allowed."
      } else if (isTooLarge) {
        s"Uploaded file (${filePart.filename}) is too large. File size should not be larger than ${MaxFileSize / MB} mB."
      } else {
        ""
      }
    }
  }

}
