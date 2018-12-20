package com.github.woooking.cosyn.code.model.stmt

import com.github.woooking.cosyn.code.model.Node

abstract class Statement extends Node {
    def generateCode(indent: String): String = s"$indent$this"
}
