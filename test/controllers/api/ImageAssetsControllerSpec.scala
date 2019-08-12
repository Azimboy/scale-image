package controllers.api

import org.apache.commons.io.FileUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.TestFileUtils._

import scala.concurrent.ExecutionContext

class ImageAssetsControllerSpec extends PlaySpec with BeforeAndAfterAll {

  implicit val ec = ExecutionContext.Implicits.global

  val controller = new ImageAssetsController(stubControllerComponents(), configuration)

  override def beforeAll() = {
    FileUtils.copyFileToDirectory(getTestPngFile.toFile, TempFilesPath.toFile)
  }

  override def afterAll() = {
    FileUtils.cleanDirectory(TempFilesPath.toFile)
  }

  "ImageAssetsController" should {

    "successfully download file" in {
      val result = controller.at("test1.png")(FakeRequest())

      status(result) mustBe OK
      contentType(result) mustBe Some(BINARY)
    }

    "fail download file" in {
      val result = controller.at("test2.png")(FakeRequest())

      status(result) mustBe NOT_FOUND
      contentType(result) mustBe Some(TEXT)
      contentAsString(result) mustBe "File not found."
    }

    "fail download not valid file name" in {
      val result = controller.at("t[]//est2.png")(FakeRequest())

      status(result) mustBe BAD_REQUEST
      contentType(result) mustBe Some(TEXT)
      contentAsString(result) mustBe "Please provide a valid file name."
    }

  }

}