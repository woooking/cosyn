package com.github.woooking.cosyn.api

import better.files.File
import com.github.woooking.cosyn.cosyn.Element

trait DataSource[Data] extends Element[Unit, Seq[Data]] {
    def data: Seq[Data]
}

object DataSource {
    private class JavaSourceCodeDirectory(dirs: List[File]) extends DataSource[File] {
        override def data: Seq[File] = dirs.flatMap(_.listRecursively
            .filter(_.isRegularFile)
            .filter(_.extension.contains(".java"))
            .toSeq)

        override def process(input: Unit): Seq[File] = data
    }

    def fromJavaSourceCodeDir(dirs: List[File]): DataSource[File] = new JavaSourceCodeDirectory(dirs)

    def fromJavaSourceCodeDir(dir: File): DataSource[File] = new JavaSourceCodeDirectory(List(dir))

    def fromJavaSourceCodeDir(dir: String): DataSource[File] = fromJavaSourceCodeDir(File(dir))
}
