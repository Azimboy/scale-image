package controllers.api

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.ImageService
import utils.FileUtils

import scala.concurrent.ExecutionContext

@Singleton
class ImageAssetsController @Inject()(val cc: ControllerComponents,
                                      val imageService: ImageService)
                                     (implicit val ec: ExecutionContext)
  extends AbstractController(cc) {

  def at(fileName: String) = Action {
    require(FileUtils.isCorrectFileName(fileName))

    Ok.sendPath(imageService.TempFilesPath.resolve(fileName))
  }

}
