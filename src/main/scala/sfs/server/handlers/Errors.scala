package sfs.server.handlers

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.{ExceptionHandler, StandardRoute}

object Errors {
  def Client(status: ClientError, message: String): StandardRoute = {
    val error: String = status.reason
    val statusCode: Int = status.intValue

    complete(HttpResponse(
      status,
      entity = HttpEntity(`application/json`, s"""{"statusCode": $statusCode, "error": "$error", "message": "$message"}""")
    ))
  }

  def Internal(): StandardRoute = {
    val error: String = InternalServerError.reason
    val statusCode: Int = InternalServerError.intValue
    val message = InternalServerError.defaultMessage

    complete(HttpResponse(
      statusCode,
      entity = HttpEntity(`application/json`, s"""{"statusCode": $statusCode, "error": "$error", "message": "$message"}""")
    ))
  }

  val myExceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: Exception =>
      println(e.getClass.getCanonicalName)
      println(e.getMessage)
      extractUri { uri =>
        println(s"Request to $uri could not be handled normally")
        Errors.Internal()
      }
  }
}
