package com.github.woooking.cosyn.util

class IDGenerator {
    private[this] var stream = Stream.from(0)

    def next(): Int = {
        synchronized(stream) {
            val id = stream.head
            stream = stream.tail
            id
        }
    }
}
