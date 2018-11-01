package sfs.server.handlers

import java.nio.file.{Path, Paths}
import java.sql.Timestamp

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK, Unauthorized}
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives.{complete, extract, headerValue, onSuccess}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.BasicDirectives
import akka.http.scaladsl.server.directives.FileUploadDirectives.fileUpload
import akka.stream.scaladsl.{FileIO, Sink}
import akka.stream.{IOResult, Materializer}
import akka.util.ByteString
import sfs.database.AccessToken.{extractAuth, handleAuth}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object File {
  def list: Route = {
    headerValue(extractAuth) { auth =>
      handleAuth(auth) match {
        case Success(userId) =>
          extract(_.request.uri.query()) { query =>
            query.get("file_id") match {
              case Some(fileId) =>
                try {
                  val file: Option[(Int, String, String, String, Timestamp)] = sfs.database.File.findOne(userId, fileId.toInt)
                  val path: String = file.get._3
                  val lines = sfs.database.File.decrypt(path)

                  complete(lines)
                } catch {
                  case _ : java.lang.NumberFormatException => Errors.Client(BadRequest, "file_id must be an Integer")
                  case _ : java.util.NoSuchElementException => Errors.Client(BadRequest, "File not found")
                  case e : Exception => println(e); Errors.Internal()
                }

              case None =>
                val files: String = sfs.database.File.find(userId)

                complete(HttpResponse(OK, entity = HttpEntity(`application/json`, files)))
            }
          }
        case Failure(e) =>
          val message = e.getMessage
          Errors.Client(Unauthorized, s"$message")
      }
    }
  }

  def upload: Route = {
    headerValue(extractAuth) { auth =>
      handleAuth(auth) match {
        case Success(userId) =>
          BasicDirectives.extractRequestContext { ctx =>
            implicit val materializer: Materializer = ctx.materializer

            fileUpload("file") {
              case (fileInfo, fileStream) =>
                val path: Path = Paths.get("/tmp") resolve fileInfo.fileName

                val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
                val writeResult: Future[IOResult] = fileStream.runWith(sink)
                onSuccess(writeResult) { result =>
                  result.status match {
                    case Success(_) =>
//                      encrypt and overwrite file
                      sfs.database.File.encrypt(path)
//                      save file details to db
                      sfs.database.File.save(fileInfo, path, userId)

                      complete(s"Successfully written ${result.count} bytes")
                    case Failure(e) => throw e
                  }
                }
            }
          }
        case Failure(e) =>
          val message: String = e.getMessage
          Errors.Client(Unauthorized, s"$message")
      }
    }
  }
}
