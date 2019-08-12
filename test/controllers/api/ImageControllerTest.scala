package controllers.api

import controllers.AssetsFinder
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ImageService
import utils.StubData._
import utils.TestFileUtils._

import scala.concurrent.{ExecutionContext, Future}

class ImageControllerTest extends PlaySpec with MockitoSugar {

  implicit val ec = ExecutionContext.Implicits.global

  implicit val mockAssets = mock[AssetsFinder]

  val mockImageService = mock[ImageService]
  val controller = new ImageController(stubControllerComponents(), mockImageService)

  def fileUpload(files: String*): Future[Result] = {
    controller.fileUpload(100, 100)(apiFileUpload(files: _*))
  }

  def dataUpload(jsonBody: JsValue): Future[Result] = {
    controller.dataUpload(100, 100)(apiDataUpload(jsonBody))
  }

  def fromUrl(url: String): Future[Result] = {
    controller.fromUrl(url, 100, 100)(apiFromUrl(url))
  }

  "ImageController" should {

    "successfully open index page" in {
      when(mockAssets.path(any())).thenReturn("")
      val result = controller.index(FakeRequest())

      status(result) mustBe OK
      contentType(result) mustBe Some(HTML)
      contentAsString(result) must include("Scale Image")
    }

    "successfully upload single file with status OK" in {
      when(mockImageService.validate(any(), any())).thenReturn(futureRight)
      val result = fileUpload("test1.png")

      status(result) mustBe OK
      contentType(result) mustBe Some(JSON)
    }

    "successfully upload multiple files with status OK" in {
      when(mockImageService.validate(any(), any())).thenReturn(futureRight)
      val result = fileUpload("test1.png", "test2.jpg", "test3.jpg")

      status(result) mustBe OK
      contentType(result) mustBe Some(JSON)
    }

    "fail upload multiple files with status BAD_REQUEST" in {
      when(mockImageService.validate(any(), any())).thenReturn(futureLeft)
      val result = fileUpload("test6.csv")

      status(result) mustBe BAD_REQUEST
      contentType(result) mustBe Some(TEXT)
    }

    "successfully upload base64 content data json with status OK" in {
      when(mockImageService.process(any(), any())).thenReturn(futureRight)
      val result = dataUpload(base64FileJson)

      status(result) mustBe OK
      contentType(result) mustBe Some(JSON)
    }

    "successfully upload base64 contents data json with status OK" in {
      when(mockImageService.process(any(), any())).thenReturn(futureRight)
      val result = dataUpload(base64FilesJson)

      status(result) mustBe OK
      contentType(result) mustBe Some(JSON)
    }

    "fail upload base64 data invalid json with status BAD_REQUEST" in {
      when(mockImageService.process(any(), any())).thenReturn(futureLeft)
      val result = dataUpload(base64FileJson)

      status(result) mustBe BAD_REQUEST
      contentType(result) mustBe Some(TEXT)
    }

    "fail upload base64 data json with status BAD_REQUEST" in {
      when(mockImageService.process(any(), any())).thenReturn(futureRight)
      val result = dataUpload(invalidJson)

      status(result) mustBe BAD_REQUEST
      contentType(result) mustBe Some(TEXT)
      contentAsString(result) mustBe "Please upload a valid json file."
    }

    "successfully load image from external url with status OK" in {
      when(mockImageService.download(any())).thenReturn(Future.successful(Right(getTestJpgFile)))
      val result = fromUrl(imageUrl)

      status(result) mustBe OK
      contentType(result) mustBe Some(JSON)
    }

    "fail load image from external url with status BAD_REQUEST" in {
      when(mockImageService.download(any())).thenReturn(Future.successful(Left("Error occurred during downloading file.")))
      val result = fromUrl(imageUrl)

      status(result) mustBe BAD_REQUEST
      contentType(result) mustBe Some(TEXT)
      contentAsString(result) mustBe "Error occurred during downloading file."
    }

  }

}