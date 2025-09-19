package org.gorttar.control

import org.gorttar.data.heterogeneous.list.HCons
import org.gorttar.data.heterogeneous.list.HList
import org.gorttar.data.heterogeneous.list.HList1
import org.gorttar.data.heterogeneous.list.HNil
import org.gorttar.data.heterogeneous.list.a
import org.gorttar.data.heterogeneous.list.plus
import java.io.Closeable
import kotlin.reflect.KMutableProperty0

class ManagedValue<A>(
    @PublishedApi internal val get: () -> A,
    @PublishedApi internal val set: (A) -> Unit
)

fun <A> managed(get: () -> A, set: (A) -> Unit): ManagedValue<A> = ManagedValue(get, set)
fun <A> managed(aProp: KMutableProperty0<A>): ManagedValue<A> = managed(aProp::get, aProp::set)

inline fun <reified A> coManaged(
    crossinline get: () -> A,
    crossinline set: (A) -> Unit
): ManagedValue<HList1<A>> = managed(get = { HNil + get() }, set = { set(it.a) })

inline fun <L : HList<L>, reified B> ManagedValue<L>.coManaged(
    crossinline getB: () -> B,
    crossinline setB: (B) -> Unit
): ManagedValue<HCons<L, B>> = managed(
    get = { get() + getB() },
    set = { set(it.head); setB(it.tail) }
)

inline fun <reified A> coManaged(
    property: KMutableProperty0<A>
): ManagedValue<HList1<A>> = coManaged(property::get, property::set)

inline fun <L : HList<L>, reified B> ManagedValue<L>.coManaged(
    bProperty: KMutableProperty0<B>
): ManagedValue<HCons<L, B>> = coManaged(bProperty::get, bProperty::set)

inline fun <T, R> ManagedValue<T>.on(t: T, block: (old: T) -> R): R = get().let {
    Closeable { set(it) }.use { _ ->
        set(t)
        block(it)
    }
}

inline fun <T, R> ManagedValue<T>.onTransform(
    tTransformer: (T) -> T,
    block: (old: T, new: T) -> R
): R = tTransformer(get()).let { new -> on(new) { block(it, new) } }
