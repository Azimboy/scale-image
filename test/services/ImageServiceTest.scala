package services

import akka.stream.Materializer
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData.FilePart
import utils.TestFileUtils
import utils.TestFileUtils.testFilesPath
import scala.concurrent.ExecutionContext
import org.mockito.Mockito._

class ImageServiceTest extends PlaySpec with MockitoSugar {

  implicit val ec = ExecutionContext.Implicits.global
  implicit val mat = mock[Materializer]
  implicit val ws = mock[WSClient]

  val conf = Configuration(TestFileUtils.parsedConfig)
  val imageService = new ImageService(conf, ws)

//  "asdfasdf" should {
//    "Image service" in {
//      when(imageService.validate(Seq(FilePart[TemporaryFile](
//        key = "contentFile",
//        filename = "",
//        contentType = Some("Content-Type: multipart/form-data"),
//        ref = SingletonTemporaryFileCreator.create(testFilesPath.resolve("asd"))
//      )))).the
//    }
//  }

}
