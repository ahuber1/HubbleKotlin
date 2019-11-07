package ahuber.kotlin.hubble.utils

interface Adder<T> {
    fun add(element: T)
}

inline fun <T> Adder<T>.add(block: () -> T) = add(block())

fun <T> Adder<T>.addAll(vararg elements: T) = addAll(elements.asIterable())
fun <T> Adder<T>.addAll(elements: Sequence<T>)  = addAll(elements.asIterable())
fun <T> Adder<T>.addAll(elements: Iterable<T>)  = elements.forEach { this.add(it) }

fun <T> MutableCollection<T>.createAdder() = object: Adder<T> {
    override fun add(element: T) {
        this@createAdder.add(element)
    }
}