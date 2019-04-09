name := "cosyn-common"

val circeVersion = "0.11.1"

libraryDependencies += "com.github.javaparser" % "javaparser-symbol-solver-core" % "3.12.0"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.6.0"
libraryDependencies += "com.google.guava" % "guava" % "27.0-jre"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.5.0"
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"
libraryDependencies += "org.slf4s" %% "slf4s-api" % "1.7.25"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"


libraryDependencies += "io.circe" %% "circe-core" % circeVersion
libraryDependencies += "io.circe" %% "circe-generic" % circeVersion
libraryDependencies += "io.circe" %% "circe-parser" % circeVersion

