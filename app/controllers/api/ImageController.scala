package controllers.api

import com.typesafe.scalalogging.LazyLogging
import javax.inject._
import models.AppProtocol.{Base64Content, FilesInfo, TempFile}
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FilenameUtils
import org.webjars.play.WebJarsUtil
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import services.ImageService
import utils.FileUtils.getBytes

import scala.concurrent.ExecutionContext

@Singleton
class ImageController @Inject()(val controllerComponents: ControllerComponents,
                                val imageService: ImageService)
                               (implicit val webJarsUtil: WebJarsUtil,
                                implicit val ec: ExecutionContext)
  extends BaseController
  with LazyLogging {

  import imageService._

  def index = Action {
    Ok(views.html.index())
  }

  def upload(width: Int, height: Int) = Action(parse.multipartFormData) { implicit request =>
    validate(request.body.files, width, height) match {
      case Right(paths) => Ok(Json.obj("paths" -> Json.toJson(paths)))
      case Left(error) => BadRequest(error)
    }
  }

  def fileUpload(width: Int, height: Int) = Action(parse.multipartFormData) { implicit request =>
    validate(request.body.files, width, height) match {
      case Right(paths) => Ok(getFilesJson(paths))
      case Left(error) => BadRequest(error)
    }
  }

  def dataUpload(width: Int, height: Int) = Action(parse.json[FilesInfo]) { implicit request =>
    val tempFiles = request.body.files.map(file => TempFile(file.fileName, None, Base64.decodeBase64(file.content)))
    process(tempFiles, width, height) match {
      case Right(paths) => Ok(getFilesJson(paths))
      case Left(error) => BadRequest(error)
    }
  }

  def fromUrl(url: String, width: Int, height: Int) = Action.async { implicit request =>
    val fileName = Option(FilenameUtils.getName(url))
    download(url).map {
      case Right(path) =>
        val tempFiles = Seq(TempFile(fileName, None, getBytes(path)))
        process(tempFiles, width, height) match {
          case Right(paths) => Ok(getFilesJson(paths))
          case Left(error) => BadRequest(error)
        }
      case Left(error) =>
        BadRequest(error)
    }
  }

  private def getFilesJson(paths: Seq[String]): JsValue = {
    Json.toJson(FilesInfo(paths.map { path =>
      Base64Content(None, Base64.encodeBase64String(getBytes(path)))
    }))
  }

}
