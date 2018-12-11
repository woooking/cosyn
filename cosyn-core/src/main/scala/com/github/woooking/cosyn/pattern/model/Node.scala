package com.github.woooking.cosyn.pattern.model

abstract class Node {
    var parent: Node = _

    def children: Seq[Node]
}

