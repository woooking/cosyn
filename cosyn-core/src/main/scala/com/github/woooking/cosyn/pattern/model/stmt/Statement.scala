package com.github.woooking.cosyn.pattern.model.stmt

import com.github.woooking.cosyn.pattern.model.Node

abstract class Statement extends Node {
    def generateCode(indent: String): String = s"$indent$this"
}
