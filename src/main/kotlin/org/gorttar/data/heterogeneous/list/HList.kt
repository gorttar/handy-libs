@file:Suppress("NOTHING_TO_INLINE")

package org.gorttar.data.heterogeneous.list

import java.util.*
import kotlin.collections.AbstractList
import kotlin.reflect.KClass

/**
 * left sub listed heterogeneous list.
 * Left sub listed means that
 * *    values are stored in [HCons.tail] (right part) while consecutive sub lists in [HCons.head] (left part)
 * *    It's more convenient from usage perspective than right sub listed heterogeneous list
 * *    because operators and infix functions in Kotlin are grouped from left to right.
 * *    For example a + b + c + d actually means ((a + b) + c) + d
 * *    so left sub listing can eliminate brackets in list construction expressions
 * Heterogeneous means that list preserves types of its elements in full type signature
 * [L] - full type of list
 */
sealed class HList<out L : HList<L>> {
    abstract val size: Int
    abstract val rawList: List<Any?>

    override fun equals(other: Any?): Boolean = this === other || other is HList<*> && rawList == other.rawList
    override fun hashCode(): Int = rawList.hashCode()
    override fun toString(): String = "$hListTypeName$rawList"
}

/**
 * Empty [HList]
 */
object HNil : HList<HNil>() {
    override val size: Int = 0
    override val rawList: List<Any?> = emptyList()
}

private class ListView<out T>(
    private val delegate: List<T>,
    private val offset: Int = 0,
    lastIndex: Int = delegate.lastIndex
) : AbstractList<T>() {
    private fun Int.toIndex(): Int = this - offset

    override val size: Int = lastIndex.toIndex() + 1
    override fun get(index: Int): T = index.toIndex().let {
        Objects.checkIndex(it, size)
        delegate[it]
    }
}

private class ChunkedList<T>(
    private val prevChunk: List<T> = listOf()
) : AbstractMutableList<T>() {
    private val chunk: MutableList<T> = mutableListOf()
    private fun Int.chunkIndex() = this - prevChunk.size

    override val size: Int get() = prevChunk.size + chunk.size
    override fun add(index: Int, element: T): Unit = chunk.add(index.chunkIndex(), element)
    override fun get(index: Int): T = if (index in prevChunk.indices) prevChunk[index] else chunk[index.chunkIndex()]
    override fun removeAt(index: Int): T = chunk.removeAt(index.chunkIndex())
    override fun set(index: Int, element: T): T = chunk.set(index.chunkIndex(), element)
}

/**
 * [HList] node constructor
 * [head] - consecutive sub list
 * [tail] - node value
 */
class HCons<out L : HList<L>, out A>(val head: L, tail: A) : HList<HCons<L, A>>() {
    private var isTail = true
    private val _chunk: MutableList<Any?> = when (val h = head as HList<*>) {
        is HCons<*, *> -> h.chunk
        is HNil -> ChunkedList()
    }
        .apply { add(tail) }

    private val chunk: MutableList<Any?> get() = if (isTail) _chunk.also { isTail = false } else ChunkedList(rawList)

    override val rawList: List<Any?> = ListView(_chunk)

    @Suppress("UNCHECKED_CAST")
    val tail: A = rawList.last() as A
    override val size: Int = head.size + 1
}

fun <L : HList<L>, A> HCons<L, *>.copy(tail: A): HCons<L, A> = HCons(head, tail)

fun <L : HList<L>, A> HCons<*, A>.copy(head: L): HCons<L, A> = HCons(head, tail)

fun <L : HList<L>, A> HCons<L, A>.copy(): HCons<L, A> = this

/**
 * creates [HList] by appending [a] to [this]
 */
inline operator fun <L : HList<L>, A> L.plus(a: A): HCons<L, A> = HCons(this, a)

/**
 * creates [HList] by appending [a] to [this]
 */
operator fun <L : HList<L>, A> L.get(a: A): HCons<L, A> = this + a

internal fun KClass<*>.requireSimpleName(): String = requireNotNull(simpleName)
internal val hListTypeName: String = HList::class.requireSimpleName()
