package com.github.woooking.cosyn.lab

import better.files.File.home
import com.github.woooking.cosyn.dfgprocessor.SimpleVisitor
import com.github.woooking.cosyn.filter._
import com.github.woooking.cosyn.JavaParser
import com.github.woooking.cosyn.cosyn.DataSource

import scala.util.Try
object Cao {
    def main(args: Array[String]): Unit = {
        val clientCodes = home / "lab" / "test"
        val dataSource = DataSource.fromJavaSourceCodeDir(clientCodes)
        val sourceContentFilter = new SourceContentFilter("org.apache.lucene")
        val files = dataSource.data.filter(sourceContentFilter.valid)
        val cus = files.map(f => Try {
            JavaParser.parseFile(f)
        }).filter(_.isSuccess).map(_.get)
        val cfgs = cus.flatMap(new SimpleVisitor().generateCFGs)
        val bodyFilters: Seq[NodeFilter] = Seq(
            new ObjectCreationCUFilter("TermQuery"),
            new ObjectCreationCUFilter("Term"),
            new ObjectCreationCUFilter("PhraseQuery"),
            new MethodCallCUFilter("add")
        )
        val bodys = (cfgs.map(_.decl) /: bodyFilters)((bs, f) => bs.filter(b => f.valid(b.delegate)))
        bodys.map(_.delegate).foreach(b => {
            println("=====")
            println(b)
        })
    }
}
