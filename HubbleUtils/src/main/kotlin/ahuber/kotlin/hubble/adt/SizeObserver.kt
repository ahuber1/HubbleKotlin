package ahuber.kotlin.hubble.adt

interface SizeObserver<C> {
    fun sizeChanged(collection: C)
}