package controllers.api

import com.typesafe.scalalogging.LazyLogging
import controllers.AssetsFinder
import javax.inject._
import models.AppProtocol.{Dimension, FilesInfo, TempFile}
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FilenameUtils
import play.api.libs.json.{JsArray, JsString, JsValue, Json}
import play.api.mvc._
import services.ImageService
import utils.FileUtils.getBytes

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImageController @Inject()(val controllerComponents: ControllerComponents,
                                val imageService: ImageService)
                               (implicit val assets: AssetsFinder,
                                implicit val ec: ExecutionContext)
  extends BaseController
    with LazyLogging {

  import imageService._

  implicit def getResult(result: Future[Either[String, List[String]]])(implicit requestHeader: RequestHeader, ec: ExecutionContext): Future[Result] = {
    result.map {
      case Right(fileIds) => Ok(getImageUrlsAsJson(fileIds))
      case Left(error) => BadRequest(error)
    }
  }

  private def getImageUrlsAsJson(fileIds: Seq[String])(implicit requestHeader: RequestHeader): JsValue = {
    Json.obj("images" -> JsArray(fileIds.map { fileId =>
      Json.obj("url" -> JsString(controllers.api.routes.ImageAssetsController.at(fileId).absoluteURL()))
    }))
  }

  def index = Action {
    Ok(views.html.index())
  }

  def fileUpload(width: Int, height: Int) = Action.async { implicit request =>
    request.body.asMultipartFormData match {
      case Some(multipartData) =>
        validate(multipartData.files.toList, Dimension(width, height))
      case None =>
        Future.successful(BadRequest("Please upload a valid image files."))
    }
  }

  def dataUpload(width: Int, height: Int) = Action.async { implicit request =>
    request.body.asJson.flatMap(_.asOpt[FilesInfo]) match {
      case Some(filesInfo) =>
        val tempFiles = filesInfo.files.map(file => TempFile(file.fileName, None, Base64.decodeBase64(file.content)))
        process(tempFiles.toList, Dimension(width, height))
      case None =>
        Future.successful(BadRequest("Please upload a valid json file."))
    }
  }

  def fromUrl(url: String, width: Int, height: Int) = Action.async { implicit request =>
    val fileName = Option(FilenameUtils.getName(url))
    download(url).flatMap {
      case Right(path) =>
        val tempFiles = List(TempFile(fileName, None, getBytes(path)))
        process(tempFiles, Dimension(width, height))
      case Left(error) =>
        Future.successful(BadRequest(error))
    }
  }

}
