package org.gorttar.control

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.gorttar.data.heterogeneous.list.component1
import org.gorttar.data.heterogeneous.list.component2
import org.gorttar.data.heterogeneous.list.hListOf
import org.junit.jupiter.api.Test

private const val initialX = 0
private const val changedX = 1 + initialX
private const val changedX2 = 2 + initialX

private const val initialY = "y"
private const val changedY = "y$initialY"

class ManagedValueTest {
    private var x = initialX
    private var y = initialY

    @Test
    fun `ManagedValue members tests`() {
        fun ManagedValue<Int>.check() {
            x = initialX
            assertThat(get()).isEqualTo(initialX)
            set(changedX)
            assertThat(get()).isEqualTo(changedX)
            assertThat(x).isEqualTo(changedX)
        }

        managed({ x }) { x = it }.check()
        managed(::x).check()
    }

    @Test
    fun `test 'on' extension`() {
        managed(::x).on(changedX) { old1 ->
            assertThat(old1).isEqualTo(initialX)
            assertThat(x).isEqualTo(changedX)

            managed(::x).on(changedX2) { old2 ->
                assertThat(old2).isEqualTo(changedX)
                assertThat(x).isEqualTo(changedX2)
            }

            assertThat(x).isEqualTo(changedX)
        }
        assertThat(x).isEqualTo(initialX)
    }

    @Test
    fun `test 'onTransform' extension`() {
        managed(::x).onTransform({ changedX }) { old1, new1 ->
            assertThat(old1).isEqualTo(initialX)
            assertThat(x).isEqualTo(changedX)
            assertThat(new1).isEqualTo(changedX)

            managed(::x).onTransform({ changedX2 }) { old2, new2 ->
                assertThat(old2).isEqualTo(changedX)
                assertThat(x).isEqualTo(changedX2)
                assertThat(new2).isEqualTo(changedX2)
            }

            assertThat(x).isEqualTo(changedX)
        }
        assertThat(x).isEqualTo(initialX)
    }

    @Test
    fun `test 'coManaged' extension`() {
        coManaged(::x).coManaged(::y).on(hListOf(changedX, changedY)) { (oldX, oldY) ->
            assertThat(oldX).isEqualTo(initialX)
            assertThat(oldY).isEqualTo(initialY)
            assertThat(x).isEqualTo(changedX)
            assertThat(y).isEqualTo(changedY)
        }
        assertThat(x).isEqualTo(initialX)
        assertThat(y).isEqualTo(initialY)
    }
}