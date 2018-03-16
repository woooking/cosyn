package com.github.woooking.cosyn.filter

import better.files.File

trait FileFilter {
    def valid(file: File): Boolean
}
