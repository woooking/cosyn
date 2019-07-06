package com.github.woooking.cosyn.pattern

import better.files.File
import pureconfig._
import pureconfig.generic.auto._

case class CosynConfig(
                          clientCodeDir: File,
                          srcCodeDirs: List[File],
                          methodFullQualifiedSignature: String,
                          fileContents: List[String] = List(),
                          minFreq: Int = 4,
                          printFileNames: Boolean = false,
                          printParseErrors: Boolean = false,
                          printUnsupportedWarnings: Boolean = false,
                          debug: Boolean = false,
                      )

object CosynConfig {
    implicit val fileReader: ConfigReader[File] = ConfigReader[String].map(s => File(s))

//    val global: CosynConfig = pureconfig.loadConfig[CosynConfig] match {
//        case Left(failures) =>
//            failures.toList
//                .map(_.description)
//                .foreach(println)
//            ???
//        case Right(value) => value
//    }

    val global: CosynConfig = CosynConfig(File("/home/woooking/lab/client-codes/test/create-hyperlink"), List(File("/home/woooking/lab/poi-4.0.0/src/java"),
        File("/home/woooking/lab/jdk-11/src")), "org.apache.poi.ss.usermodel.CreationHelper.createHyperlink(org.apache.poi.common.usermodel.HyperlinkType)")
}


