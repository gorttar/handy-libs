package org.gorttar.data.heterogeneous.list.generators

import org.gorttar.data.heterogeneous.list.hListTypeName

fun main(): Unit = generateAliases()

internal fun generateAliases(): Unit = writeMainSrc(
    srcName = "Aliases",
    content =
    """
    |// @formatter:off
    |
    |typealias ${hListTypeName}1<A> =
    |        $hConsTypeName<$hNilTypeName, A>
    |
    |${
        (minPropName + 1..maxPropName).joinToString("\n\n") {
            val lastType = it.typeName
            val prevTypes = (minPropName.typeName until lastType).joinToString()
            """
            |typealias $hListTypeName${it.number}<$prevTypes, $lastType> =
            |        $hConsTypeName<$hListTypeName${it.number - 1}<$prevTypes>, $lastType>
            """.trimMargin()
        }
    }
    |
    |// @formatter:on
    |
    |""".trimMargin()
)
