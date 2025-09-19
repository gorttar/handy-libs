package org.gorttar.output

fun main() {
    withIndentExample()
    withErrIndentExample()
    withIndentConsoleExample()
}

/**
 * prints
 * """
 * Entering withIndentExample
 *     Entering first withIndent
 *         Entering second withIndent
 *         Exiting second withIndent
 *     Exiting first withIndent
 * Exiting withIndentExample
 * """
 * to [System.out]
 */
private fun withIndentExample() {
    println("Entering withIndentExample")
    withIndent {
        println("Entering first withIndent")
        withIndent {
            println("Entering second withIndent")
            println("Exiting second withIndent")
        }
        println("Exiting first withIndent")
    }
    println("Exiting withIndentExample")
}

/**
 * prints
 * """
 * Entering withErrIndentExample
 *     Entering first withIndent
 *         Entering second withIndent
 *         Exiting second withIndent
 *     Exiting first withIndent
 * Exiting withErrIndentExample
 * """
 * to [System.err]
 */
private fun withErrIndentExample() {
    System.err.println("Entering withErrIndentExample")
    withErrIndent {
        System.err.println("Entering first withIndent")
        withErrIndent {
            System.err.println("Entering second withIndent")
            System.err.println("Exiting second withIndent")
        }
        System.err.println("Exiting first withIndent")
    }
    System.err.println("Exiting withErrIndentExample")
}

/**
 * prints
 * """
 * Entering withErrIndentExample
 *     Entering first withIndent
 *         Entering second withIndent
 *         Exiting second withIndent
 *     Exiting first withIndent
 * Exiting withErrIndentExample
 * """
 * to both [System.out] and [System.err]
 */
private fun withIndentConsoleExample() {
    println("Entering withIndentConsole")
    System.err.println("Entering withIndentConsole")
    withIndentConsole {
        println("Entering first withIndent")
        System.err.println("Entering first withIndent")
        withIndentConsole {
            println("Entering second withIndent")
            System.err.println("Entering second withIndent")
            println("Exiting second withIndent")
            System.err.println("Exiting second withIndent")
        }
        println("Exiting first withIndent")
        System.err.println("Exiting first withIndent")
    }
    println("Exiting withIndentConsole")
    System.err.println("Exiting withIndentConsole")
}