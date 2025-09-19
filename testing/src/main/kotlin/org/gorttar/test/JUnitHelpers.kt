package org.gorttar.test

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest

fun <Case> dynamicTests(
    vararg cases: Case,
    assertion: Case.() -> Unit
): Iterator<DynamicTest> = cases.asSequence().dynamicTests(assertion)

fun <Case> Sequence<Case>.dynamicTests(assertion: Case.() -> Unit): Iterator<DynamicTest> =
    map { dynamicTest(it.toString()) { assertion(it) } }.iterator()
