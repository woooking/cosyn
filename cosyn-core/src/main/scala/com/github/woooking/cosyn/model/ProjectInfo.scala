package com.github.woooking.cosyn.model

import better.files.File
import better.files._

case class ProjectInfo(name: String, root: File, specialRoots: List[File]) {
    def roots: List[File] = {
        root.listRecursively
            .filter(_.isDirectory)
            .filter(_.toString().endsWith("/src/main/java"))
            .toList ++ specialRoots
    }
}

object ProjectInfo {
    val projects = List(
        ProjectInfo(
            "Android-Universal-Image-Loader",
            file"/home/woooking/lab/java-codes/Android-Universal-Image-Loader-master",
            List()
        ),
        ProjectInfo(
            "AndroidUtilCode",
            file"/home/woooking/lab/java-codes/AndroidUtilCode-master",
            List()
        ),
        ProjectInfo(
            "bamboo",
            file"/home/woooking/lab/java-codes/bamboo-master",
            List()
        ),
        ProjectInfo(
            "butterknife",
            file"/home/woooking/lab/java-codes/butterknife-master",
            List()
        ),
        ProjectInfo(
            "elasticsearch",
            file"/home/woooking/lab/java-codes/elasticsearch-master",
            List()
        ),
        ProjectInfo(
            "glide",
            file"/home/woooking/lab/java-codes/glide-master",
            List()
        ),
        ProjectInfo(
            "butterknife",
            file"/home/woooking/lab/java-codes/butterknife-master",
            List()
        ),
        ProjectInfo(
            "butterknife",
            file"/home/woooking/lab/java-codes/butterknife-master",
            List()
        ),
    )
}
