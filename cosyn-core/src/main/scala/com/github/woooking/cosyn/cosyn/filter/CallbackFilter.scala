package com.github.woooking.cosyn.cosyn.filter

case class CallbackFilter[T](callback: Seq[T] => Unit) extends SeqFilter[T] {
    override def filter(data: Seq[T]): Seq[T] = {
        callback(data)
        data
    }
}
