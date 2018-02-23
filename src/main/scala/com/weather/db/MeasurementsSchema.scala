package com.weather.db

import java.util
import java.util.UUID

import jp.co.bizreach.dynamodb4s._
import com.amazonaws.services.dynamodbv2.model._
import com.gu.scanamo.{ Scanamo, Table }
import org.apache.commons.logging.{ Log, LogFactory }

import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

final case class Measurement(id: UUID, hardwareVersion: Option[String], gpsPosition: Option[Int], temperature: Option[Int], humidity: Option[Int], pressure: Option[Int], light: Option[Int])
final case class Measurements(measurements: Seq[Measurement])

object MeasurementSchema extends DynamoTable with MyDynamoDb {
  val log: Log = LogFactory.getLog(MeasurementSchema.getClass)
  val table = "Measurements"

  if (!db.tableNames.contains(table)) {
    db.createTable(
      util.Arrays.asList(new AttributeDefinition("id", ScalarAttributeType.S)),
      table,
      util.Arrays.asList(new KeySchemaElement("id", KeyType.HASH)),
      new ProvisionedThroughput(10L, 10L)
    )
  }

  private val measurement = Table[Measurement](table)

  def addItem(m: Measurement) = Scanamo.exec(db)(measurement.put(m))

  def getAll: Future[List[Measurement]] = Future {
    Scanamo.exec(db)(measurement.scan()).filter {
      case Left(_) =>
        log.error("DynamoReadError occurred."); false
      case Right(_) => true
    }.map {
      case Right(measurement) => measurement
      case _ => null
    }
  }

}
