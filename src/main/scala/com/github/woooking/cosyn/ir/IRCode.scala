package com.github.woooking.cosyn.ir

class IRCode {
    private [this] var tempIndex = 0

    def createTempVariable(): IRTemp = {
        val temp = IRTemp(tempIndex)
        tempIndex += 1
        temp
    }
}
