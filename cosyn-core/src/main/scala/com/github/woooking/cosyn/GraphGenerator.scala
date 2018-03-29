package com.github.woooking.cosyn

trait GraphGenerator[Data, Graph] {
    def generate(data: Seq[Data]): Seq[Graph]
}

