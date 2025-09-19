package org.gorttar.data.heterogeneous.list.generators

import org.gorttar.data.heterogeneous.list.hListTypeName
import org.junit.jupiter.api.TestFactory
import kotlin.text.Typography.dollar

private const val testClassName = "ExtensionPropertiesKtTest"
private val testFactoryKClass = TestFactory::class

fun main(): Unit = generateExtensionPropertiesTest()

internal fun generateExtensionPropertiesTest(): Unit = writeTestSrc(
    testClassName,
    """
    |import assertk.assertThat
    |import assertk.assertions.isEqualTo
    |import org.gorttar.test.dynamicTests
    |import ${testFactoryKClass.qualifiedName}
    |
    |// @formatter:off
    |
    |class $testClassName {
    |${
            (minPropName..maxPropName).joinToString("\n\n") { propName ->
                """
                |    @${testFactoryKClass.simpleName}
                |    fun $propName() = dynamicTests(
                |${
                        (propName.number..maxPropNumber).asSequence().map {
                            "Case(xs$it, xs$it.$propName)"
                        }.chunked(5) { it.joinToString() }.joinToString(",\n") { "        $it" }
                    }
                |    ) { assertThat(actual, "${propName.ordinalStr} value in ${dollar}xs").isEqualTo($propName) }
                """.trimMargin()
            }
        }
    |
    |    private data class Case<XS : $hListTypeName<XS>, A>(val xs: XS, val actual: A)
    |}
    |
    |// @formatter:on
    |
    |""".trimMargin()
)
