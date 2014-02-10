name := "runtime"

version := "1.0-SNAPSHOT"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)

libraryDependencies+= "info.bigdatahowto" % "bd-api" % "1.0-SNAPSHOT" changing()

play.Project.playScalaSettings
