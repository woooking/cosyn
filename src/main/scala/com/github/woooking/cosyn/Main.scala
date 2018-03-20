package com.github.woooking.cosyn

import better.files.File
import better.files.File.home
import com.github.woooking.cosyn.filter._
import com.github.woooking.cosyn.mine.Setting

object Main {
    def getJavaFilesFromDirectory(dir: File): List[File] = {
        dir.listRecursively
            .filter(_.isRegularFile)
            .filter(_.extension.contains(".java"))
            .toList
    }

    def main(args: Array[String]): Unit = {
        implicit val setting = Setting.create(minFreq = 5)
//        val clientCodes = home / "lab" / "lucene-client-codes"
//        val clientCodes = home / "lab" / "test"
//        val clientCodes = home / "lab" / "guava-client-codes"
        val clientCodes = home / "lab" / "nio-client-codes"
        val cosyn = new Cosyn(clientCodes)
        cosyn.register(new FileContentFilter("java.nio.channels"))
        cosyn.register(new MethodCallCUFilter("open"))
        cosyn.register(new MethodCallDFGFilter("open"))
//        cosyn.register(new DataNodeDFGFilter("HashMultiset"))
        cosyn.process()
    }
}
