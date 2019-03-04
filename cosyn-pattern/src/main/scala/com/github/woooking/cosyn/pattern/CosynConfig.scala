package com.github.woooking.cosyn.pattern

import better.files.File.home

case class CosynConfig(debug: Boolean = false)

object CosynConfig {
    val clientCodeDir = home / "data" / "client-codes" / "fill-cell-color"

//    val srcCodeDirs = Array(
//        home / "data" / "poi-4.0.1" / "src" / "java" path,
//        home / "data" / "poi-4.0.1" / "src" / "ooxml" / "java" path,
//        home / "data" / "jdk-11" / "src" path,
//    )

    val srcCodeDirs = Array(
        home / "lab" / "poi-4.0.0" / "src" / "java" path,
        home / "lab" / "jdk-11" / "src" path,
    )

    val global = CosynConfig(
        debug = true
    )
}


