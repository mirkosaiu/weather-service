package com.weather.routes

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete

import scala.concurrent.Future
import akka.util.Timeout
import com.weather.JsonSupport
import com.weather.db.{ Measurement, MeasurementSchema, Measurements }

import scala.concurrent.ExecutionContext.Implicits.global

trait ApiRoutes extends JsonSupport {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem
  lazy val log = Logging(system, classOf[ApiRoutes])
  // other dependencies that UserRoutes use
  def userRegistryActor: ActorRef
  // Required by the `ask` (?) method below
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
        }
    }
}
