package sfs.database

import java.io.PrintWriter
import java.nio.file.Path
import java.sql.Timestamp

import akka.http.scaladsl.server.directives.FileInfo

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.ProvenShape

object File {
  // Definition of the Files table
  class Files(tag: Tag) extends Table[(Int, String, String, String, String, Int, Timestamp)](tag, "files") {
    def id: Column[Int] = column[Int]("id", O.PrimaryKey) // This is the primary key column
    def name: Column[String] = column[String]("name")
    def path: Column[String] = column[String]("path")
    def ext: Column[String] = column[String]("ext")
    def algorithms: Column[String] = column[String]("algorithms")
    def user_id: Column[Int] = column[Int]("user_id")
    def created_at: Column[Timestamp] = column[Timestamp]("created_at")
    // Every table needs a * projection with the same type as the table's type parameter
    def * : ProvenShape[(Int, String, String, String, String, Int, Timestamp)] =
      (id, name, path, ext, algorithms, user_id, created_at)
  }
  val files: TableQuery[Files] = TableQuery[Files]
  private val KEY: String = "key"

  def decrypt(path: String): String = {
    val lines = scala.io.Source.fromFile(path).mkString
    sfs.encryption.Encryption.decrypt(KEY, lines)
  }

  def encrypt(path: Path): PrintWriter = {
    val lines = scala.io.Source.fromFile(path.toString).mkString
    val encrypted = sfs.encryption.Encryption.encrypt(KEY,lines)

    new PrintWriter(path.toString) { write(encrypted); close() }
  }

  def find(userId: Int): String = {
    Database.forConfig("db") withSession {
      implicit session =>
        val list = files.filter(_.user_id === userId).take(10).map(f => (f.id, f.name, f.algorithms, f.created_at)).results(10)

        var result: String = "["

        while (list.right.get.hasNext){
          val f = list.right.get.next
          result += s"""{"id":${f._1},"name":"${f._2}","algorithms":"${f._3}","created_at":"${f._4}"},"""
        }
//        remove last comma and add a closing bracket
        result.dropRight(1).concat("]")
    }
  }
  def findOne(userId: Int, fileId: Int): Option[(Int, String, String, String, Timestamp)] = {
    Database.forConfig("db") withSession {
      implicit session =>
        files
          .filter(_.user_id === userId)
          .filter(_.id === fileId)
          .take(1)
          .map(f => (f.id, f.name, f.path, f.algorithms, f.created_at)).firstOption
    }
  }

  def save(info: FileInfo, path: Path, userId: Int): Int = {
    Database.forConfig("db") withSession {
      implicit session =>
        val ext: String = info.getContentType.mediaType.fileExtensions.head
        val algorithms: String = "AES"
        //        save the file
        files.map(f => (f.name, f.path, f.ext, f.algorithms, f.user_id)) += (info.fileName, path.toString, ext, algorithms, userId)
    }
  }
}
