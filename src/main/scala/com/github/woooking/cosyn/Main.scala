package com.github.woooking.cosyn

import better.files.File
import better.files.File.home
import com.github.woooking.cosyn.middleware.{FileFilter, MethodCallFilter}
import com.github.woooking.cosyn.mine.Setting

object Main {
    def getJavaFilesFromDirectory(dir: File): List[File] = {
        dir.listRecursively
            .filter(_.isRegularFile)
            .filter(_.extension.contains(".java"))
            .toList
    }

    def main(args: Array[String]): Unit = {
        implicit val setting = Setting.create()
        val clientCodes = home / "lab" / "lucene-client-codes"
        val cosyn = new Cosyn(clientCodes)
        cosyn.register(new FileFilter {
            override def valid(file: File): Boolean = file.contentAsString.contains("org.apache.lucene")
        })
        cosyn.register(new MethodCallFilter("search"))
        cosyn.process()
    }
}
