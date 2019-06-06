package com.github.woooking.cosyn.kg

import better.files.File
import pureconfig.ConfigReader

case class KnowledgeGraphConfig(
                                   uri: String,
                                   username: String,
                                   password: String,
                                   srcCodeDirs: List[String],
                                   jdkSrcCodeDir: String,
                                   entityPackage: String = "com.github.woooking.cosyn.kg.entity"
                               )

object KnowledgeGraphConfig {
    implicit val fileReader: ConfigReader[File] = ConfigReader[String].map(s => File(s))

    val global: KnowledgeGraphConfig = pureconfig.loadConfig[KnowledgeGraphConfig] match {
        case Left(failures) =>
            failures.toList
                .map(_.description)
                .foreach(println)
            ???
        case Right(value) => value
    }
}

