package services

import mockws.MockWSHelpers._
import org.scalatest.{AsyncFunSuite, MustMatchers}
import utils.StubData.dimension
import utils.TestFileUtils.{configuration, getFileParts, getTempFiles, mockWs}

import scala.concurrent.ExecutionContext

class ImageServiceTest extends AsyncFunSuite with MustMatchers {

  implicit val ec = ExecutionContext.Implicits.global

  val imageService = new ImageService(configuration, mockWs)

  test("successfully validate file") {
    imageService.validate(getFileParts("test1.png"), dimension).map { value =>
      value.map(_.size) mustBe Right(1)
    }
  }

  test("successfully validate files") {
    imageService.validate(getFileParts("test1.png", "test2.jpg", "test3.jpg"), dimension).map { value =>
      value.map(_.size) mustBe Right(3)
    }
  }

  test("fail validate csv file") {
    imageService.validate(getFileParts("test6.csv"), dimension).map { value =>
      value mustBe Left("Uploaded file (test6.csv) is not a valid image. Only JPG and PNG files are allowed.")
    }
  }

  test("fail validate pdf files") {
    imageService.validate(getFileParts("test3.jpg", "test7.pdf", "test5.jpg"), dimension).map { value =>
      value mustBe Left("Uploaded file (test7.pdf) is not a valid image. Only JPG and PNG files are allowed.")
    }
  }

  test("successfully process file") {
    imageService.process(getTempFiles(getFileParts("test1.png")), dimension).map { value =>
      value.map(_.size) mustBe Right(1)
    }
  }

  test("successfully process files") {
    imageService.process(getTempFiles(getFileParts("test3.jpg", "test4.jpg", "test5.jpg")), dimension).map { value =>
      value.map(_.size) mustBe Right(3)
    }
  }

  test("fail process pdf file") {
    imageService.process(getTempFiles(getFileParts("test7.pdf")), dimension).map { value =>
      value mustBe Left("Error occurred during scaling image.")
    }
  }

  test("successfully download file") {
    imageService.download("test1.png").map { value =>
      value.isRight mustBe true
    }
  }

  test("fail download file") {
    imageService.download("other.file").map { value =>
      value mustBe Left("Couldn't download file. ResponseStatus: 404")
    }
  }

}
