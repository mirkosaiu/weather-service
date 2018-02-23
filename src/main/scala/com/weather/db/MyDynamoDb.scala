package com.weather.db

import awscala.dynamodbv2.DynamoDB
import com.weather.MyConfiguration

trait MyDynamoDb extends MyConfiguration {
  // LOCAL
  //  implicit val db = DynamoDB.local()

  // SERVER
  implicit val db: DynamoDB = DynamoDB.apply(awsCredentials)
}