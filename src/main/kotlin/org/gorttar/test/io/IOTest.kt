package org.gorttar.test.io

import assertk.assertThat
import assertk.assertions.support.fail
import org.gorttar.control.coManaged
import org.gorttar.control.on
import org.gorttar.data.heterogeneous.list.hListOf
import org.junit.jupiter.api.assertTimeoutPreemptively
import org.opentest4j.AssertionFailedError
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.time.Duration

/** API section */

/** performs console IO test described by [scenario] on given [block] */
fun runIOTest(
    scenario: IOTestBuilder.() -> Unit,
    block: () -> Unit
): Unit = doIOTesting(scenario, block, ::recordIO)

/**
 * performs console IO test described by [scenario] on given [block]
 * execution time is limited by [timeout]
 */
fun runIOTest(
    scenario: IOTestBuilder.() -> Unit,
    timeout: Duration,
    block: () -> Unit
): Unit = doIOTesting(scenario, block) { input, _ -> recordIO(input, timeout, block) }

/** Feeds [input] to [System.`in`] of given [block] and records its console IO activity */
fun recordIO(input: String, block: () -> Unit): List<IORecord> = IORecorder().doRecording(input, block).records

/**
 * Feeds [input] to [System.`in`] of given [block] and records its console IO activity
 * execution time is limited by [timeout]
 */
fun recordIO(input: String, timeout: Duration, block: () -> Unit): List<IORecord> = IORecorder().let { recorder ->
    recorder.runCatching {
        assertTimeoutPreemptively(timeout) { doRecording(input, block) }
    }.fold({ recorder.records }) { if (it is AssertionFailedError) recorder.records + TIMEOUT(it) else throw it }
}

sealed class IOTestBuilder {
    /**
     * declares that [expected] string should be passed to [System.out] by tested block
     * @see runIOTest
     */
    fun expectString(expected: String): Unit =
        recordsBuilder.recordOUT { write(expected.normalizeLineBreaks().toByteArray()) }

    /**
     * declares that [expected] line should be passed to [System.out] by tested block
     * @see runIOTest
     */
    fun expectLine(expected: String): Unit = expectString("$expected$cr")

    /**
     * declares that [expected] string should be passed to [System.err] by tested block
     * @see runIOTest
     */
    fun expectErrString(expected: String): Unit =
        recordsBuilder.recordERR { write(expected.normalizeLineBreaks().toByteArray()) }

    /**
     * declares that [expected] line should be passed to [System.err] by tested block
     * @see runIOTest
     */
    fun expectErrLine(expected: String): Unit = expectErrString("$expected$cr")

    /**
     * declares that [line] should be passed to [System.`in`] by [runIOTest] and read by tested block
     * @see runIOTest
     */
    fun sendLine(line: String): Unit = "${line.normalizeLineBreaks()}$cr".let {
        inputBuilder.append(it)
        recordsBuilder.recordIN { write(it.toByteArray()) }
    }

    internal val ioTest get() = IOTest("$inputBuilder", recordsBuilder.records)
    private val recordsBuilder = IORecordsBuilder()
    private val inputBuilder = StringBuilder()
}

/** OS independent line separator */
val cr: String by lazy {
    ByteArrayOutputStream().use {
        PrintStream(it).println()
        it.toString()
    }
}

/** implementation section */

internal class IOTest(val input: String, val expectedRecords: List<IOContentRecord>)
private class IOTestBuilderImpl : IOTestBuilder()

private class IORecorder {
    val records: List<IOContentRecord> get() = recordsBuilder.records
    fun recordINByte(byte: Byte): Unit = recordsBuilder.recordIN { write(byte.toInt()) }
    fun recordOUTByte(byte: Byte): Unit = recordsBuilder.recordOUT { write(byte.toInt()) }
    fun recordERRByte(byte: Byte): Unit = recordsBuilder.recordERR { write(byte.toInt()) }

    fun recordINBytes(bytes: ByteArray, off: Int, len: Int): Unit =
        recordsBuilder.recordIN { write(bytes, off, len) }

