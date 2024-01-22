package org.gorttar.test

import org.gorttar.data.heterogeneous.list.HCons
import org.gorttar.data.heterogeneous.list.HList
import org.gorttar.data.heterogeneous.list.HNil
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KCallable
import kotlin.text.Typography.nbsp

data class Row<out Data, out Expected>(val data: Data, val expected: Expected)

open class HeaderBuilder private constructor(private val cells: List<String>) {
    companion object : HeaderBuilder(emptyList()) {
        operator fun HeaderBuilder.get(next: String): HeaderBuilder = HeaderBuilder(cells + next)
        operator fun HeaderBuilder.div(expected: HeaderBuilder): Head = Row(cells, expected.cells)
    }
}

typealias NERow<Data, D, Expected, E> = Row<HCons<Data, D>, HCons<Expected, E>>
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

val hData: HeaderBuilder = HeaderBuilder
val hExpect: HeaderBuilder = hData
val data: HNil = HNil
val expect: HNil = HNil

operator fun <AL : HList<AL>, BL : HList<BL>> AL.div(bl: BL): Row<AL, BL> = Row(this, bl)

fun <Data, D, Expected, E> forAll(
    head: Head,
    vararg rows: NERow<Data, D, Expected, E>,
    test: (data: HCons<Data, D>, expected: HCons<Expected, E>) -> Unit
): Iterable<DynamicContainer>
        where Data : HList<Data>, Expected : HList<Expected> {
    validateTestData(head, rows)
    val (dataPadEnds, expectPadEnds) = (
            rows.asSequence()
                .map { it.data.rawList to it.expected.rawList } + (head.data to head.expected)
            )
        .map { (d, e) -> d.map { it.str.length } to e.map { it.str.length } }
        .fold(
            MutableList(head.data.size) { 0 } to MutableList(head.expected.size) { 0 }
        ) { acc, (dataLengths, expectLengths) ->
            acc.also { (dataPadEnds, expectPadEnds) ->
                fun List<Int>.updatePadEnds(padEnds: MutableList<Int>) = forEachIndexed { idx, len ->
                    padEnds[idx] = maxOf(padEnds[idx], len)
                }
                dataLengths.updatePadEnds(dataPadEnds)
                expectLengths.updatePadEnds(expectPadEnds)
            }
        }
    return listOf(
        dynamicContainer(
            "$nbsp$nbsp${head.showHead(dataPadEnds to expectPadEnds)}",
            rows
                .map {
                    dynamicTest(it.show(dataPadEnds to expectPadEnds)) { test(it.data, it.expected) }
                }
        )
    )
//    return (sequenceOf(dynamicTest(head.showHead(dataPadEnds to expectPadEnds)) {}) +
//            rows.asSequence()
//                .map { dynamicTest(it.show(dataPadEnds to expectPadEnds)) { test(it.data, it.expected) } }
//            )
//        .iterator()
}

private fun <D, Data, E, Expected> validateTestData(
    head: Head,
    rows: Array<out NERow<Data, D, Expected, E>>
) where Data : HList<Data>, Expected : HList<Expected> {
    val dataSize = head.data.size
    val expectSize = head.expected.size
    rows
        .filter { it.data.size != dataSize || it.expected.size != expectSize }
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
    .joinToString("|")

private fun NERow<*, *, *, *>.show(padEnds: Pair<List<Int>, List<Int>>): String =
    "${data.rawList.show(padEnds.first)}||${expected.rawList.show(padEnds.second)}"

private fun Head.showHead(padEnds: Pair<List<Int>, List<Int>>): String =
    "${data.show(padEnds.first)}||${expected.show(padEnds.second)}"

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
        val (data, hData) = alignNames(Names::data, Names::hData)
        val (expect, hExpect) = alignNames(Names::expect, Names::hExpect)
        Names.data = data
        Names.expect = expect
        Names.hData = hData
        Names.hExpect = hExpect
    }
}
