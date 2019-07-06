package com.github.woooking.cosyn.core.config

import better.files.File
import pureconfig._
import pureconfig.generic.auto._

case class CoreConfig(
                         nlpServerUri: String,
                         nlpServerPort: Int,
                     )

object CoreConfig {
    implicit val fileReader: ConfigReader[File] = ConfigReader[String].map(s => File(s))

//    val global: CoreConfig = pureconfig.loadConfig[CoreConfig] match {
//        case Left(failures) =>
//            failures.toList
//                .map(_.description)
//                .foreach(println)
//            ???
//        case Right(value) => value
//    }

    val global: CoreConfig = CoreConfig("http://162.105.88.99", 9000)
}


