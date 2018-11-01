package sfs.server

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, entity, path, post, _}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import sfs.server.handlers.Errors.myExceptionHandler
import sfs.server.handlers.File
import sfs.server.handlers.Models.{loginModel, registerModel}
import sfs.server.handlers.User.{login, register}

import scala.concurrent.ExecutionContextExecutor

object Handler {
  // needed to run the route
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val route: Route = handleExceptions(myExceptionHandler) {
    get { // INDEX
      path("file") { File.list }
    } ~
    post { // INDEX
      path("file") { File.upload }
    } ~
    post {
      path("user" / "login") {
        entity(as[loginModel]) { user => login(email = user.email, password = user.password) }
      }
    } ~
    post {
        path("user" / "register") {
          entity(as[registerModel]) { user => register(email = user.email, name = user.name, password = user.password) }
        }
      }
  }
}