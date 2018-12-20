package com.github.woooking.cosyn.pattern.api

trait Pipeline[In, Out] {
    def >>:(input: In): Out

    def |[Out2](other: Pipeline[Out, Out2]): Pipeline[In, Out2] = (input: In) => (input >>: this) >>: other
}

object Pipeline {
    def id[T]: Filter[T] = identity[T]

    type Filter[T] = Pipeline[T, T]
}
