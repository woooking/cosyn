package com.github.woooking.cosyn.pattern.api

import better.files.File

trait DataSource[Data] {
    def data: Seq[Data]
}

object DataSource {
    private class JavaSourceCodeDirectory(dirs: List[File]) extends DataSource[File] {
        override def data: Seq[File] = dirs.flatMap(_.listRecursively
            .filter(_.isRegularFile)
            .filter(_.extension.contains(".java"))
            .toSeq)
    }

    def fromJavaSourceCodeDir(dirs: List[File]): DataSource[File] = new JavaSourceCodeDirectory(dirs)

    def fromJavaSourceCodeDir(dir: File): DataSource[File] = new JavaSourceCodeDirectory(List(dir))

    def fromJavaSourceCodeDir(dir: String): DataSource[File] = fromJavaSourceCodeDir(File(dir))
}
