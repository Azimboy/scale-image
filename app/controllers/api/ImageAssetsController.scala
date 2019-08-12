package controllers.api

import java.nio.file.{Files, Paths}

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.FileUtils

import scala.concurrent.ExecutionContext

@Singleton
class ImageAssetsController @Inject()(val cc: ControllerComponents,
                                      val configuration: Configuration)
                                     (implicit val ec: ExecutionContext)
  extends AbstractController(cc) {

  val appConf = configuration.get[Configuration]("application")
  val tempFilesFolder = appConf.get[String]("temp-files-path")

  def at(fileName: String) = Action {
    require(FileUtils.isCorrectFileName(fileName))

    val filePath = Paths.get(tempFilesFolder).resolve(fileName)
    if (Files.exists(filePath)) {
      Ok.sendPath(filePath)
    } else {
      NotFound("File not found.")
    }
  }

}
