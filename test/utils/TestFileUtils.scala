package utils

import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.ConfigFactory
import mockws.MockWS
import mockws.MockWSHelpers.Action
import models.AppProtocol.TempFile
import play.api.http.{DefaultFileMimeTypesProvider, HttpConfiguration}
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.libs.json.JsValue
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.Results.{NotFound, Ok}
import play.api.test.FakeRequest
import play.api.test.Helpers.{CONTENT_TYPE, GET, JSON, POST}
import play.api.{Configuration, Environment}
import utils.FileUtils.getBytes

import scala.concurrent.ExecutionContext

object TestFileUtils {

  private val environment = Environment.simple()
  private val config = Configuration.load(environment)
  private val httpConfiguration = HttpConfiguration.fromConfiguration(config, environment)
  private implicit val defaultFileMimeTypes = new DefaultFileMimeTypesProvider(httpConfiguration.fileMimeTypes).get

  implicit val ec = ExecutionContext.Implicits.global

  val configFile = new File("test/resources/application_test.conf")
  val configuration = Configuration(ConfigFactory.parseFile(configFile))

  val tempFilesFolder = configuration.get[String]("application.temp-files-path")
  val testFilesFolder = configuration.get[String]("application.test-files-path")

  val TestFilesPath = Paths.get(testFilesFolder)
  val TempFilesPath = Paths.get(tempFilesFolder)

  Files.createDirectories(TempFilesPath)

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

  def getTestPngFile = getTestFiles("test1.png").head
  def getTestJpgFile = getTestFiles("test2.jpg").head

  def getTestFiles(fileNames: String*): List[Path] = {
    fileNames.map(TestFilesPath.resolve).toList
  }

  def getFileParts(fileNames: String*): List[FilePart[TemporaryFile]] = {
    fileNames.map { fileName =>
      FilePart[TemporaryFile](
        key = "contentFile",
        filename = fileName,
        contentType = Some("Content-Type: multipart/form-data"),
        ref = SingletonTemporaryFileCreator.create(TestFilesPath.resolve(fileName))
      )
    }.toList
  }

  def getTempFiles(fileParts: List[FilePart[TemporaryFile]]): List[TempFile] = {
    fileParts.map(file => TempFile(Some(file.filename), file.contentType, getBytes(file.ref.path)))
  }

  def getMultipartData(fileNames: String*): MultipartFormData[TemporaryFile] = {
    MultipartFormData(
      dataParts = Map.empty[String, Seq[String]],
      files = getFileParts(fileNames: _*),
      List()
    )
  }

  def apiFileUpload(files: String*) = {
    FakeRequest(POST, "/api/file-upload")
      .withMultipartFormDataBody(getMultipartData(files: _*))
  }

  def apiDataUpload(jsonBody: JsValue) = {
    FakeRequest(POST, "/api/data-upload")
      .withHeaders(CONTENT_TYPE -> JSON)
      .withJsonBody(jsonBody)
  }

  def apiFromUrl(url: String) = {
    FakeRequest(POST, s"/api/from-url?url=$url")
  }

}
