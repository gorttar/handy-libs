package org.gorttar.data.heterogeneous.list.generators

import org.gorttar.data.heterogeneous.list.hListTypeName
import kotlin.math.ceil

private val hListOfFunName = "${hListTypeName.replaceFirstChar { it.lowercase() }}Of"

fun main(): Unit = generateLiterals()

internal fun generateLiterals(): Unit = writeMainSrc(
    srcName = "Literals",
    content =
    """
    |// @formatter:off
    |
    |fun $hListOfFunName(): $hNilTypeName = $hNilTypeName
    |${
        (minPropName..maxPropName).joinToString("\n") { lastPropName ->
            val propNames = minPropName..lastPropName
            val types = propNames.joinToString { "${it.typeName}" }
            """
            |
            |fun <$types> $hListOfFunName(
            |    ${
                propNames.chunked(ceil(lastPropName.number / 2.0).toInt()).joinToString(",\n|    ") { chunk ->
                    chunk.joinToString { "$it: ${it.typeName}" }
                }
            }
            |): $hListTypeName${lastPropName.number}<$types> =
            |    $hListOfFunName(${(minPropName until lastPropName).joinToString()}) + $lastPropName
            """.trimMargin()
        }
    }
    |
    |// @formatter:on
    |
    |""".trimMargin()
)