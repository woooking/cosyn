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
        implicit val setting = Setting.create(minFreq = 10)
        val clientCodes = home / "lab" / "lucene-client-codes"
//        val clientCodes = home / "lab" / "test"
//        val clientCodes = home / "lab" / "guavaclient-codes"
        val cosyn = new Cosyn(clientCodes)
//        cosyn.register(new FileFilter {
//            override def valid(file: File): Boolean = file.contentAsString.contains("com.google.common")
//        })
        cosyn.register(new FileContentFilter("org.apache.lucene.search"))
        cosyn.register(new MethodCallCUFilter("search"))
        cosyn.register(new MethodCallDFGFilter("search"))
//        cosyn.register(new DataNodeDFGFilter("HashMultiset"))
        cosyn.process()
    }
}
