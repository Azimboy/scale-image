package integration

import org.apache.commons.io.FileUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsNull
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.StubData._
import utils.TestFileUtils._

class IntegrationSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll {

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(configuration)
      .build()
  }

  override def beforeAll() = {
    FileUtils.copyFileToDirectory(getTestJpgFile.toFile, TempFilesPath.toFile)
  }

  override def afterAll() = {
    FileUtils.cleanDirectory(TempFilesPath.toFile)
  }

  "Scale Image Application" should {

    "open multipart file upload UI" in {
      route(app, FakeRequest(GET, "/")).map { result =>
        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
        contentAsString(result) must include("Scale Image")
      }
    }

    "show page not found" in {
      route(app, FakeRequest(GET, "/unknown")).map { result =>
        status(result) mustBe NOT_FOUND
        contentAsString(result) mustBe "Page not found."
      }
    }

    "multipart/data successfully upload single file" in {
      route(app, apiFileUpload("test1.png")).map { result =>
        status(result) mustBe OK
        contentType(result) mustBe Some(JSON)
      }
    }

    "multipart/data successfully upload multiple files" in {
      route(app, apiFileUpload("test1.png", "test2.jpg", "test3.jpg", "test4.jpg", "test5.jpg")).map { result =>
        status(result) mustBe OK
        contentType(result) mustBe Some(JSON)
      }
    }

    "multipart/data fail upload csv files" in {
      route(app, apiFileUpload("test6.csv")).map { result =>
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some(TEXT)
//        println(contentAsString(result))
        contentAsString(result) mustBe "Uploaded file (test6.csv) is not a valid image. Only JPG and PNG files are allowed."
      }
    }

    "multipart/data fail upload pdf file with multiple files" in {
      route(app, apiFileUpload("test2.jpg", "test3.jpg", "test7.pdf", "test5.jpg")).map { result =>
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some(TEXT)
        contentAsString(result) mustBe "Uploaded file (test7.pdf) is not a valid image. Only JPG and PNG files are allowed."
      }
    }

    "multipart/data fail upload invalid file" in {
      route(app, apiFileUpload("invalid-image.jpg")).map { result =>
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some(TEXT)
        contentAsString(result) mustBe "Error occurred during scaling image."
      }
    }

    "multipart/data empty file upload show empty result" in {
      route(app, apiFileUpload()).map { result =>
        status(result) mustBe OK
        contentType(result) mustBe Some(JSON)
      }
    }

    "base64/data successfully upload single data" in {
      route(app, apiDataUpload(base64FileJson)).map { result =>
        status(result) mustBe OK
        contentType(result) mustBe Some(JSON)
      }
    }

    "base64/data successfully upload multiple data" in {
      route(app, apiDataUpload(base64FilesJson)).map { result =>
        status(result) mustBe OK
        contentType(result) mustBe Some(JSON)
      }
    }

    "base64/data fail upload txt file" in {
      route(app, apiDataUpload(base64TxtFile)).map { result =>
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some(TEXT)
      }
    }

    "base64/data fail upload invalid data" in {
      route(app, apiDataUpload(invalidJson)).map { result =>
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some(TEXT)
        contentAsString(result) mustBe "Please upload a valid json file."
      }
    }

    "base64/data fail upload empty data" in {
      route(app, apiDataUpload(JsNull)).map { result =>
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some(TEXT)
        contentAsString(result) mustBe "Please upload a valid json file."      }
    }

    "download successfully image" in {
      route(app, apiFromUrl(imageUrl)).map { result =>
        status(result) mustBe OK
        contentType(result) mustBe Some(JSON)
      }
    }

    "download fail image not found" in {
      route(app, apiFromUrl("http://localhost/assest/image/some.jpg")).map { result =>
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some(TEXT)
        contentAsString(result) mustBe "Couldn't connect to image url."
      }
    }

    "successfully get assets file " in {
      route(app, FakeRequest(GET, "/api/image/test2.jpg")).map { result =>
        status(result) mustBe OK
        contentType(result) mustBe Some("image/jpeg")
      }
    }

    "fail get assets file" in {
      route(app, FakeRequest(GET, "/api/image/test1.jpg")).map { result =>
        status(result) mustBe NOT_FOUND
        contentType(result) mustBe Some(TEXT)
        contentAsString(result) mustBe "File not found."
      }
    }

    "fail download not valid file name" in {
      route(app, FakeRequest(GET, "/api/image/sd:**cxvt2")).map { result =>
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some(TEXT)
        contentAsString(result) mustBe "Please provide a valid file name."
      }
    }

  }

}