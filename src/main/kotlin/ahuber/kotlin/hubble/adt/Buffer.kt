package ahuber.kotlin.hubble.adt

import ahuber.kotlin.hubble.utils.swap
import java.util.*
import kotlin.collections.ArrayList

public class Buffer<T>(val capacity: Int) : MutableCollection<T> {
    private val array: Array<T?>
    private val observers = Collections.synchronizedList(ArrayList<SizeObserver<Buffer<T>>>())
    private var startIndex = 0
    private var endIndex = 0
    private var suppressObservers = false

    init {
        require(capacity > 1) {
            "The capacity of the buffer must be greater than one. The specified capacity was $capacity"
        }

        @Suppress("UNCHECKED_CAST")
        array = arrayOfNulls<Any?>(capacity) as Array<T?>
    }

    override var size: Int = 0
        private set

    @Synchronized
    fun register(observer: SizeObserver<Buffer<T>>) {
        observers.add(observer)
    }

    @Synchronized
    fun unregister(observer: SizeObserver<Buffer<T>>) = observers.remove(observer)

    //region add/remove
    @Synchronized
    override fun add(element: T): Boolean {
        if (isFull) {
            return false
        }

        if (isNotEmpty()) {
            endIndex = incrementIndex(endIndex, array.size)

            if (startIndex == endIndex) {
                startIndex = incrementIndex(startIndex, array.size)
            }
        }

        array[endIndex] = element
        incrementSize()
        return true
    }

    @Synchronized
    override fun remove(element: T): Boolean {
        val iterator = IndexedIterator(this)
        var previous = -1

        while (iterator.hasNext()) {
            val (index, item) = iterator.next()

            // If we did not find the item to remove
            if (previous == -1) {
                if (element == item) {
                    previous = index
                }

                continue
            }

            // If we did find the item to remove, swap
            array.swap(previous, index)
            previous = index
        }

        // If we did NOT find the item to remove
        if (previous == -1) {
            return false
        }

        endIndex = decrementIndex(endIndex, array.size)
        decrementSize()
        return true
    }
    //endregion

    override fun clear() {
        startIndex = 0
        endIndex = 0
        setSize(0)
    }

    override fun addAll(elements: Collection<T>) = addAll(elements, true)

    fun addAll(elements: Collection<T>, failIfInsufficientSpace: Boolean = true) = makeBulkChanges {
        val newSize = size + elements.size

        if (failIfInsufficientSpace && newSize > capacity) {
            return@makeBulkChanges false
        }

        var collectionChanged = false

        for (it in elements) {
            val addSuccessful = add(it)
            collectionChanged = collectionChanged || addSuccessful

            // Break from loop if we can no longer add items
            if (!addSuccessful) {
                break;
            }
        }

        return@makeBulkChanges collectionChanged
    }

    override fun removeAll(elements: Collection<T>) = makeBulkChanges {
        elements.fold(false) { collectionChanged, item ->
            val itemRemoved = remove(item)
            return collectionChanged || itemRemoved
        }
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val itemsToRemove = this.filter { !elements.contains(it) }
        return removeAll(itemsToRemove)
    }

    fun extract(n: Int) = makeBulkChanges {
        @Suppress("NAME_SHADOWING") val n = n.coerceIn(0 until size)
        val copy = take(n)
        startIndex = incrementIndex(startIndex, array.size, copy.size)
        setSize(size - copy.size)
        copy
    }

    @Synchronized
    override fun contains(element: T) = any { it == element }

    @Synchronized
    override fun containsAll(elements: Collection<T>) =
        when {
            elements.isEmpty() && isEmpty() -> true
            elements.isEmpty() || isEmpty() -> false
            else -> all(this::contains)
        }

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): MutableIterator<T> = BufferIterator(IndexedIterator(this))

    private fun invokeObservers() {
        if (suppressObservers) {
            return
        }

        observers.forEach { it.sizeChanged(this) }
    }

    //region increment/decrement/set size
    private fun incrementSize() = setSize(size + 1)

    private fun decrementSize() = setSize(size - 1)

    private fun setSize(newValue: Int) {
        if (size == newValue) {
            return
        }

        require(newValue >= 0) { "size cannot be negative. (newValue = $newValue)" }
        require (newValue < capacity) {
            "The size cannot be greater than the capacity. (newValue = $newValue, capacity = $capacity)"
        }

        size = newValue
        invokeObservers()
    }
    //endregion

    private inline fun <T1> makeBulkChanges(block: () -> T1): T1 {
        val previousSize = size

        try {
            suppressObservers = true
            return block()
        } finally {
            suppressObservers = false

            if (size != previousSize) {
                invokeObservers()
            }
        }
    }

    private class IndexedIterator<T>(private val buffer: Buffer<T>) : MutableIterator<IndexedValue<T>> {
        private var actualIndex: Int = buffer.startIndex
        private var logicalIndex: Int = 0
        private var previous: T? = null

        override fun hasNext(): Boolean = actualIndex != incrementIndex(buffer.endIndex, buffer.array.size)

        override fun next(): IndexedValue<T> {
            val item = buffer.array[actualIndex]!!
            val indexedItem = IndexedValue(logicalIndex++, item)
            previous = item
            actualIndex = incrementIndex(actualIndex, buffer.array.size)
            previous = item
            return indexedItem
        }

        override fun remove() {
            val previous = this.previous

            if (previous != null) {
                buffer.remove(previous)
            }
        }
    }

    private class BufferIterator<T>(private val iterator: IndexedIterator<T>): MutableIterator<T> {
        override fun hasNext() = iterator.hasNext()
        override fun next() = iterator.next().value
        override fun remove() = iterator.remove()
    }

    private companion object {
        fun incrementIndex(index: Int, length: Int, amount: Int = 1) =
            (index + amount.coerceIn(0..length)) % length

        fun decrementIndex(index: Int, length: Int) = (index + length - 1) % length
    }
}

val <T> Buffer<T>.isFull get() = size == capacity