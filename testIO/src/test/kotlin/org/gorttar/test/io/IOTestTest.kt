package org.gorttar.test.io

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError
import java.time.Duration

class IOTestTest {
    @Test
    fun recordIO() {
        val actual = recordIO("hello\n") {
            println("Start")
            val readLine = readLine()
            println("Read: $readLine")
            System.err.println("ErrRead: $readLine")
            println("End")
            System.err.print("ErrEnd")
            System.err.println()
        }
        assertThat(actual).containsExactly(
            OUT("Start$cr"),
            IN("hello$cr"),
            OUT("Read: hello$cr"),
            ERR("ErrRead: hello$cr"),
            OUT("End$cr"),
            ERR("ErrEnd$cr")
        )
    }

    @Test
    fun `recordIO with timeout`() {
        val actual = recordIO(
            "hello\n",
            Duration.ofMillis(500)
        ) {
            println("Start")
            val readLine = readLine()
            println("Read: $readLine")
            System.err.println("ErrRead: $readLine")
            Thread.sleep(1000)
            println("End")
            System.err.print("ErrEnd")
            System.err.println()
        }
        assertThat(actual.dropLast(1)).isEqualTo(
            listOf(
                OUT("Start$cr"),
                IN("hello$cr"),
                OUT("Read: hello$cr"),
                ERR("ErrRead: hello$cr")
            )
        )
        assertThat(actual.last()).isInstanceOf(TIMEOUT::class).given {
            assertThat(it.t).isInstanceOf(AssertionFailedError::class)
        }
    }

    @Test
    fun `runIOTest positive case`() {
        runIOTest({
            expectLine("Start")
            sendLine("errHello")
            expectErrLine("ErrRead: errHello")
            sendLine("hello")
            expectLine("Read: hello")
            expectErrString("ErrEnd\n")
            expectString(
                """
                |End
                |
                """.trimMargin()
            )
        }) {
            println("Start")
            System.err.println("ErrRead: ${readLine()}")
            println("Read: ${readLine()}")
            System.err.println("ErrEnd")
            println("End")
        }
    }
}
