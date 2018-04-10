package com.weather.db

import java.util
import java.util.UUID

import jp.co.bizreach.dynamodb4s._
import com.amazonaws.services.dynamodbv2.model._
import com.gu.scanamo.{ DynamoFormat, Scanamo, Table }
import org.apache.commons.logging.{ Log, LogFactory }
import org.joda.time.{ DateTime, DateTimeZone }

import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

final case class MeasurementFromStation(temperature: Double, humidity: Double, pressure: Double, luminosity: Int, gas: Int)
final case class Measurement(id: UUID, hardwareVersion: Option[String], gpsPosition: Option[Int], date: Option[DateTime], temperature: Option[Double], humidity: Option[Double], pressure: Option[Double], luminosity: Option[Int], gas: Option[Int])
final case class Measurements(measurements: Seq[Measurement])

object MeasurementSchema extends DynamoTable with MyDynamoDb {
  val log: Log = LogFactory.getLog(MeasurementSchema.getClass)
  val table = "Measurements"

  //  if (!db.tableNames.contains(table)) {
  //    db.createTable(
  //      util.Arrays.asList(new AttributeDefinition("id", ScalarAttributeType.S)),
  //      table,
  //      util.Arrays.asList(new KeySchemaElement("id", KeyType.HASH)),
  //      new ProvisionedThroughput(10L, 10L)
  //    )
  //  }

  implicit val jodaStringFormat = DynamoFormat.coercedXmap[DateTime, String, IllegalArgumentException](DateTime.parse(_).withZone(DateTimeZone.UTC))(_.toString)

  private val measurements = Table[Measurement](table)

  def addItem(m: Measurement) = Scanamo.exec(db)(measurements.put(m))

  def getAll: Future[List[Measurement]] = Future {
    Scanamo.exec(db)(measurements.scan()).filter {
      case Left(_) =>
        log.error("DynamoReadError occurred."); false
      case Right(_) => true
    }.map {
      case Right(measurement) => measurement
      case _ => null
    }
  }

}
