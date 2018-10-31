package com.github.woooking.cosyn

import java.nio.file.Path

import better.files.File.home
import com.github.woooking.cosyn.api.Cosyn
import com.github.woooking.cosyn.impl.java.JavaDFGGenerator
import com.github.woooking.cosyn.dfgprocessor.FromDFGGenerator
import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.mine.Setting

object Main {
    def main(args: Array[String]): Unit = {
        implicit val setting = Setting.create(DFGNode.parser, DFGEdge.parser, minFreq = 5, minNodes = 5)

        val clientCodeRoot = home / "lab" / "client-codes" / "poi"
        //        val clientCodeDirs = clientCodeRoot.list.filter(_.isDirectory)
        //        clientCodeDirs.foreach(dir => {
        //            val parser = JavaProjectParser.parse(dir.path)
        //        })

        val graphGenerator = JavaDFGGenerator(None)
        //        graphGenerator.register(new MethodCallCUFilter("search"))
        //        graphGenerator.register(new MethodCallDFGFilter("search"))
        //
        val cosyn = new Cosyn[Path, DFGNode, DFGEdge, SimpleDFG, String](
            clientCodeRoot.path,
            graphGenerator,
            FromDFGGenerator()
        )
        //        cosyn.register(new SourceContentFilter("org.apache.lucene"))
        //        cosyn.register(new MethodCallFragmentFilter("search"))
        val result = cosyn.process()
        result.foreach(println)
    }
}
