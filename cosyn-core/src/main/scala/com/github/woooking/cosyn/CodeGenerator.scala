package com.github.woooking.cosyn

import de.parsemis.miner.general.Fragment

trait CodeGenerator[N, E, Graph] {
    def generate(originalGraph: Seq[Graph])(fragments: Fragment[N, E]): String
}

