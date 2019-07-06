package com.github.woooking.cosyn.kg

import java.nio.file.Path

import better.files.File
import pureconfig._
import pureconfig.generic.auto._

case class KnowledgeGraphConfig(
                                   uri: String,
                                   username: String,
                                   password: String,
                                   srcCodeDirs: List[String],
                                   jdkSrcCodeDir: String,
                                   debug: Boolean = false,
                                   entityPackage: String = "com.github.woooking.cosyn.kg.entity"
                               )

object KnowledgeGraphConfig {
    implicit val fileReader: ConfigReader[File] = ConfigReader[String].map(s => File(s))

//    val global: KnowledgeGraphConfig = pureconfig.loadConfig[KnowledgeGraphConfig] match {
//        case Left(failures) =>
//            failures.toList
//                .map(_.description)
//                .foreach(println)
//            ???
//        case Right(value) => value
//    }

    val global: KnowledgeGraphConfig = KnowledgeGraphConfig("bolt://162.105.88.99", "neo4j", "neo4jpoi", List("/home/woooking/lab/poi-3.14/src/java",
        "/home/woooking/lab/poi-3.14/src/ooxml/java"), "/home/woooking/lab/jdk-11/src")
}

