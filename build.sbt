name := "spray-akka-sample-heroku"

version := "1.0"

scalaVersion := "2.11.5"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

enablePlugins(JavaAppPackaging)

libraryDependencies ++= {
  val akkaVersion = "2.3.9"
  val sprayVersion = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayVersion,
    "io.spray"            %%  "spray-routing" % sprayVersion,
    "io.spray"            %%  "spray-testkit" % sprayVersion  % "test",
    "io.spray"            %% "spray-json"     % sprayVersion,
    "com.typesafe.akka"   %%  "akka-actor"    % akkaVersion,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaVersion   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
    "com.github.tototoshi" %% "scala-csv"     % "1.3.0"
  )
}