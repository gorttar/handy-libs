package org.gorttar.data.heterogeneous.list.generators

import org.gorttar.data.heterogeneous.list.hListTypeName

fun main(): Unit = generateDestructuring()

internal fun generateDestructuring(): Unit = writeMainSrc(
    srcName = "Destructuring",
    content = """
    |// @formatter:off
    |
    |${
        (minPropName..maxPropName).joinToString("\n\n") { propName ->
            val typeName = propName.typeName
            val typesBefore = (minPropName until propName).map { "*" } + typeName
            val propNumber = propName.number
            val funName = "component$propNumber"
            val propIndex = propNumber - 1
            """
            |/** $funName of [$hListTypeName$propNumber]..[$hListTypeName$maxPropNumber] */
            |@JvmName("$propName$propNumber")
            |inline operator fun <reified $typeName> $hListTypeName$propNumber<${typesBefore.joinToString()}>
            |        .$funName(): $typeName = $rawListPropName[$propIndex] as $typeName
            |${
                (propName ..< maxPropName).joinToString("\n") {
                    val listArity = it.number + 1
                    val typesStr = (typesBefore + (propName..it).map { "*" }).joinToString()
                    """
                    |
                    |@JvmName("$propName$listArity")
                    |inline operator fun <reified $typeName> $hListTypeName$listArity<$typesStr>
                    |        .$funName(): $typeName = $rawListPropName[$propIndex] as $typeName
                    """.trimMargin()
                }
            }""".trimMargin()
        }
    }
    |// @formatter:on
    |
    |""".trimMargin()
)
