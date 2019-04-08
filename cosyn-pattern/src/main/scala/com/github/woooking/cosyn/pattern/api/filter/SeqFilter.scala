package com.github.woooking.cosyn.pattern.api.filter

import com.github.woooking.cosyn.pattern.api.Pipe.Filter

/**
  * 一个SeqFilter是一个输入输出类型均为[[Seq]]的[[Filter]]
  * @tparam T SeqFilter的输入输出类型[[Seq]]的泛型参数
  */
trait SeqFilter[T] extends Filter[Seq[T]] {
    def valid(input: T): Boolean

    override final def >>:(input: Seq[T]): Seq[T] = input filter valid
}
