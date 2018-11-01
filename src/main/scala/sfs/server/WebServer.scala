package sfs.server

import akka.http.scaladsl.Http

import scala.io.StdIn
import sfs.server.Handler.{executionContext, materializer, route, system}

import scala.concurrent.Future

object WebServer {

  def main(args: Array[String]) {
    val host: String = "localhost"
    val port: Int = 8080

    val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, host, port)
    println(s"Server online at http://$host:$port")

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done

  }
}