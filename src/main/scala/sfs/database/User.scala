package sfs.database

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.ProvenShape
import java.sql.Timestamp

import org.mindrot.jbcrypt.BCrypt

object User {
  // this is a class that represents the table I've created in the database
  class Users(tag: Tag) extends Table[(Int, String, String, String, Timestamp)](tag, "users") {
    def id: Column[Int] = column[Int]("id")
    def name: Column[String] = column[String]("name")
    def email: Column[String] = column[String]("email")
    def password: Column[String] = column[String]("password")
    def created_at: Column[Timestamp] = column[Timestamp]("created_at")
    def * : ProvenShape[(Int, String, String, String, Timestamp)] = (id, name, email, password, created_at)
  }
  val users: TableQuery[Users] = TableQuery[Users]

  def checkHash(str: String, strHashed: String): Boolean = BCrypt.checkpw(str,strHashed)

  def getHash(str: String) : String = BCrypt.hashpw(str, BCrypt.gensalt())

  def findByEmail(email: String): Option[(Int, String, String, String)]  = {
    Database.forConfig("db") withSession {
      implicit session => users.filter(_.email === email).take(1).map(u => (u.id, u.name, u.email, u.password)).firstOption
    }
  }

  def save(name:String, email: String, password: String): Option[(Int, String, String, String)]  = {
    val hash: String = getHash(password)

    Database.forConfig("db") withSession {
      implicit session =>
//        save the user
        users.map(u => (u.name, u.email, u.password)) += (name, email, hash)
//        return user data
        users.filter(_.email === email).take(1).map(u => (u.id, u.name, u.email, u.password)).firstOption
    }
  }
}
