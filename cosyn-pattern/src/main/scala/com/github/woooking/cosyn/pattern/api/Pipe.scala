package com.github.woooking.cosyn.pattern.api

import cats.Monoid

/**
  * 管道抽象接口。
  *
  * @tparam In 管道输入的类型
  * @tparam Out 管道输出的类型
  */
trait Pipe[In, Out] {
    def >>:(input: In): Out

    /**
      * 管道之间可通过该函数组装成一个新的管道。
      * @param other 接在该该管道之后的管道
      * @tparam Out2 <code>other</code>的输出类型
      * @return 组装之后的管道
      */
    final def |[Out2](other: Pipe[Out, Out2]): Pipe[In, Out2] = (input: In) => (input >>: this) >>: other
}

object Pipe {
    def id[T]: Filter[T] = identity[T]

    /**
      * 一个Filter是一个输入类型和输出类型相同的[[Pipe]]。
      * @tparam T 过滤器的输入和输出类型
      */
    type Filter[T] = Pipe[T, T]

    implicit def filterCombineMonoid[T]: Monoid[Filter[T]] = new Monoid[Filter[T]] {
        def empty: Filter[T] = id
        def combine(x: Filter[T], y: Filter[T]): Filter[T] = x | y
    }
}
