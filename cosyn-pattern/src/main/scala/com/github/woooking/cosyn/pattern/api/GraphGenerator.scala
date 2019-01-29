package com.github.woooking.cosyn.pattern.api

trait GraphGenerator[Data, Graph] {
    def generate(data: Data): Seq[Graph]
}
