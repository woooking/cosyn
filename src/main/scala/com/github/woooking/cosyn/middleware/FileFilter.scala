package com.github.woooking.cosyn.middleware

import better.files.File

trait FileFilter {
    def valid(file: File): Boolean
}
