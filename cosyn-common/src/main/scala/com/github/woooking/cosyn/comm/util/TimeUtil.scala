package com.github.woooking.cosyn.comm.util

import scala.collection.mutable

object TimeUtil {
    private val count = mutable.Map[String, Int]()
    private val time = mutable.Map[String, Double]()

    def profile[V](name: String)(body: => V): V = {
        val start = System.currentTimeMillis().toDouble
        val result = body
        val end = System.currentTimeMillis().toDouble
        val newCount = count.getOrElse(name, 0) + 1
        val newTime = time.getOrElse(name, 0.0) + (end - start) / 1000.0
        count(name) = newCount
        time(name) = newTime
        result
    }

    def print(): Unit = {
        count.foreach {
            case (name, c) =>
                val t = time(name)
                println(s"Function $name called $c times, ave cost ${t / c}s.")
        }
    }
}
