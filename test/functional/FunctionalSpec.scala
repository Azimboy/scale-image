package functional

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

class FunctionalSpec extends PlaySpec with GuiceOneAppPerSuite {

  "Routes" should {

    "send 404 on a bad request" in  {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

    "send 200 on a good request" in  {
      route(app, FakeRequest(GET, "/")).map(status(_)) mustBe Some(OK)
    }

  }

  "ImageController" should {

    "render the index page" in {
      val index = route(app, FakeRequest(GET, "/")).get

      status(index) mustBe Status.OK
      contentType(index) mustBe Some("text/html")
      contentAsString(index) must include("Scale Image")
    }

    "render the index page" in {
      val ss = FakeRequest(GET, "/api/file-upload")
      val index = route(app, ss).get

      status(index) mustBe Status.OK
      contentType(index) mustBe Some("text/html")
      contentAsString(index) must include("Scale Image")
    }

  }

}