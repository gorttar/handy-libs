package org.gorttar.output

import org.gorttar.test.io.runIOTest
import org.junit.jupiter.api.Test
import java.io.PrintStream
import kotlin.reflect.KProperty0

internal class IndentKtTest {
    @Test
    fun withIndentTest() = runIOTest({
        expectLine(expected)
        expectErrLine(expectedNoIndent)
    }) {
        testScenario(::withIndent, System::out)
        testScenario(::withIndent, System::err)
    }

    @Test
    fun withErrIndentTest() = runIOTest({
        expectLine(expectedNoIndent)
        expectErrLine(expected)
    }) {
        testScenario(::withErrIndent, System::out)
        testScenario(::withErrIndent, System::err)
    }

    @Test
    fun withIndentConsoleTest() = runIOTest({
        expectLine(expected)
        expectErrLine(expected)
    }) {
        testScenario(::withIndentConsole, System::out)
        testScenario(::withIndentConsole, System::err)
    }
}

private const val indent = "$defaultIndent$defaultIndent"
private val expected = """
    |Entering test
    |${indent}Entering first withIndent
    |$indent${indent}Entering second withIndent
    |$indent$indent${indent}Entering third withIndent
    |$indent$indent${indent}Multiline into third indent line 1
    |$indent$indent${indent}Multiline into third indent line 2
    |$indent$indent${indent}null
    |$indent$indent${indent}Multiline into third indent line 3
    |$indent$indent${indent}Multiline into third indent line 4
    |$indent$indent${indent}Exiting third withIndent
    |$indent${indent}Exiting second withIndent
    |${indent}Exiting first withIndent
    |Exiting test
    """.trimMargin()
private val expectedNoIndent = expected.lineSequence().map(String::trimStart).joinToString("\n")

private fun testScenario(
    withIndent: (indent: String, () -> Unit) -> Unit,
    printStreamGetter: KProperty0<PrintStream>
) = printStreamGetter.run {
    println("Entering test")
    withIndent(indent) {
        println("Entering first withIndent")
        withIndent(indent) {
            println("Entering second withIndent")
            withIndent(indent) {
                println("Entering third withIndent")
                println(
                    """
                        |Multiline into third indent line 1
                        |Multiline into third indent line 2
                        """.trimMargin()
                )
                println(null)
                print(
                    """
                        |Multiline into third indent line 3
                        |Multiline into third indent line 4
                        |
                        """.trimMargin()
                )
                println("Exiting third withIndent")
            }
            println("Exiting second withIndent")
        }
        println("Exiting first withIndent")
    }
    println("Exiting test")
}

private val KProperty0<PrintStream>.println: (String?) -> Unit get() = get()::println
private val KProperty0<PrintStream>.print: (String?) -> Unit get() = get()::print