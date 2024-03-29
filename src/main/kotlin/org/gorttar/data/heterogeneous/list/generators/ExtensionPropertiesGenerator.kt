package org.gorttar.data.heterogeneous.list.generators

import org.gorttar.data.heterogeneous.list.hListTypeName

fun main(): Unit = generateExtensionProperties()

internal fun generateExtensionProperties(): Unit = writeMainSrc(
    srcName = "ExtensionProperties",
    content =
    """
    |// @formatter:off
    |
    |${
        (minPropName..maxPropName).joinToString("\n\n") { propName ->
            val typeName = propName.typeName
            val typesBefore = (minPropName until propName).map { "*" } + typeName
            """
            |/** ${propName.ordinalStr} value of [$hListTypeName${propName.number}]..[$hListTypeName$maxPropNumber] */
            |${
                """
                |inline val <$typeName> $hListTypeName${propName.number}<${typesBefore.joinToString<Any>()}>.$propName: $typeName
                |    @JvmName("$propName${propName.number}") get() = $tailPropName
                """.trimMargin()
            }
            |${
                (propName until maxPropName).joinToString("\n") {
                    val listArity = it.number + 1
                    val typesStr = (typesBefore + (propName..it).map { "*" }).joinToString()
                    """
                    |inline val <$typeName> $hListTypeName$listArity<$typesStr>.$propName: $typeName
                    |    @JvmName("$propName$listArity") get() = $headPropName.$propName
                    """.trimMargin()
                }
            }""".trimMargin()
        }
    }
    |// @formatter:on
    |
    |""".trimMargin()
)
