package com.github.woooking.cosyn.code.model

abstract class Node {
    var parent: Node = _

    def children: Seq[Node]
}

