package com.github.woooking.cosyn

import better.files.File
import com.github.woooking.cosyn.dfg.DFG
import com.github.woooking.cosyn.ir.Visitor
import com.github.woooking.cosyn.middleware.{CompilationUnitFilter, FileFilter}
import com.github.woooking.cosyn.mine.{Miner, Setting}

import scala.collection.mutable
import scala.util.Try

class Cosyn(dir: File) {
    val fileFilters = mutable.ArrayBuffer[FileFilter]()
    val compilationUnitFilters = mutable.ArrayBuffer[CompilationUnitFilter]()

    private def getJavaFilesFromDirectory(dir: File): List[File] = {
        dir.listRecursively
            .filter(_.isRegularFile)
            .filter(_.extension.contains(".java"))
            .toList
    }

    def register(filter: FileFilter): Unit = {
        fileFilters += filter
    }

    def register(filter: CompilationUnitFilter): Unit = {
        compilationUnitFilters += filter
    }

    def process(): Unit = {
        implicit val setting = Setting.create()

        val files = (getJavaFilesFromDirectory(dir) /: fileFilters) ((fs, f) => fs.filter(f.valid))
        println(s"总文件数：${files.length}")
        val cus = (files.map(file => Try {
            JavaParser.parseFile(file)
        }).filter(_.isSuccess).map(_.get) /: compilationUnitFilters) ((cus, f) => cus.filter(f.valid))
        println(s"总编译单元数：${cus.length}")
        val cfgs = cus.flatMap(Visitor.generateCFGs)
        println(s"总控制流图数：${cfgs.length}")
        val dfgs = cfgs.map(DFG.apply)
        println(s"总数据流图数：${dfgs.length}")
        Miner.mine(dfgs)
    }
}
