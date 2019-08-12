package models

import play.api.libs.json.Json

object AppProtocol {

  case class Base64Content(fileName: Option[String], content: String)
  case class FilesInfo(files: Seq[Base64Content])

  implicit val base64ContentFormat = Json.format[Base64Content]
  implicit val filesInfoFormat = Json.format[FilesInfo]

  case class Dimension(width: Int, height: Int)
  case class TempFile(fileName: Option[String], contentType: Option[String], content: Array[Byte])

}
