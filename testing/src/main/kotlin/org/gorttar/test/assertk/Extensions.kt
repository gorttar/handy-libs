package org.gorttar.test.assertk

import assertk.Assert
import assertk.assertions.support.expected
import assertk.assertions.support.show
import assertk.assertions.isFailure as isFailureK

inline fun <reified T : Any> Assert<Any?>.isInstanceOf(): Assert<T> = transform {
    it as? T ?: expected("to be instance of:${show(T::class)} but had class:${it?.let { show(it::class) }}")
}

inline fun <reified T : Throwable> Assert<Result<Any?>>.isFailure(): Assert<T> = isFailureK().isInstanceOf()
