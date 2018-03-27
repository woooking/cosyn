package com.github.woooking.cosyn.filter

import com.github.woooking.cosyn.dfgprocessor.dfg.DFG

trait DFGFilter {
    def valid(dfg: DFG): Boolean
}
