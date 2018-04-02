package com.github.woooking.cosyn.filter

import com.github.woooking.cosyn.dfgprocessor.dfg.SimpleDFG

trait DFGFilter {
    def valid(dfg: SimpleDFG): Boolean
}
