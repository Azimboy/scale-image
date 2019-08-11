package controllers.api

import controllers.AssetsFinder
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ImageService
import utils.TestFileUtils._

import scala.concurrent.ExecutionContext

class ImageControllerSpec extends PlaySpec with MockitoSugar {

  implicit val mockAssets = mock[AssetsFinder]
  implicit val ec = ExecutionContext.Implicits.global

  val mockImageService = mock[ImageService]
  val controller = new ImageController(stubControllerComponents(), mockImageService)

  "The ImageController" must {
    "open index page" in {
      when(mockAssets.path(any())).thenReturn("")
      val result = controller.index(FakeRequest())

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include("Square Preview")
    }

    "upload multiple files with status OK" in {
      when(mockImageService.validate(any(), any(), any())).thenReturn(Either.cond(true, List(""), ""))
      val fakeReq = getFakeRequest("/api/file-upload", "test1.png")
      val result = controller.fileUpload(100, 100).apply(fakeReq)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
    }
  }
}