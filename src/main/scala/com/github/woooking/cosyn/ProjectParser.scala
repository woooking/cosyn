package com.github.woooking.cosyn

import better.files.File
import com.github.javaparser.JavaParser
import com.github.woooking.cosyn.javaparser.CompilationUnit

import scala.collection.mutable

class ProjectParser(dir: File) {
    val files: Seq[File] = dir.listRecursively
        .filter(_.isRegularFile)
        .filter(_.extension.contains(".java"))
        .toSeq

    val cus: mutable.Map[String, CompilationUnit] = mutable.Map()

    def parseFile(file: File): CompilationUnit = {
        if (!files.contains(file)) throw new Exception(s"$file is not a file in $dir")
        cus.getOrElseUpdate(file.pathAsString, CompilationUnit(JavaParser.parse(file.toJava)))
    }
}
