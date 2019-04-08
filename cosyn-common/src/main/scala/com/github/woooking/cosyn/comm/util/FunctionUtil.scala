package com.github.woooking.cosyn.comm.util

import cats.Monoid

object FunctionUtil {
    def sum[A: Monoid](as: List[A]): A = as.foldLeft(Monoid[A].empty)(Monoid[A].combine)
}
