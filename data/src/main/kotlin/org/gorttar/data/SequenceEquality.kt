package org.gorttar.data

private val NULL = Any()

infix fun Sequence<*>.eq(ySequence: Sequence<*>): Boolean = listOf(this, ySequence)
    .map { it + generateSequence { NULL } }
    .let { (xs, ys) -> xs zip ys }
    .takeWhile { it != Pair(NULL, NULL) }
    .all { (x, y) -> x == y }
