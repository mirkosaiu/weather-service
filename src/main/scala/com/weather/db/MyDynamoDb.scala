package com.weather.db

import awscala.dynamodbv2.DynamoDB
import com.amazonaws.regions.{ Region, Regions }
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.weather.MyConfiguration

trait MyDynamoDb extends MyConfiguration {
  // LOCAL
  //  implicit val db = DynamoDB.local()

  // SERVER
  //  implicit val db: DynamoDB = DynamoDB.at(Region.getRegion(Regions.US_WEST_2))

  import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
  import com.amazonaws.regions.Regions

  // This client will default to US West (Oregon)
  implicit val db: AmazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_WEST_2).build()
}