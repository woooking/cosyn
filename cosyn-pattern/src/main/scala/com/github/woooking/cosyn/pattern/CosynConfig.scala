package com.github.woooking.cosyn.pattern

import better.files.File
import pureconfig._
import pureconfig.generic.auto._

case class CosynConfig(
                          clientCodeDir: File,
                          srcCodeDirs: List[File],
                          classFullQualifiedName: String,
                          methodFullQualifiedSignature: String,
                          minFreq: Int = 4,
                          printParseErrors: Boolean = false,
                          printUnsupportedWarnings: Boolean = false,
                          debug: Boolean = false,
                      )

object CosynConfig {
    implicit val fileReader: ConfigReader[File] = ConfigReader[String].map(s => File(s))

    val global: CosynConfig = pureconfig.loadConfig[CosynConfig] match {
        case Left(failures) =>
            failures.toList
                .map(_.description)
                .foreach(println)
            ???
        case Right(value) => value
    }
}


