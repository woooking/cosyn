package com.github.woooking.cosyn.filter

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.javaparser.NodeDelegate

trait NodeFilter {
    def valid(node: Node): Boolean
}
