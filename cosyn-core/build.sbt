name := "cosyn-core"

libraryDependencies += "com.github.javaparser" % "javaparser-symbol-solver-core" % "3.12.0"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.6.0"
libraryDependencies += "com.google.guava" % "guava" % "24.0-jre"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"
libraryDependencies += "org.slf4s" %% "slf4s-api" % "1.7.25"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.12"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.5.19"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.3"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.9.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

