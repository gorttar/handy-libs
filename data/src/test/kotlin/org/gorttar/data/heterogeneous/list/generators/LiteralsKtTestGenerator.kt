package org.gorttar.data.heterogeneous.list.generators

import org.junit.jupiter.api.Test

private const val testClassName = "LiteralsKtTest"
private val testKClass = Test::class

fun main(): Unit = generateLiteralsTest()

internal fun generateLiteralsTest(): Unit = writeTestSrc(
    testClassName,
    """
    |import assertk.assertThat
    |import assertk.assertions.isEqualTo
    |import assertk.assertions.isSameInstanceAs
    |import ${testKClass.qualifiedName}
    |
    |// @formatter:off
    |
    |class $testClassName {
    |    @${testKClass.simpleName}
    |    fun `0 args literal`() = assertThat(
    |        hListOf()
    |    ).isSameInstanceAs(xs0)
    |
    |${
            (minPropName..maxPropName).joinToString("\n\n") { lastPropName ->
                val lastPropNumber = lastPropName.number
                """
                |    @${testKClass.simpleName}
                |    fun `$lastPropNumber  args literal`() = assertThat(
                |        hListOf(${(minPropName..lastPropName).joinToString()})
                |    ).isEqualTo(xs$lastPropNumber)
                """.trimMargin()
            }
        }
    |}
    |
    |// @formatter:on
    |
    |""".trimMargin()
)