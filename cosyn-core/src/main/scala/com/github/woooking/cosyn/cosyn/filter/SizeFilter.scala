package com.github.woooking.cosyn.cosyn.filter

import scala.util.Random

case class SizeFilter[T](size: Int, shuffle: Boolean = false) extends SeqFilter[T] {
    override def valid(data: Seq[T]): Seq[T] = (if (shuffle) new Random(101).shuffle(data) else data).take(size)
}
