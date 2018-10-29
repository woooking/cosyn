package com.github.woooking.cosyn

import better.files.File
import better.files.File.home
import com.github.woooking.cosyn.api.DataSource
import com.github.woooking.cosyn.api.impl.JavaProjectParser
import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.dfgprocessor.{DFGGenerator, FromDFGGenerator}
import com.github.woooking.cosyn.filter._
import com.github.woooking.cosyn.mine.Setting

object Main {
    def main(args: Array[String]): Unit = {
//        implicit val setting = Setting.create(DFGNode.parser, DFGEdge.parser, minFreq = 5, minNodes = 5)

        val clientCodeRoot = home / "lab" / "client-codes" / "poi"
        val clientCodeDirs = clientCodeRoot.list.filter(_.isDirectory)
        clientCodeDirs.foreach(dir => {
            val parser = new JavaProjectParser(dir.path)
        })

//        val graphGenerator = DFGGenerator(None)
//        graphGenerator.register(new MethodCallCUFilter("search"))
//        graphGenerator.register(new MethodCallDFGFilter("search"))
//
//        val cosyn = new Cosyn[File, DFGNode, DFGEdge, SimpleDFG, String](
//            DataSource.fromJavaSourceCodeDir(clientCodes),
//            graphGenerator,
//            FromDFGGenerator()
//        )
//        cosyn.register(new SourceContentFilter("org.apache.lucene"))
//        cosyn.register(new MethodCallFragmentFilter("search"))
//        val result = cosyn.process()
//        result.foreach(println)
    }
}
