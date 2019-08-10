import com.typesafe.scalalogging.LazyLogging
import javax.inject._
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(env: Environment,
                             config: Configuration,
                             sourceMapper: OptionalSourceMapper,
                             router: Provider[Router])
  extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with LazyLogging {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    val result = statusCode match {
      case 500 =>
        logger.error(s"ClientError: statusCode = $statusCode, uri = ${request.uri}, message = $message")
        InternalServerError("Something went wrong. Please try again.")
      case 404 =>
        NotFound("Page not found.")
      case _ =>
        logger.warn(s"onClientError: statusCode = $statusCode, uri = ${request.uri}, message = $message")
        BadRequest("Please try again.")
    }
    Future.successful(result)
  }

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    logger.error("A server error occurred.", exception)
    Future.successful(InternalServerError("Error occurred."))
  }

}