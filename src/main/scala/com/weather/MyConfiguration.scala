package com.weather

import awscala.Credentials
import com.amazonaws.regions.{ Region, Regions }
import com.typesafe.config.ConfigFactory

trait MyConfiguration {

  implicit val region: Region = com.amazonaws.regions.Region.getRegion(Regions.US_WEST_2)

  val env = System.getenv("env")

  sealed abstract class Env(val name: String)
  case object Local extends Env("Local")
  case object Test extends Env("Test")
  case object Dev extends Env("Dev")
  case object Staging extends Env("Staging")
  case object Prod extends Env("Prod")

  private val conf = env match {
    case Local.name => ConfigFactory.load("application.local")
    case Test.name => ConfigFactory.load("application.test")
    case Dev.name => ConfigFactory.load("application.dev")
    case Staging.name => ConfigFactory.load("application.staging")
    case Prod.name => ConfigFactory.load("application.prod")
    case _ => ConfigFactory.load("application")
  }

  val awsAccessKeyId: String = conf.getString("aws.accessKeys.accessKeyId")
  val awsSecretAccessKey: String = conf.getString("aws.accessKeys.secretAccessKey")

  val awsCredentials = Credentials.apply(awsAccessKeyId, awsSecretAccessKey)

}
