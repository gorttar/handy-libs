package org.gorttar.data.heterogeneous.list.generators

import org.gorttar.data.heterogeneous.list.HCons
import org.gorttar.data.heterogeneous.list.HNil
import org.gorttar.data.heterogeneous.list.requireSimpleName
import java.io.FileOutputStream
import java.io.PrintStream
import kotlin.reflect.jvm.javaField

internal typealias PropName = Char
internal typealias TypeName = Char

internal const val minPropName: PropName = 'a'

internal const val maxPropName: PropName = 'z'
internal val maxPropNumber: Int = maxPropName.number

internal val packagePath: String by lazy { packageName.replace('.', '/') }
internal val PropName.typeName: TypeName get() = uppercaseChar()

internal val PropName.number: Int get() = this - minPropName + 1
internal val PropName.ordinalStr: String get() = "$number".let { "$it${lastDigitsToOrdinalSuffix[it.takeLast(2)] ?: "th"}" }

internal val hConsTypeName: String = HCons::class.requireSimpleName()
internal val hNilTypeName: String = HNil::class.requireSimpleName()
internal val headPropName: String = HCons<*, *>::head.name
internal val tailPropName: String = HCons<*, *>::tail.name

internal fun writeMainSrc(
    srcName: String,
    content: String,
    fileHeader: String? = null
): Unit = writeSrc(srcPath = srcPath, srcName = srcName, content = content, fileHeader = fileHeader)

internal fun writeSrc(
    srcPath: String,
    srcName: String,
    content: String,
    fileHeader: String? = null
): Unit = "$srcPath/$srcName.kt".let(::FileOutputStream).let(::PrintStream).use {
    it.print(
        """
        |${fileHeader?.let { "$fileHeader\n|" } ?: ""}/**
        | * !!!This file is auto generated! All changes to it will be overwritten after running generator!!!
        | */
        |
        |package $packageName
        |
        |$content""".trimMargin()
    )
}

private val packageName: String =
    ::packageName.javaField!!.declaringClass.name.replace("(\\.[^.]*){2}$".toRegex(), "")
private val srcPath = "src/main/kotlin/$packagePath"
private val lastDigitsToOrdinalSuffix = ((2..9).asSequence().map { "$it" } + "").flatMap {
    sequenceOf("1" to "st", "2" to "nd", "3" to "rd").map { (digit, suffix) -> "$it$digit" to suffix }
}.toMap()