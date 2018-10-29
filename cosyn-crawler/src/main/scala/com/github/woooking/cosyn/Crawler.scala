package com.github.woooking.cosyn

import org.kohsuke.github.GitHub

object Crawler {
    def main(args: Array[String]): Unit = {
        val github = GitHub.connect
        val result = github.searchContent()
            .q("")
            .language("java")
            .list()
    }
}
