package com.github.woooking.cosyn.filter
import better.files.File

class FileContentFilter(content: String) extends FileFilter {
    override def valid(file: File): Boolean = file.contentAsString.contains(content)
}
