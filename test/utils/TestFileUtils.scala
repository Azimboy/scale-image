package utils

import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.ConfigFactory
import models.AppProtocol.TempFile
import play.api.Configuration
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.test.Helpers.POST
import play.api.test.{FakeHeaders, FakeRequest}
import utils.FileUtils.getBytes

object TestFileUtils {

  val configFile = new File("test/resources/application_test.conf")
  val configuration = Configuration(ConfigFactory.parseFile(configFile))

  val tempFilesFolder = configuration.get[String]("application.temp-files-path")
  val testFilesFolder = configuration.get[String]("application.test-files-path")

  val TestFilesPath = Paths.get(testFilesFolder)
  val TempFilesPath = Paths.get(tempFilesFolder)

  Files.createDirectories(TempFilesPath)

  def getTestPngFile = getTestFiles("test1.png").head
  def getTestJpgFile = getTestFiles("test2.jpg").head

  def getTestFiles(fileNames: String*): Seq[Path] = {
    fileNames.map(TestFilesPath.resolve)
  }

  def getFileParts(fileNames: String*): Seq[FilePart[TemporaryFile]] = {
    fileNames.map { fileName =>
      FilePart[TemporaryFile](
        key = "contentFile",
        filename = fileName,
        contentType = Some("Content-Type: multipart/form-data"),
        ref = SingletonTemporaryFileCreator.create(TestFilesPath.resolve(fileName))
      )
    }
  }

  def getTempFiles(fileParts: Seq[FilePart[TemporaryFile]]): Seq[TempFile] = {
    fileParts.map(file => TempFile(Some(file.filename), file.contentType, getBytes(file.ref.path)))
  }

  def getFakeRequest(url: String, fileNames: String*): FakeRequest[MultipartFormData[TemporaryFile]] = {
    val data = MultipartFormData(
      dataParts = Map.empty[String, Seq[String]],
      files = getFileParts(fileNames: _*),
      List()
    )
    FakeRequest(POST, url, FakeHeaders(), data)
  }

}
