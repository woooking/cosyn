package com.github.woooking.cosyn.middleware
import better.files.File

class FileContentFilter(content: String) extends FileFilter {
    override def valid(file: File): Boolean = file.contentAsString.contains(content)
}
