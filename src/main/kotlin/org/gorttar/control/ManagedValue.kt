package org.gorttar.control

import org.gorttar.data.heterogeneous.list.*
import kotlin.reflect.KMutableProperty0

class ManagedValue<V>(
    @PublishedApi
    internal inline val get: () -> V,
    @PublishedApi
    internal inline val set: (V) -> Unit
)

fun <A> managed(getA: () -> A, setA: (A) -> Unit): ManagedValue<A> = ManagedValue(getA, setA)
fun <A> managed(aProp: KMutableProperty0<A>): ManagedValue<A> = managed(aProp::get, aProp::set)

inline fun <A, B> ManagedValue<A>.coManaged(
    crossinline getB: () -> B,
    crossinline setB: (B) -> Unit
): ManagedValue<HList2<A, B>> = managed({ get().`+`(getB()) }) {
    set(it.head.tail)
    setB(it.tail)
}

@JvmName("coManagedN")
inline fun <L : HList<L>, B> ManagedValue<L>.coManaged(
    crossinline getB: () -> B,
    crossinline setB: (B) -> Unit
): ManagedValue<HCons<L, B>> = managed({ get() + getB() }) {
    set(it.head)
    setB(it.tail)
}

fun <A, B> ManagedValue<A>.coManaged(
    bProp: KMutableProperty0<B>
): ManagedValue<HList2<A, B>> = coManaged(bProp::get, bProp::set)

@JvmName("coManagedN")
fun <L : HList<L>, B> ManagedValue<L>.coManaged(
    bProp: KMutableProperty0<B>
): ManagedValue<HCons<L, B>> = coManaged(bProp::get, bProp::set)

inline fun <T, R> ManagedValue<T>.on(t: T, block: (old: T) -> R): R = get().let {
    try {
        set(t)
        block(it)
    } finally {
        set(it)
    }
}

inline fun <T, R> ManagedValue<T>.onTransform(
    tTransformer: (T) -> T,
    block: (oldAndNew: Pair<T, T>) -> R
): R = tTransformer(get()).let { new -> on(new) { block(it to new) } }