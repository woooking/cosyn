package com.github.woooking.cosyn.dfg

import de.parsemis.parsers.LabelParser

trait DFGEdge {

}

object DFGEdge {
    val singleton = new DFGEdge {}

    val parser = new LabelParser[DFGEdge] {
        override def serialize(labelType: DFGEdge): String = ""

        override def parse(s: String): DFGEdge = singleton
    }
}