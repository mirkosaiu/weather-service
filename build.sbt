lazy val akkaHttpVersion = "10.0.11"
lazy val akkaVersion    = "2.5.8"

enablePlugins(JavaAppPackaging)
enablePlugins(JavaServerAppPackaging)
enablePlugins(RpmPlugin)
enablePlugins(SystemdPlugin)
enablePlugins(DockerPlugin)

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "mirkosaiu",
      scalaVersion    := "2.12.4"
    )),
    name := "weather-service",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "jp.co.bizreach" %% "aws-dynamodb-scala" % "0.0.6",
      "com.gu" %% "scanamo" % "1.0.0-M3",

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
    )
  )

import com.typesafe.sbt.packager.docker._

// use += to add an item to a Sequence
dockerCommands += Cmd("EXPOSE", "8080")
