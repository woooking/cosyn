package com.github.woooking.cosyn.pattern.filter

import de.parsemis.miner.general.Fragment

trait FragmentFilter[N, E] {
    def valid(fragment: Fragment[N, E]): Boolean
}
