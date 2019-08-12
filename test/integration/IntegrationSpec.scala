package integration

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Configuration, Mode}
import utils.TestFileUtils._

class IntegrationSpec extends PlaySpec with GuiceOneAppPerSuite {

//  override def fakeApplication(): Application = {
//    new GuiceApplicationBuilder()
////      .in(Mode.Test)
//      .overrides(bind[Configuration].toInstance(configuration))
////      .overrides(bind[WSClient].toInstance(mockWs))
//      .build()
//  }

  "Scale Image" should {

    "successfully open file upload UI" in  {
      route(app, FakeRequest(GET, "/")).map { result =>
        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
        contentAsString(result) must include("Scale Image")
      }
    }

    "show page not found" in  {
      route(app, FakeRequest(GET, "/unknown")).map { result =>
        status(result) mustBe NOT_FOUND
        contentAsString(result) mustBe "Page not found."
      }
    }

    "successfully upload single file" in  {
      route(app, apiFileUpload("test1.png")).map { result =>
        status(result) mustBe OK
        contentType(result) mustBe Some(JSON)
      }
    }

  }

}