package com.github.woooking.cosyn

case class CosynConfig(debug: Boolean = false)

object CosynConfig {
    val global = CosynConfig(
        debug = true
    )
}


