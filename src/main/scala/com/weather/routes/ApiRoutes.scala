package com.weather.routes

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete

import scala.concurrent.Future
import akka.util.Timeout
import com.weather.JsonSupport
import com.weather.db.{Measurement, MeasurementSchema, Measurements}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

trait ApiRoutes extends JsonSupport {
  implicit def system: ActorSystem
  lazy val log = Logging(system, classOf[ApiRoutes])
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val apiRoutes: Route =
    pathPrefix("api" / "measurements") {
      get {
        val measurements: Future[Measurements] = MeasurementSchema.getAll.map(Measurements(_))
        complete(measurements)
      } ~
      post {
        entity(as[Measurement]) { m =>
          MeasurementSchema.addItem(m)
          complete("Ok")
        }
      } ~
      path("generate-random") {
        get {
          val m = Measurement(UUID.randomUUID(), Some("v1"), Some(Random.nextInt(50)), Some(Random.nextInt(50)), Some(Random.nextInt(50)), Some(Random.nextInt(50)), Some(Random.nextInt(50)))
          MeasurementSchema.addItem(m)

          complete("Ok")
        }
      }
    } ~
    pathPrefix("api" / Segment / IntNumber) { (key, value) =>
      get {
        complete(s"$key=$value")
      }
    } ~
    pathPrefix(Segment / Segment / Segment) { (name1, name2, adjective) =>
      get {
        complete(s"$name1 e $name2 sono $adjective.")
      }
    } ~
    pathSingleSlash {
      get {
        complete("Welcome to my App. eccheccazzo!!")
      }
    }

}
