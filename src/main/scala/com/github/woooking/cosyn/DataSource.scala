package com.github.woooking.cosyn

import better.files.File

trait DataSource[Data] {
    def data: Seq[Data]
}

object DataSource {
    private class JavaSourceCodeDirectory(dir: File) extends DataSource[File] {
        override def data: Seq[File] = dir.listRecursively
            .filter(_.isRegularFile)
            .filter(_.extension.contains(".java"))
            .toSeq
    }

    def fromJavaSourceCodeDir(dir: File): DataSource[File] = new JavaSourceCodeDirectory(dir)

    def fromJavaSourceCodeDir(dir: String): DataSource[File] = new JavaSourceCodeDirectory(File(dir))
}
