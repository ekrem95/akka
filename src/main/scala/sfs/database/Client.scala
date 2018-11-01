package sfs.database

import akka.japi
import sfs.server.handlers.Models.findClient

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.ProvenShape

object Client {
  // Definition of the Clients table
  class Clients(tag: Tag) extends Table[(String, String, String, String, String)](tag, "clients") {
    def id: Column[String] = column[String]("id", O.PrimaryKey) // This is the primary key column
    def c_type: Column[String] = column[String]("type")
    def redirect_uris: Column[String] = column[String]("redirect_uris")
    def default_redirect_uri: Column[String] = column[String]("default_redirect_uri")
    def allowed_grant_types: Column[String] = column[String]("allowed_grant_types")
    // Every table needs a * projection with the same type as the table's type parameter
    def * : ProvenShape[(String, String, String, String, String)] =
      (id, c_type, redirect_uris, default_redirect_uri, allowed_grant_types)
  }
  val clients: TableQuery[Clients] = TableQuery[Clients]

  def findOne(id: String): Option[findClient] = {
    Database.forConfig("db") withSession {
      implicit session =>
        clients.filter(_.id === id).take(1).map(c => (c.id, c.c_type)).firstOption match {
          case Some(c) => japi.Option.Some(findClient(id = c._1, c_type = c._2))
          case None    => None
        }

    }
  }

  def generate():String = {
    Database.forConfig("db") withSession {
      implicit session =>
        val clientId: String = java.util.UUID.randomUUID.toString
        //        save the client
        clients.map(c => (c.id, c.c_type)) += (clientId, "confidential")

        clientId
    }
  }
}
