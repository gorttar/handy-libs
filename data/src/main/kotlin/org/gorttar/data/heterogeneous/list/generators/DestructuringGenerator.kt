package org.gorttar.data.heterogeneous.list.generators

import org.gorttar.data.heterogeneous.list.hListTypeName

fun main(): Unit = generateDestructuring()

internal fun generateDestructuring(): Unit = writeMainSrc(
    srcName = "Destructuring",
    content =
    """
    |// @formatter:off
    |
    |${
        (minPropName..maxPropName).joinToString("\n\n") { propName ->
            val typeName = propName.typeName
            val typesBefore = (minPropName until propName).map { "*" } + typeName
            val propNumber = propName.number
            val funName = "component$propNumber"
            """
            |/** $funName of [$hListTypeName$propNumber]..[$hListTypeName$maxPropNumber] */
            |@JvmName("$propName$propNumber")
            |inline operator fun <$typeName> $hListTypeName$propNumber<${typesBefore.joinToString()}>
            |        .$funName(): $typeName = $tailPropName
            |${
                (propName until maxPropName).joinToString("\n") {
                    val listArity = it.number + 1
                    val typesStr = (typesBefore + (propName..it).map { "*" }).joinToString()
                    """
                    |
                    |@JvmName("$propName$listArity")
                    |inline operator fun <$typeName> $hListTypeName$listArity<$typesStr>
                    |        .$funName(): $typeName = $headPropName.$funName()
                    """.trimMargin()
                }
            }""".trimMargin()
        }
    }
    |// @formatter:on
    |
    |""".trimMargin(),
    fileHeader = """
    |@file:Suppress("NOTHING_TO_INLINE")
    |
    """.trimMargin()
)
