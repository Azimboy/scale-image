package modules

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
    logger.error(s"onClientError: statusCode = $statusCode, uri = ${request.uri}, message = $message")
    Future.successful(InternalServerError("Something went wrong."))
  }

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    logger.error("A server error occurred.", exception)
    Future.successful(InternalServerError("Error occurred."))
  }

}