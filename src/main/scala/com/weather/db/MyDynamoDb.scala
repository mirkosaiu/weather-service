package com.weather.db

import awscala.dynamodbv2.DynamoDB
import com.amazonaws.regions.{ Region, Regions }
import com.weather.MyConfiguration

trait MyDynamoDb extends MyConfiguration {
  // LOCAL
  //  implicit val db = DynamoDB.local()

  // SERVER
  implicit val db: DynamoDB = DynamoDB.at(Region.getRegion(Regions.US_WEST_2))
}