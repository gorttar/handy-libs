package org.gorttar.repr

import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.memberFunctions

val Any?.repr: String get() = repr(ProcessedCompounds())

private val componentNamePattern = "^component[0-9]+$".toRegex()

private fun Any?.repr(processed: ProcessedCompounds): String = when (this) {
    null -> "null"
    is String -> "\"$this\""
    is Char -> "'$this'"
    is Number, is Boolean -> "$this"
    is Collection<*> -> repr("[", "]", this, processed)
    is Map<*, *> -> repr("{", "}", entries, processed) { (k, v) -> "${k.repr(processed)}=${v.repr(processed)}" }
    is Pair<*, *> -> repr("Pair(", ")", this.toList(), processed)
    is Triple<*, *, *> -> repr("Triple(", ")", this.toList(), processed)
    else -> {
        val kClass = this::class
        val asCollection = kClass.memberFunctions.asSequence()
            .filter { it.name.matches(componentNamePattern) }
            .sortedBy { it.name.replace("component", "").toInt() }
            .filter { it.visibility == PUBLIC }
            .filterIsInstance<Function1<Any, Any?>>()
            .map { it(this) }
            .toList()
        if (asCollection.isNotEmpty()) repr("${kClass.simpleName}(", ")", asCollection, processed)
        else "$this"
    }
}

private inline fun <E> Any.repr(
    prefix: String,
    postfix: String,
    asCollection: Collection<E>,
    processed: ProcessedCompounds,
    crossinline transform: (E) -> String = { it.repr(processed) }
): String = when (this) {
    in processed -> "(cycle ${
        when (this) {
            is Collection<*> -> "Collection"
            is Map<*, *> -> "Map"
            is Pair<*, *> -> "Pair"
            is Triple<*, *, *> -> "Triple"
            else -> "${this::class.simpleName}"
        }
    } #${processed[this]})"
    else -> {
        processed.add(this)
        asCollection.joinToString(", ", prefix, postfix) { transform(it) }
    }
}

private class ProcessedCompounds {
    private class IdentityKey(private val obj: Any) {
        override fun equals(other: Any?): Boolean = other is IdentityKey && obj === other.obj
        override fun hashCode(): Int = System.identityHashCode(obj)
    }

    private var c = 1
    private val processedCompounds = mutableMapOf<IdentityKey, Int>()

    operator fun contains(obj: Any): Boolean = IdentityKey(obj) in processedCompounds
    fun add(obj: Any): Int = processedCompounds.computeIfAbsent(IdentityKey(obj)) { c++ }
    operator fun get(obj: Any): Int? = processedCompounds[IdentityKey(obj)]
}