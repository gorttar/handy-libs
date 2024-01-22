package org.gorttar.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.gorttar.data.heterogeneous.list.component1
import org.gorttar.data.heterogeneous.list.component2
import org.gorttar.data.heterogeneous.list.component3
import org.gorttar.data.heterogeneous.list.get
import org.gorttar.test.HeaderBuilder.Companion.div
import org.gorttar.test.HeaderBuilder.Companion.get
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.TestFactory

class ForAllKtTest {
    @TestFactory
    fun forAll(): Iterable<DynamicContainer> = forAll(
        // @formatter:off
        hData["first"]["second"        ]["third"         ] / hExpect["firstExpected"]["secondExpected"],
        data ["1"    ][2.toBigInteger()][3.toBigDecimal()] / expect ["4"            ][5               ],
        data ["6"    ][7.toBigInteger()][8.toBigDecimal()] / expect ["9"            ][10              ],
        // @formatter:on
    ) { (first, second, third), (firstExpected, secondExpected) ->
        assertThat(firstExpected.toInt() - first.toInt()).isEqualTo(3)
        assertThat(secondExpected - second.toInt()).isEqualTo(3)
        assertThat(secondExpected - third.toInt()).isEqualTo(2)
    }
}
