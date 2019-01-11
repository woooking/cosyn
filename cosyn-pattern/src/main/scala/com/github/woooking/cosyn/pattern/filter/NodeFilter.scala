package com.github.woooking.cosyn.pattern.filter

import com.github.javaparser.ast.Node

trait NodeFilter {
    def valid(node: Node): Boolean
}
