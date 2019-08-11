package controllers.api

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import controllers.AssetsFinder
import models.AppProtocol.TempFile
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ImageService
import utils.TestFileUtils._

import scala.concurrent.ExecutionContext

class ImageControllerTest extends PlaySpec with MockitoSugar {

  implicit val ec = ExecutionContext.Implicits.global
  implicit val actorSystem = ActorSystem("ImageControllerTest")
  implicit val actorMaterializer = ActorMaterializer()

  implicit val mockAssets = mock[AssetsFinder]

  val mockImageService = mock[ImageService]
  val controller = new ImageController(stubControllerComponents(), mockImageService)

  def fileUpload(files: String*) = {
    val fakeReq = getFakeRequest("/api/file-upload", files: _*)
    controller.fileUpload(100, 100)(fakeReq)
  }

  def dataUpload(jsonBody: JsValue) = {
    val fakeReq = FakeRequest(POST, "/api/data-upload")
      .withHeaders("Content-Type" -> "application/json")
      .withJsonBody(jsonBody)
    controller.dataUpload(100, 100)(fakeReq)
  }

  "ImageController" should {

    "successfully open index page" in {
      when(mockAssets.path(any())).thenReturn("")
      val result = controller.index(FakeRequest())

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include("Scale Image")
    }

    "successfully upload single file with status OK" in {
      when(mockImageService.validate(any(), any(), any())).thenReturn(Right(Seq("")))
      val result = fileUpload("test1.png")

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
    }

    "successfully upload multiple files with status OK" in {
      when(mockImageService.validate(any(), any(), any())).thenReturn(Right(Seq("")))
      val result = fileUpload("test1.png", "test2.jpg", "test3.jpg")

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
    }

    "fail upload multiple files with status BAD_REQUEST" in {
      when(mockImageService.validate(any(), any(), any())).thenReturn(Left(""))
      val result = fileUpload("test6.csv")

      status(result) mustBe BAD_REQUEST
      contentType(result) mustBe Some("text/plain")
    }

    "successfully upload base64 data json with status OK" in {
      when(mockImageService.process(any[Seq[TempFile]], any(), any())).thenReturn(Right(Seq("")))
      val result = dataUpload(base64FilesJson)

      result.map(println)
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
    }

    "fail upload base64 data json with status BAD_REQUEST" in {
      when(mockImageService.process(any(), any(), any())).thenReturn(Left(""))
      val result = dataUpload(base64FileJson)

      status(result) mustBe BAD_REQUEST
      contentType(result) mustBe Some("text/plain")
    }

  }

}