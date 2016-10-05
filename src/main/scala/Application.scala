package main

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import config.Routes

object Application extends App {
  val mainActorSystem = "system-main"
  implicit val system = ActorSystem(mainActorSystem)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val routes = new Routes
  val bind = Http().bindAndHandle(routes.route, "127.0.0.1", 8080)
}
