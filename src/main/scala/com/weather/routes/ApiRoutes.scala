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
import com.weather.db.{Measurement, MeasurementFromStation, MeasurementSchema, Measurements}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

trait ApiRoutes extends JsonSupport {
  implicit def system: ActorSystem
  lazy val log = Logging(system, classOf[ApiRoutes])
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration
  import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

  lazy val apiRoutes: Route = cors() {
    pathPrefix("api" / "measurements") {
      pathEnd(
        get {
          val measurements: Future[Measurements] = MeasurementSchema.getAll.map(Measurements(_))
          complete(measurements)
        } ~
        post { // store measurement from the station
          entity(as[MeasurementFromStation]) { m =>
            val measurement = Measurement(UUID.randomUUID(), Some("1.0"), Some(0), Some(m.temperature), Some(m.humidity), Some(m.pressure), Some(m.luminosity), Some(m.gas))
            MeasurementSchema.addItem(measurement)
            complete("Ok")
          }
        }
      )
    } ~
    pathSingleSlash {
      get {
        complete("Welcome to my App. eccheccazzo!!")
      }
    }
  }

}
