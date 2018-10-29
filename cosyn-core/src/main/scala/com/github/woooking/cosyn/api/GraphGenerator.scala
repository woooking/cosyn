package com.github.woooking.cosyn.api

trait GraphGenerator[Data, Graph] {
    def generate(data: Seq[Data]): Seq[Graph]
}
