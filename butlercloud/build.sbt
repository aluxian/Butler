name := "ButlerCloud"

version := "0.1"

scalaVersion := "2.10.4"

resolvers ++= Seq(
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Kamon Repository" at "http://repo.kamon.io",
  "neo" at "http://m2.neo4j.org/releases"
)

// Akka
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT",
  "com.typesafe.akka" %% "akka-camel" % "2.4-SNAPSHOT",
  "org.apache.camel" % "camel-netty" % "2.14.0",
  "org.apache.camel" % "camel-ahc" % "2.14.0"
  // TODO match camel versions
)

// XMPP Client for GCM
libraryDependencies ++= Seq(
  "org.igniterealtime.smack" % "smack-core" % "4.0.5",
  "org.igniterealtime.smack" % "smack-tcp" % "4.0.5"
)

// Utils
libraryDependencies ++= Seq(
  "net.liftweb" % "lift-json_2.10" % "3.0-M1",
  "com.sun.jersey" % "jersey-core" % "1.18.2",
  "org.slf4j" % "slf4j-simple" % "1.7.7"
)

// Databases
libraryDependencies ++= Seq(
  "org.neo4j" % "neo4j" % "2.1.5",
  "org.neo4j" % "neo4j-rest-graphdb" % "2.0.1",
  "org.reactivemongo" %% "reactivemongo" % "0.11.0-SNAPSHOT"
)

// Kamon monitoring
libraryDependencies ++= Seq(
  "io.kamon" %% "kamon-core" % "0.3.5",
  "io.kamon" %% "kamon-statsd" % "0.3.5",
  "org.aspectj" % "aspectjweaver" % "1.8.4"
)

// Testing
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % "2.3.7" % "test",
  "org.scalatest" %% "scalatest" % "2.2.2" % "test"
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xlint", "-Ywarn-dead-code", "-feature")

parallelExecution in Test := false

parallelExecution in ScoverageTest := false

javaOptions in run += "-javaagent:" +
  System.getProperty("user.home") + "/.ivy2/cache/org.aspectj/aspectjweaver/jars/aspectjweaver-1.8.4.jar"

fork := true

testOptions in Test <+= (target in Test) map {
  t => Tests.Argument("-u", (t / "../shippable/testresults").toString)
}

instrumentSettings

ScoverageKeys.minimumCoverage := 70

ScoverageKeys.failOnMinimumCoverage := false

ScoverageKeys.highlighting := true
