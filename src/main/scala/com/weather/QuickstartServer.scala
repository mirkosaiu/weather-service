package com.weather

import actors.UserRegistryActor
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.joda.time.DateTime
import routes.ApiRoutes

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object QuickstartServer extends ApiRoutes {

  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //  val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

  lazy val routes: Route = apiRoutes

  def main(args: Array[String]): Unit = {

    println(DateTime.now)

    Http().bindAndHandle(routes, "0.0.0.0", 8080)
    println(s"Server online at http://localhost:8080/")
    Await.result(system.whenTerminated, Duration.Inf)
  }
}
