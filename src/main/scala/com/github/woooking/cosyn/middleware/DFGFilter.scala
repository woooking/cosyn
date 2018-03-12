package com.github.woooking.cosyn.middleware

import com.github.woooking.cosyn.dfg.DFG

trait DFGFilter {
    def valid(dfg: DFG): Boolean
}
