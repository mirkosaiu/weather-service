package com.weather

import java.util.UUID

import actors.UserRegistryActor.ActionPerformed
import actors.{User, Users}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.weather.db.{Measurement, MeasurementFromStation, Measurements}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._
  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)
    def read(value: JsValue) = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ => throw new DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }
  implicit val userJsonFormat = jsonFormat3(User)
  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val measurementJsonFormat = jsonFormat9(Measurement)
  implicit val measurementsJsonFormat = jsonFormat1(Measurements)
  implicit val measurementsFromStationJsonFormat = jsonFormat5(MeasurementFromStation)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