    fun recordOUTBytes(bytes: ByteArray, off: Int, len: Int): Unit =
        recordsBuilder.recordOUT { write(bytes, off, len) }

    fun recordERRBytes(bytes: ByteArray, off: Int, len: Int): Unit =
        recordsBuilder.recordERR { write(bytes, off, len) }

    private val recordsBuilder = IORecordsBuilder()
}

private class IORecordsBuilder {
    val records: List<IOContentRecord>
        get() = _records + (curOperation?.run { listOf(curRecord) } ?: emptyList())

    inline fun recordIN(recorderBlock: OutputStream.() -> Unit): Unit = IN_TYPE.record(recorderBlock)
    inline fun recordOUT(recorderBlock: OutputStream.() -> Unit): Unit = OUT_TYPE.record(recorderBlock)
    inline fun recordERR(recorderBlock: OutputStream.() -> Unit): Unit = ERR_TYPE.record(recorderBlock)

    private inline fun IOContentRecord.record(
        recorderBlock: OutputStream.() -> Unit
    ) = curOperation.takeIf { it != this }?.also {
        _records += it.curRecord
        curContentBuilder.reset()
    }.let {
        curOperation = this
        curContentBuilder.recorderBlock()
    }

    private val curContentBuilder = ByteArrayOutputStream()
    private var curOperation: IOContentRecord? = null
    private val _records = arrayListOf<IOContentRecord>()
    private val IOContentRecord.curRecord
        get() = "$curContentBuilder".let {
            when (this) {
                is IN -> IN(it)
                is OUT -> OUT(it)
                is ERR -> ERR(it)
            }
        }

    companion object {
        private val IN_TYPE = IN("")
        private val OUT_TYPE = OUT("")
        private val ERR_TYPE = ERR("")
    }
}

private fun doIOTesting(
    scenario: IOTestBuilder.() -> Unit,
    block: () -> Unit,
    recordIO: (String, () -> Unit) -> List<IORecord>
) = IOTestBuilderImpl().apply(scenario).ioTest.let { test ->
    assertThat(recordIO(test.input, block)).run {
        given {
            if (it != test.expectedRecords) fail(test.expectedRecords.pretty(), it.pretty())
        }
    }
}

private fun IORecorder.doRecording(input: String, block: () -> Unit) = apply {
    coManaged(System::`in`, System::setIn).coManaged(System::out, System::setOut).coManaged(System::err, System::setErr)
        .on(
            hListOf(
                object : ByteArrayInputStream(input.toByteArray()) {
                    override fun read(): Int = super.read().also { recordINByte(it.toByte()) }
                    override fun read(b: ByteArray, off: Int, len: Int): Int =
                        super.read(b, off, len).also { recordINBytes(bytes = b, off = off, len = len) }
                },
                object : ByteArrayOutputStream() {
                    override fun write(b: Int): Unit = super.write(b).also { recordOUTByte(b.toByte()) }
                    override fun write(b: ByteArray, off: Int, len: Int): Unit =
                        super.write(b, off, len).also { recordOUTBytes(b, off, len) }
                }.let(::PrintStream),
                object : ByteArrayOutputStream() {
                    override fun write(b: Int): Unit = super.write(b).also { recordERRByte(b.toByte()) }
                    override fun write(b: ByteArray, off: Int, len: Int): Unit =
                        super.write(b, off, len).also { recordERRBytes(b, off, len) }
                }.let(::PrintStream)
            )
        ) { block() }
}

private fun List<IORecord>.pretty() = joinToString("\n") {
    when (it) {
        is IOContentRecord -> "${it.typeName}(${it.content.lines().joinToString("\\n")})"
        is TIMEOUT -> "${it.typeName} at \n ${ByteArrayOutputStream().let(::PrintStream).let(it.t::printStackTrace)}"
    }
}

private fun String.normalizeLineBreaks() = lineSequence().joinToString(cr)
