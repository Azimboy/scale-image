package controllers.api

import com.typesafe.scalalogging.LazyLogging
import javax.inject._
import org.webjars.play.WebJarsUtil
import play.api.mvc._
import services.ImageService._

@Singleton
class ImageController @Inject()(val controllerComponents: ControllerComponents)
                               (implicit val webJarsUtil: WebJarsUtil)
  extends BaseController
  with LazyLogging {

  def index = Action {
    Ok(views.html.index())
  }

  def fileUpload(width: Int, height: Int) = Action(parse.multipartFormData) { implicit request =>
    val results = request.body.files.map { filePart =>
      for {
        _     <- validate(filePart.filename)
        image <- resize(filePart, width, height)
        path  =  save(image)
      } yield path
    }
    accumulateResults(results) match {
      case Right(paths) =>
        Ok(views.html.result(paths))
      case Left(error) =>
        BadRequest(error)
    }
  }

  private def accumulateResults(results: Seq[Either[String, String]]): Either[String, Seq[String]] = {
    results.foldRight(Right(Nil): Either[String, Seq[String]]) { (item, acc) =>
      for {
        paths <- acc.right
        currentPath <- item.right
      } yield currentPath +: paths
    }
  }

}
