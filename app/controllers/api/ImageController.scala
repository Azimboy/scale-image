package controllers.api

import com.typesafe.scalalogging.LazyLogging
import javax.inject._
import org.apache.commons.codec.binary.Base64
import org.webjars.play.WebJarsUtil
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import services.ImageService

object ImageController {
  case class Base64Content(content: String)
  case class FilesInfo(files: Seq[Base64Content])

  implicit val base64ContentFormat = Json.format[Base64Content]
  implicit val filesInfoFormat = Json.format[FilesInfo]
}

@Singleton
class ImageController @Inject()(val controllerComponents: ControllerComponents,
                                val imageService: ImageService)
                               (implicit val webJarsUtil: WebJarsUtil)
  extends BaseController
  with LazyLogging {

  import ImageController._
  import imageService._

  def index = Action {
    Ok(views.html.index())
  }

  def upload(width: Int, height: Int) = Action(parse.multipartFormData) { implicit request =>
    validateTempFiles(request.body.files, width, height) match {
      case Right(paths) => Ok(Json.obj("paths" -> Json.toJson(paths)))
      case Left(error) => BadRequest(error)
    }
  }

  def apiUpload(width: Int, height: Int) = Action(parse.multipartFormData) { implicit request =>
    validateTempFiles(request.body.files, width, height) match {
      case Right(paths) => Ok(getFilesJson(paths))
      case Left(error) => BadRequest(error)
    }
  }

  def jsonUpload(width: Int, height: Int) = Action(parse.json[FilesInfo]) { implicit request =>
    processFiles(request.body.files.map(file => Base64.decodeBase64(file.content)), width, height) match {
      case Right(paths) => Ok(getFilesJson(paths))
      case Left(error) => BadRequest(error)
    }
  }

  private def validateTempFiles(files: Seq[MultipartFormData.FilePart[TemporaryFile]], width: Int, height: Int): Either[String, Seq[String]] = {
    if (files.exists(filePart => imageService.isImage(filePart.filename))) {
      imageService.processFiles(files.map(filePart => getBytes(filePart.ref.path)), width, height)
    } else {
      Left(s"Uploaded file is not a valid image. Only JPG and PNG files are allowed.")
    }
  }

  private def getFilesJson(paths: Seq[String]): JsValue = {
    Json.toJson(FilesInfo(paths.map { path =>
      Base64Content(Base64.encodeBase64String(getBytes(path)))
    }))
  }
}
