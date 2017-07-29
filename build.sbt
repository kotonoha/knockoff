name := "knockoff"

organization := "ws.kotonoha"

version := "0.8.2-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions ++= (scalaVersion.value match {
  case sv if sv startsWith "2.10" => Seq("-language:implicitConversions")
  case _ => Nil
})

libraryDependencies += (scalaVersion.value match {
  case sv if sv startsWith "2.11" => "org.scalatest" %% "scalatest" % "2.2.6" % Test
  case _ => "org.scalatest" %% "scalatest" % "1.9" % "test"
})

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
)

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"

libraryDependencies += "com.googlecode.owasp-java-html-sanitizer" % "owasp-java-html-sanitizer" % "r136"

// Publishing setup to Sonatype's OSS hosting.
//
// We generally do not publish anything but releases for this project.

publishMavenStyle := true

// Do not publish test artifacts
publishArtifact in Test := false

pomExtra := (
  <url>http://tristanjuricek.com/knockoff</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>https://github.com/tristanjuricek/knockoff/blob/master/license.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git://github.com/tristanjuricek/knockoff.git</url>
    <connection>scm:git:git@github.com:tristanjuricek/knockoff.git</connection>
  </scm>
  <developers>
    <developer>
      <id>tristanjuricek</id>
      <name>Tristan Juricek</name>
      <url>http://tristanjuricek.com</url>
    </developer>
  </developers>)

publishTo := Some("knockoff Sonatype releases" at
                  "https://oss.sonatype.org/service/local/staging/deploy/maven2")

credentials += Credentials(Path.userHome / ".sbt" / "sonatype.sbt")
