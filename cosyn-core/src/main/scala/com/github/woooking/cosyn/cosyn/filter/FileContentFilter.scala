package com.github.woooking.cosyn.cosyn.filter

import better.files.File

case class FileContentFilter(content: String) extends SingleFilter[File] {
    override def valid(file: File): Boolean = file.contentAsString.contains(content)
}
