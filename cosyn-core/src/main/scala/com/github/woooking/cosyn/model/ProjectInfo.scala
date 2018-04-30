package com.github.woooking.cosyn.model

import better.files.File
import better.files._

case class ProjectInfo(name: String, roots: List[File])

object ProjectInfo {
    val projects = List(
        ProjectInfo("Android-Universal-Image-Loader", List(
            file"/home/woooking/lab/java-codes/Android-Universal-Image-Loader-master/library/src/main/java",
            file"/home/woooking/lab/java-codes/Android-Universal-Image-Loader-master/sample/src/main/java"
        ))
    )
}
