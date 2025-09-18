package org.gorttar.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.gorttar.data.heterogeneous.list.component1
import org.gorttar.data.heterogeneous.list.component2
import org.gorttar.data.heterogeneous.list.component3
import org.gorttar.test.Data.Companion.data
import org.gorttar.test.Data.Companion.get
import org.gorttar.test.Data.Companion.hData
import org.gorttar.test.Expected.Companion.expect
import org.gorttar.test.Expected.Companion.get
import org.gorttar.test.Expected.Companion.hExpect
import org.gorttar.test.Row.Companion.div
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.TestFactory
import java.math.BigDecimal
import java.math.BigInteger

inline val Int.bi: BigInteger get() = toBigInteger()
inline val Int.bd: BigDecimal get() = toBigDecimal()

class ForAllKtTest {
    @TestFactory
    fun `forAll test before table`(): Iterable<DynamicContainer> = forAll(
        { (first, second, third), (firstExpected, secondExpected) ->
            assertThat(firstExpected.toInt() - first.toInt()).isEqualTo(3)
            assertThat(secondExpected - second.toInt()).isEqualTo(3)
            assertThat(secondExpected - third.toInt()).isEqualTo(2)
        },
        // @formatter:off
        hData["first"]["second"]["third"] / hExpect["firstExpected"]["secondExpected"],
        data ["1"    ][2.bi    ][3.bd   ] / expect ["4"            ][5               ],
        data ["6"    ][7.bi    ][8.bd   ] / expect ["9"            ][10              ],
        // @formatter:on
    )

    @TestFactory
    fun `forAll test after table`(): Iterable<DynamicContainer> = forAll(
        // @formatter:off
        hData["first"]["second"]["third"] / hExpect["firstExpected"]["secondExpected"],
        data ["11"   ][12.bi   ][13.bd  ] / expect ["14"           ][15              ],
        data ["16"   ][17.bi   ][18.bd  ] / expect ["19"           ][20              ],
        // @formatter:on
    ) { (first, second, third), (firstExpected, secondExpected) ->
        assertThat(firstExpected.toInt() - first.toInt()).isEqualTo(3)
        assertThat(secondExpected - second.toInt()).isEqualTo(3)
        assertThat(secondExpected - third.toInt()).isEqualTo(2)
    }
}
