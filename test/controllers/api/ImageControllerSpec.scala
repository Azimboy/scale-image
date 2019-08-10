package controllers.api

import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.webjars.play.WebJarsUtil
import play.api.test.Helpers._
import services.ImageService
import utils.TestFileUtils._
import org.mockito.Mockito._
import scala.collection.JavaConverters._
import org.mockito.ArgumentMatchers._
import org.mockito.AdditionalMatchers._
import org.scalatest.Matchers
import org.scalatest.matchers.Matcher
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

import scala.concurrent.ExecutionContext

class ImageControllerSpec extends PlaySpec with MockitoSugar {

  implicit val mockWebJars = mock[WebJarsUtil]
  implicit val ec = ExecutionContext.Implicits.global

  val mockImageService = mock[ImageService]
  val controller = new ImageController(stubControllerComponents(), mockImageService)

  "The ImageController" must {
//    "upload a file successfully" in {
//      val tmpFile = FileUploader.TestFilesPath.resolve("test1.png").toFile
////      tmpFile.deleteOnExit()
//
//      val responseFuture = controller.fileUpload(100, 100)()
//      val response = await(responseFuture)
//      response.status mustBe OK
//      response.body mustBe "file size = 11"
//    }

    "upload multiple files with status OK" in {

//      when(mockImageService.tempFilesFolder).thenReturn(tempFilesFolder)
//      when(mockImageService.process(getTempFiles(getFileParts("test1.png")), 100, 100))
//        .thenReturn(Right(Seq("")))
//      when(mockImageService.save(Image.fromPath(getTestPngFile)))
//        .thenReturn(getTestPngFile.toString)
//      when(mockImageService.accumulateResults(Seq(Right("")))).thenReturn(Either.cond(true, Seq(""), ""))
//      when(mockImageService.download("")).thenReturn(Future.successful(Left("")))
      when(mockImageService.validate(any(), 100, 100))
        .thenReturn(Either.cond(true, List(""), ""))
      val fakeRes = getFakeRequest("/api/file-upload", "test1.png")
      val result = controller.fileUpload(100, 100).apply(fakeRes)
      status(result) mustBe OK
    }
  }
}