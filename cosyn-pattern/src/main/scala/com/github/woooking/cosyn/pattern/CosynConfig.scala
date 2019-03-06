package com.github.woooking.cosyn.pattern

import java.nio.file.Path

import better.files.File
import better.files.File.home

object CosynConfig {
    val debug: Boolean = false

    val clientCodeDir: File = home / "data" / "client-codes" / "fill-cell-color"

//    val srcCodeDirs = Array(
//        home / "data" / "poi-4.0.1" / "src" / "java" path,
//        home / "data" / "poi-4.0.1" / "src" / "ooxml" / "java" path,
//        home / "data" / "jdk-11" / "src" path,
//    )

    val srcCodeDirs: Array[Path] = Array(
        home / "lab" / "poi-4.0.0" / "src" / "java" path,
        home / "lab" / "jdk-11" / "src" path,
    )

    val resultDir: File = home / "lab" / "patterns"
}


