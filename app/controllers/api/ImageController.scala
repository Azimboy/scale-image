package controllers.api

import com.typesafe.scalalogging.LazyLogging
import controllers.AssetsFinder
import javax.inject._
import models.AppProtocol.{FilesInfo, TempFile}
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FilenameUtils
import play.api.libs.json.{JsArray, JsString, JsValue, Json}
import play.api.mvc._
import services.ImageService
import utils.FileUtils.getBytes

import scala.concurrent.ExecutionContext

@Singleton
class ImageController @Inject()(val controllerComponents: ControllerComponents,
                                val imageService: ImageService)
                               (implicit val assets: AssetsFinder,
                                implicit val ec: ExecutionContext)
  extends BaseController
    with LazyLogging {

  import imageService._

  def index = Action {
    Ok(views.html.index())
  }

  implicit def getResult(result: Either[String, Seq[String]])(implicit requestHeader: RequestHeader): Result = {
    result match {
      case Right(paths) => Ok(getImageUrlsAsJson(paths))
      case Left(error) => BadRequest(error)
    }
  }

  private def getImageUrlsAsJson(paths: Seq[String])(implicit requestHeader: RequestHeader): JsValue = {
    Json.obj("images" -> JsArray(paths.map { path =>
      Json.obj("url" -> JsString(controllers.routes.Assets.versioned(path).absoluteURL()))
    }))
  }

  def fileUpload(width: Int, height: Int) = Action(parse.multipartFormData) { implicit request =>
    validate(request.body.files, width, height)
  }

  def dataUpload(width: Int, height: Int) = Action(parse.json[FilesInfo]) { implicit request =>
    val tempFiles = request.body.files.map(file => TempFile(file.fileName, None, Base64.decodeBase64(file.content)))
    process(tempFiles, width, height)
  }

  def fromUrl(url: String, width: Int, height: Int) = Action.async { implicit request =>
    val fileName = Option(FilenameUtils.getName(url))
    download(url).map {
      case Right(path) =>
        val tempFiles = Seq(TempFile(fileName, None, getBytes(path)))
        process(tempFiles, width, height)
      case Left(error) =>
        BadRequest(error)
    }
  }

}
