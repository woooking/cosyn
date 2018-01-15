package com.github.woooking.cosyn.javaparser

import com.github.javaparser.ast.Node

trait NodeDelegate[T <: Node] {
    val delegate: T
}

