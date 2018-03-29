package com.github.woooking.cosyn

import better.files.File
import better.files.File.home
import com.github.woooking.cosyn.dfgprocessor.dfg.{DFG, DFGEdge, DFGNode}
import com.github.woooking.cosyn.dfgprocessor.{DFGGenerator, FromDFGGenerator}
import com.github.woooking.cosyn.filter._
import com.github.woooking.cosyn.mine.Setting

object Main {
    def main(args: Array[String]): Unit = {
        implicit val setting = Setting.create(DFGNode.parser, DFGEdge.parser, minFreq = 5, minNodes = 5)
        val clientCodes = home / "lab" / "java-codes"
        //        val clientCodes = home / "lab" / "test"
        //        val clientCodes = home / "lab" / "guava-client-codes"
        //        val clientCodes = home / "lab" / "nio-client-codes"
        //        val clientCodes = home / "lab" / "github" / "setBorderBottom"
        val graphGenerator = DFGGenerator()
        graphGenerator.register(new MethodCallCUFilter("search"))
        graphGenerator.register(new MethodCallDFGFilter("search"))

        val cosyn = new Cosyn[File, DFGNode, DFGEdge, DFG](
            DataSource.fromJavaSourceCodeDir(clientCodes),
            graphGenerator,
            FromDFGGenerator()
        )
        cosyn.register(new SourceContentFilter("org.apache.lucene"))
        cosyn.register(new MethodCallFragmentFilter("search"))
        val result = cosyn.process()
        result.foreach(println)
    }
}
