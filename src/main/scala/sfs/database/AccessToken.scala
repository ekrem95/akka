package sfs.database

import java.sql.Timestamp

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.Authorization
import org.mindrot.jbcrypt.BCrypt
import pdi.jwt.{Jwt, JwtAlgorithm, JwtOptions}
import sfs.server.handlers.Models
import sfs.server.handlers.Models.findAccessToken
import spray.json._

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.ProvenShape
import scala.util.{Failure, Success, Try}

object AccessToken {
  // Definition of the AccessTokens table
  class AccessTokens(tag: Tag) extends Table[(String, String, Timestamp, String, String, String, Int, String)](tag, "access_tokens") {
    def id: Column[String] = column[String]("id", O.PrimaryKey) // This is the primary key column
    def client_id: Column[String] = column[String]("client_id")
    def expires: Column[Timestamp] = column[Timestamp]("expires")
    def refresh_token: Column[String] = column[String]("refresh_token")
    def scopes: Column[String] = column[String]("scopes")
    def secret: Column[String] = column[String]("secret")
    def user_id: Column[Int] = column[Int]("user_id")
    def redirect_uri: Column[String] = column[String]("redirect_uri")
    // Every table needs a * projection with the same type as the table's type parameter
    def * : ProvenShape[(String, String, Timestamp, String, String, String, Int, String)] =
      (id, client_id, expires, refresh_token, scopes, secret, user_id, redirect_uri)
  }
  val atokens: TableQuery[AccessTokens] = TableQuery[AccessTokens]
  val defaultScopes: String = "read-write"

  def extractAuth: HttpHeader => Option[`Authorization`] = {
    case a: `Authorization` => Some(a)
    case _                  => None
  }

  def handleAuth(a: `Authorization`): Try[Int] = {
    val token: String = a.credentials.token
    val decode: Try[(String, String, String)] = Jwt.decodeRawAll(token, JwtOptions(signature = false))

    try {
//      get content of authorization header
      val decoded: Map[String, JsValue] = decode.get._2.parseJson.asJsObject.fields
      val clientId: Option[JsValue] = decoded.get("client_id")
      val accessTokenId: Option[JsValue] = decoded.get("access_token_id")

      val client: Option[Models.findClient] = sfs.database.Client.findOne(clientId.get.toString.replaceAll("^\"|\"$", ""))
      val accessToken: Option[findAccessToken] = findOne(accessTokenId.get.toString.replaceAll("^\"|\"$", ""))

      if(client.isEmpty || accessToken.isEmpty) {
        return Failure(new Exception("Invalid Access Token"))
      }

      if(client.get.id != accessToken.get.client_id) {
        return Failure(new Exception("Invalid Access Token"))
      }

//      decode with access token secret
      if(Jwt.decodeRawAll(token, accessToken.get.secret, Seq(JwtAlgorithm.HS256)).isFailure) {
        return Failure(new Exception("Malformed Access Token"))
      }

      Success(accessToken.get.user_id)
    } catch {
      case _: java.util.NoSuchElementException => Failure(new Exception("Malformed Access Token"))
      case e: Exception => println(e); Failure(new Exception("Unauthorized"))
    }
  }

  def findOne(id: String): Option[findAccessToken] = {
    Database.forConfig("db") withSession {
      implicit session =>
        atokens.filter(_.id === id).take(1).map(a =>
          (a.id, a.client_id, a.scopes, a.secret, a.user_id)).firstOption match {
          case Some(a) => Some(findAccessToken(
            id = a._1, client_id = a._2, scopes = a._3, secret = a._4, user_id = a._5
          ))
          case None => None
        }
    }
  }

  def generate(clientId: String, userId: Int): (String, String) = {
    Database.forConfig("db") withSession {
      implicit session =>
        val accessTokenId: String = java.util.UUID.randomUUID.toString
        val secret = BCrypt.hashpw(java.util.UUID.randomUUID.toString, BCrypt.gensalt())
        //        save the token
        atokens.map(a => (a.id, a.client_id, a.scopes, a.secret, a.user_id)) +=
          (accessTokenId, clientId, defaultScopes, secret, userId)

        (accessTokenId, secret)
    }
  }

  def NewJWTWithClaims(userId: Int): String = {
    val clientId: String = Client.generate()
    val accessToken: (String, String) = generate(clientId, userId)
    val accessTokenId = accessToken._1
    val secret = accessToken._2

    val claim = s"""{"client_id":"$clientId","access_token_id":"$accessTokenId"}"""

    Jwt.encode(claim, secret, JwtAlgorithm.HS256)
  }
}
