package sfs.server.handlers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import sfs.database.AccessToken.NewJWTWithClaims
import sfs.database.User._
import sfs.server.handlers.Models.registerResponse

object User {
  def login(email: String, password: String): StandardRoute = {
    val user: Option[(Int, String, String, String)] = findByEmail(email=email)

    user match {
      case Some(u) =>
        if(!checkHash(password, u._4)){
          return Errors.Client(status = BadRequest, message = "Password does not match the confirm password")
        }

        val token: String = NewJWTWithClaims(u._1)
        complete(registerResponse(access_token = token))


      case None => Errors.Client(status = BadRequest, message = "User does not exist")
    }
  }

  def register(name: String, email: String, password: String): StandardRoute = {
    val exists: Option[(Int, String, String, String)] = findByEmail(email=email)

    exists match {
      case Some(_) => Errors.Client(status = BadRequest, message = "The email address you have entered is already registered")
      case None =>
        val result: Option[(Int, String, String, String)] = save(name, email, password)

        result match {
          case Some(u) =>
            val token: String = NewJWTWithClaims(u._1)
            complete(registerResponse(access_token = token))
          case None => Errors.Internal()
        }
    }
  }
}
