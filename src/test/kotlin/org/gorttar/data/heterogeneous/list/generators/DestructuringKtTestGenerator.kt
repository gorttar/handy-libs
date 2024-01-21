package org.gorttar.data.heterogeneous.list.generators

import org.junit.jupiter.api.Test

private const val testClassName = "DestructuringKtTest"
private val testKClass = Test::class

fun main(): Unit = generateDestructuringTest()

internal fun generateDestructuringTest(): Unit = writeTestSrc(
    testClassName,
    """
    |import assertk.assertAll
    |import assertk.assertThat
    |import assertk.assertions.isEqualTo
    |import ${testKClass.qualifiedName}
    |
    |// @formatter:off
    |
    |class $testClassName {
    |${(minPropName..maxPropName).joinToString("\n\n") { lastPropName ->
            val lastPropNumber = lastPropName.number
            """
            |    @${testKClass.simpleName}
            |    fun `xs$lastPropNumber destructuring`() = assertAll {
            |        val (${(minPropName..lastPropName).joinToString { "${it}1" }}) = xs$lastPropNumber
            |${(minPropName..lastPropName).joinToString("\n") { """|        assertThat(${it}1).isEqualTo($it)""" }}
            |    }
            """.trimMargin()
        }}
    |}
    |
    |// @formatter:on
    |
    |""".trimMargin()
)