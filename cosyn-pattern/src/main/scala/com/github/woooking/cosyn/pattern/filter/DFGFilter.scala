package com.github.woooking.cosyn.pattern.filter

import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.SimpleDFG

trait DFGFilter {
    def valid(dfg: SimpleDFG): Boolean
}
