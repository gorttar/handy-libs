package org.gorttar.data

private val nil = Any()

infix fun Sequence<*>.eq(ySequence: Sequence<*>): Boolean = listOf(this, ySequence)
    .map { it + generateSequence { nil } }
    .let { (xs, ys) -> xs zip ys }
    .takeWhile { it != Pair(nil, nil) }
    .all { (x, y) -> x == y }
