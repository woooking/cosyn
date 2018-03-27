package com.github.woooking.cosyn.filter
import better.files.File

class SourceContentFilter(content: String) extends SourceFilter[File] {
    override def valid(file: File): Boolean = file.contentAsString.contains(content)
}
