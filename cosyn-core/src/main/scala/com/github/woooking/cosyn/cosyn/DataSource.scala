package com.github.woooking.cosyn.cosyn

import better.files.File

trait DataSource[Data] extends Element[Unit, Seq[Data]] {
    def data: Seq[Data]
}

object DataSource {
    private class JavaSourceCodeDirectory(dir: File) extends DataSource[File] {
        override def data: Seq[File] = dir.listRecursively
            .filter(_.isRegularFile)
            .filter(_.extension.contains(".java"))
            .toSeq

        override def process(input: Unit): Seq[File] = data
    }

    def fromJavaSourceCodeDir(dir: File): DataSource[File] = new JavaSourceCodeDirectory(dir)

    def fromJavaSourceCodeDir(dir: String): DataSource[File] = new JavaSourceCodeDirectory(File(dir))
}
