package ahuber.kotlin.hubble.utils

operator fun <C : MutableCollection<T>, T> C.invoke(block: suspend SequenceScope<T>.() -> Unit) {
    this.addAll(sequence(block))
}

suspend fun <T> SequenceScope<T>.yieldElement(block: () -> T) = this.yield(block())