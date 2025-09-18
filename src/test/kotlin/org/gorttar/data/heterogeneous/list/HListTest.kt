package org.gorttar.data.heterogeneous.list

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotSameInstanceAs
import assertk.assertions.isSameInstanceAs
import org.gorttar.test.dynamicTests
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.math.BigDecimal
import java.math.BigInteger

class HListTest {
    @Test
    fun `equals and hashCode`(): Unit = assertAll {
        assertThat(xs26).isEqualTo(xs26Expected)
        assertThat(xs26.hashCode()).isEqualTo(xs26Expected.hashCode())
        assertThat(xs26).isNotSameInstanceAs(xs26Expected)
    }

    @TestFactory
    fun `string representation`() = HList::class.simpleName.let {
        data class Case(val xs: HList<*>, val expected: String)

        dynamicTests(
            Case(HNil, "$it[]"),
            Case(xs1, "$it[$a]"),
            Case(
                xs26,
                "$it[$a, $b, $c, $d, $e, $f, $g, $h, $i, $j, $k, $l, $m, $n, $o, $p, $q, $r, $s, $t, $u, $v, $w, $x, $y, $z]"
            )
        ) { assertAll { assertThat(xs.toString()).isEqualTo(expected) } }
    }

    @Test
    fun `copy altering head`(): Unit = assertThat(xs26.copy(head = HNil)).isEqualTo(HList1(HNil, z))

    @Test
    fun `copy altering tail`(): Unit = assertThat(xs26.copy(tail = Unit)).isEqualTo(HList26(xs25, Unit))

    @Test
    fun `copy the same`(): Unit = assertThat(xs26.copy()).isSameInstanceAs(xs26)

    @Test
    fun `rawList HNil`(): Unit = assertThat(HNil.rawList).isEmpty()

    @Test
    fun `rawList simple case`(): Unit = assertThat(xs2.rawList).containsExactly(a, b)

    @Test
    fun `rawList branch case`(): Unit = assertThat((xs1 + c).rawList).containsExactly(a, c)
}

private val xs26Expected: HList26<
        Byte, Short, Long, Int, Char, BigInteger, Double, Float, BigDecimal,
        String, List<Byte>, List<Short>, List<Long>, List<Int>, List<Char>, List<BigInteger>, List<Double>, List<Float>,
        List<BigDecimal>, List<String>, Set<Byte>, Set<Short>, Set<Long>, Set<Int>, Set<Char>, Set<BigInteger>
        > = HNil + a + b + c + d + e + f + g + h + i + j + k + l + m + n + o + p + q + r + s + t + u + v + w + x + y + z

