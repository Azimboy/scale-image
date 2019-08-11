package services

import java.nio.file.Files

import com.sksamuel.scrimage.Image
import mockws.MockWS
import mockws.MockWSHelpers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.{DefaultFileMimeTypesProvider, HttpConfiguration}
import play.api.mvc.Results.{NotFound, Ok}
import play.api.test.Helpers.GET
import play.api.{Configuration, Environment}
import utils.TestFileUtils._

import scala.concurrent.ExecutionContext

class ImageServiceTest extends PlaySpec with ScalaFutures {

  private val environment = Environment.simple()
  private val config = Configuration.load(environment)
  private val httpConfiguration = HttpConfiguration.fromConfiguration(config, environment)
  private implicit val defaultFileMimeTypes = new DefaultFileMimeTypesProvider(httpConfiguration.fileMimeTypes).get

  implicit val ec = ExecutionContext.Implicits.global

  val mockWs = MockWS {
    case (GET, fileName) =>
      Action {
        val filePath = TestFilesPath.resolve(fileName)
        if (Files.exists(filePath)) {
          Ok.sendPath(filePath)
        } else {
          NotFound("File not found.")
        }
      }
    case _ =>
      Action { NotFound("Url not found.") }
  }

  val imageService = new ImageService(configuration, mockWs)

  "ImageService" should {
    "successfully validate file" in {
      val result = imageService.validate(getFileParts("test1.png"), 100, 100)

      result.map(_.size) mustBe Right(1)
    }

    "successfully validate files" in {
      val result = imageService.validate(getFileParts("test1.png", "test2.jpg", "test3.jpg"), 100, 100)

      result.map(_.size) mustBe Right(3)
    }

    "fail validate csv file" in {
      val result = imageService.validate(getFileParts("test6.csv"), 100, 100)

      result mustBe Left("Uploaded file (test6.csv) is not a valid image. Only JPG and PNG files are allowed.")
    }

    "successfully process file" in {
      val result = imageService.process(getTempFiles(getFileParts("test1.png")), 100, 100)

      result.map(_.size) mustBe Right(1)
    }

    "successfully process files" in {
      val result = imageService.process(getTempFiles(getFileParts("test3.jpg", "test4.jpg", "test5.jpg")), 100, 100)

      result.map(_.size) mustBe Right(3)
    }

    "fail process pdf file" in {
      val result = imageService.validate(getFileParts("test7.pdf"), 100, 100)

      result mustBe Left("Uploaded file (test7.pdf) is not a valid image. Only JPG and PNG files are allowed.")
    }

    "successfully save file" in {
      val result = imageService.save(Image.fromPath(getTestJpgFile))

      println(result)
      result must include (tempFilesFolder)
    }

    "successfully download file" in {
      val result = imageService.download("test1.png")

      whenReady(result) { value =>
        println(value)
        value mustBe Right()
      }
    }
  }

}
