package com.github.woooking.cosyn

import better.files.File
import better.files.File.home
import com.github.woooking.cosyn.middleware._
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
//        val clientCodes = home / "lab" / "lucene-client-codes"
        val clientCodes = home / "lab" / "test"
//        val clientCodes = home / "lab" / "guava-client-codes"
        val cosyn = new Cosyn(clientCodes)
//        cosyn.register(new FileFilter {
//            override def valid(file: File): Boolean = file.contentAsString.contains("com.google.common")
//        })
//        cosyn.register(new MethodCallCUFilter("create"))
//        cosyn.register(new MethodCallDFGFilter("create"))
//        cosyn.register(new DataNodeDFGFilter("HashMultiset"))
        cosyn.process()
    }
}
