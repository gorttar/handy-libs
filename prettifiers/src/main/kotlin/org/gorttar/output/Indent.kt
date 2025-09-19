package org.gorttar.output

import org.gorttar.control.coManaged
import org.gorttar.control.managed
import org.gorttar.control.onTransform
import org.gorttar.data.heterogeneous.list.component1
import org.gorttar.data.heterogeneous.list.component2
import org.gorttar.data.heterogeneous.list.hListOf
import java.io.PrintStream
import java.util.concurrent.atomic.AtomicBoolean

/** API section */

/**
 * Temporarily overrides [System.out] with [IndentedPrintStream]
 * which prepends [indent] to each line passed from [block] to [System.out] [print] or [println]
 * @see withIndentExample and tests for details
 */
inline fun <T> withIndent(
    indent: String = defaultIndent,
    block: () -> T
): T = managed(System::out, System::setOut).onTransform({ IndentedPrintStream(indent, it) }) { _, _ -> block() }

/**
 * Temporarily overrides [System.err] with [IndentedPrintStream]
 * which prepends [indent] to each line passed from [block] to [System.err] [print] or [println]
 * @see withErrIndentExample and tests for details
 */
inline fun <T> withErrIndent(
    indent: String = defaultIndent,
    block: () -> T
): T = managed(System::err, System::setErr).onTransform({ IndentedPrintStream(indent, it) }) { _, _ -> block() }

/**
 * Temporarily overrides [System.out] and [System.err] with [IndentedPrintStream]
 * which prepends [indent] to each line passed from [block] to [System.out] or [System.err] [print] or [println]
 * @see withIndentConsoleExample and tests for details
 */
inline fun <T> withIndentConsole(
    indent: String = defaultIndent,
    block: () -> T
): T = coManaged(System::out, System::setOut).coManaged(System::err, System::setErr).onTransform({ (out, err) ->
    hListOf(IndentedPrintStream(indent, out), IndentedPrintStream(indent, err))
}) { _, _ -> block() }

/** implementation section */

@PublishedApi
internal class IndentedPrintStream(
    val indent: String,
    private val delegate: PrintStream
) : PrintStream(delegate, true) {
    private val shouldIndent = AtomicBoolean(true)

    override fun print(s: String?) = s?.split("\n").let {
        it?.dropLast(1)?.forEach(this::println)
        delegate.print(if (shouldIndent.getAndSet(false)) "$indent${it?.last()}" else it?.last())
    }

    override fun print(obj: Any?) = print("$obj")
    override fun print(b: Boolean) = print(b as Any)
    override fun print(c: Char) = print(c as Any)
    override fun print(i: Int) = print(i as Any)
    override fun print(l: Long) = print(l as Any)
    override fun print(f: Float) = print(f as Any)
    override fun print(d: Double) = print(d as Any)
    override fun print(s: CharArray) = print(String(s))

    override fun println(x: String?) = x?.split("\n").let {
        it?.asSequence()?.take(it.size - 1)?.forEach(::putLine)
        putLine(it?.last())
    }

    private fun putLine(s: String?) = delegate.println(if (shouldIndent.getAndSet(true)) "$indent$s" else s)

    override fun println(x: Any?) = println("$x")
    override fun println() = println("")
    override fun println(x: Boolean) = println(x as Any)
    override fun println(x: Char) = println(x as Any)
    override fun println(x: Int) = println(x as Any)
    override fun println(x: Long) = println(x as Any)
    override fun println(x: Float) = println(x as Any)
    override fun println(x: Double) = println(x as Any)
    override fun println(x: CharArray) = println(String(x))
}

@PublishedApi
internal const val defaultIndent = "    "