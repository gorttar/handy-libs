package org.gorttar.test

import org.gorttar.data.heterogeneous.list.HCons
import org.gorttar.data.heterogeneous.list.HList
import org.gorttar.data.heterogeneous.list.HNil
import org.gorttar.data.heterogeneous.list.plus
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KCallable
import kotlin.text.Typography.nbsp

@JvmInline
value class Data<out D> private constructor(val d: D) {
    companion object {
        operator fun Data<List<String>>.get(next: String): Data<List<String>> = Data(d + next)
        operator fun <L : HList<L>, A> Data<L>.get(a: A): Data<HCons<L, A>> = Data(d + a)
        val hData: Data<List<String>> = Data(emptyList())
        val data: Data<HNil> = Data(HNil)
    }
}


@JvmInline
value class Expected<out E> private constructor(val e: E) {
    companion object {
        operator fun Expected<List<String>>.get(next: String): Expected<List<String>> = Expected(e + next)
        operator fun <L : HList<L>, A> Expected<L>.get(a: A): Expected<HCons<L, A>> = Expected(e + a)
        val hExpect: Expected<List<String>> = Expected(emptyList())
        val expect: Expected<HNil> = Expected(HNil)
    }
}


class Row<out D, out E> private constructor(val data: Data<D>, val expected: Expected<E>) {
    companion object {
        @JvmName("hDiv")
        operator fun Data<List<String>>.div(expected: Expected<List<String>>): Head = Row(this, expected)
        operator fun <AL : HList<AL>, BL : HList<BL>> Data<AL>.div(expected: Expected<BL>): Row<AL, BL> =
            Row(this, expected)
    }
}

typealias NERow<DL, D, EL, E> = Row<HCons<DL, D>, HCons<EL, E>>
typealias Head = Row<List<String>, List<String>>

val Any?.str: String
    get() = when (this) {
        is String -> "\"$this\""
        is BigDecimal -> "$this (BD)"
        is Float -> "$this (f)"
        is BigInteger -> "$this (BI)"
        is Long -> "${this}L"
        is Short -> "$this (s)"
        is Byte -> "$this (b)"
        else -> "$this"
    }

inline fun <DL, D, EL, E> forAll(
    head: Head,
    vararg rows: NERow<DL, D, EL, E>,
    crossinline test: (data: HCons<DL, D>, expected: HCons<EL, E>) -> Unit
): Iterable<DynamicContainer>
        where DL : HList<DL>, EL : HList<EL> {
    validateTestData(head, rows)
    val (dataPadEnds, expectPadEnds) = (
            rows.asSequence()
                .map { it.data.d.rawList to it.expected.e.rawList } + (head.data.d to head.expected.e)
            )
        .map { (d, e) -> d.map { it.str.length } to e.map { it.str.length } }
        .fold(
            MutableList(head.data.d.size) { 0 } to MutableList(head.expected.e.size) { 0 }
        ) { acc, (dataLengths, expectLengths) ->
            acc.also { (dataPadEnds, expectPadEnds) ->
                dataLengths.updatePadEnds(dataPadEnds)
                expectLengths.updatePadEnds(expectPadEnds)
            }
        }
    return listOf(
        dynamicContainer(
            "$nbsp$nbsp${head.showHead(dataPadEnds to expectPadEnds)}",
            rows
                .map {
                    dynamicTest(it.show(dataPadEnds to expectPadEnds)) { test(it.data.d, it.expected.e) }
                }
        )
    )
}

inline fun <DL, D, EL, E> forAll(
    crossinline test: (data: HCons<DL, D>, expected: HCons<EL, E>) -> Unit,
    head: Head,
    vararg rows: NERow<DL, D, EL, E>
): Iterable<DynamicContainer>
        where DL : HList<DL>, EL : HList<EL> = forAll(head, rows = rows, test)

@PublishedApi
internal fun List<Int>.updatePadEnds(padEnds: MutableList<Int>) = forEachIndexed { idx, len ->
    padEnds[idx] = maxOf(padEnds[idx], len)
}

@PublishedApi
internal fun <D, Data, E, Expected> validateTestData(
    head: Head,
    rows: Array<out NERow<Data, D, Expected, E>>
) where Data : HList<Data>, Expected : HList<Expected> {
    val dataSize = head.data.d.size
    val expectSize = head.expected.e.size
    rows
        .filter { it.data.d.size != dataSize || it.expected.e.size != expectSize }
        .takeUnless { it.isEmpty() }
        ?.let { failures ->
            error(
                """
                Rows ${Names.data} size should be $dataSize, ${Names.expect} should be $expectSize
                The following rows have invalid size:
                ${
                    failures.joinToString(
                        prefix = "    ",
                        separator = "\n    "
                    ) { it.show(Pair(emptyList(), emptyList())) }
                }
                """.trimIndent()
            )
        }
}

private fun List<*>.show(padEnds: List<Int>): String = asSequence()
    .zip(padEnds.asSequence() + generateSequence { 0 }) { a, padEnd -> a.str.padEnd(padEnd, nbsp) }
    .joinToString(" | ")

@PublishedApi
internal fun NERow<*, *, *, *>.show(padEnds: Pair<List<Int>, List<Int>>): String =
    "${data.d.rawList.show(padEnds.first)} || ${expected.e.rawList.show(padEnds.second)}"

@PublishedApi
internal fun Head.showHead(padEnds: Pair<List<Int>, List<Int>>): String =
    "${data.d.show(padEnds.first)} || ${expected.e.show(padEnds.second)}"

private fun alignNames(first: KCallable<*>, second: KCallable<*>): Pair<String, String> = (first.name to second.name)
    .let { (f, s) ->
        val maxLength = maxOf(f.length, s.length)
        f.padEnd(maxLength, nbsp) to s.padEnd(maxLength, nbsp)
    }

private object Names {
    val data: String
    val expect: String
    val hData: String
    val hExpect: String

    init {
        val (data, hData) = alignNames(::data, ::hData)
        val (expect, hExpect) = alignNames(::expect, ::hExpect)
        this.data = data
        this.expect = expect
        this.hData = hData
        this.hExpect = hExpect
    }
}
