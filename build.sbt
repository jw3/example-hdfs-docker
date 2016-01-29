enablePlugins(JavaAppPackaging)

organization := "com.github.jw3"
name := "example-docker-hdfs"
version := "0.1"
scalaVersion := "2.11.7"
scalacOptions += "-target:jvm-1.8"
resolvers += "jw3 at bintray" at "https://dl.bintray.com/jw3/maven"
licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

libraryDependencies ++= {
    val akkaVersion = "2.4.1"
    val akkaStreamVersion = "2.0.1"

    Seq(
        "wiii" %% "awebapi" % "0.3",

        "io.spray" %% "spray-json" % "1.3.2",
        "com.typesafe" % "config" % "1.3.0",
        "net.ceedubs" %% "ficus" % "1.1.2",

        "org.apache.hadoop" % "hadoop-client" % "2.7.2",
        "org.twitter4j" % "twitter4j-core" % "4.0.4",
        "org.twitter4j" % "twitter4j-stream" % "4.0.4",
        "com.softwaremill.reactivekafka" %% "reactive-kafka-core" % "0.9.0",

        "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
        "ch.qos.logback" % "logback-classic" % "1.1.3",

        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % Runtime,

        "org.scalatest" %% "scalatest" % "2.2.5" % Test,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
        "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamVersion % Test
    )
}

////////////////////////
// docker
//
import com.typesafe.sbt.packager.docker.ExecCmd

dockerBaseImage := "anapsix/alpine-java:jre8"
dockerExposedPorts := Seq(8080)
dockerCommands ++= {
    val entrypointPath = s"${(defaultLinuxInstallLocation in Docker).value}/bin/${executableScriptName.value}"
    Seq(
        ExecCmd("RUN", "chmod", "+x", entrypointPath)
    )
}

////////////////////////
// assembly
//
mainClass in assembly := Option("wiii.Boot")
assembleArtifact in assemblyPackageScala := true
assemblyMergeStrategy in assembly := {
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
}
test in assembly := {}
