package ahuber.kotlin.hubble.utils

fun <T> Array<T>.wrap() = ArrayCollectionWrapper(this)

fun <T> ArrayCollectionWrapper<T>.unwrap() = this.array

class ArrayCollectionWrapper<T>(val array: Array<T>) : MutableList<T> {
    override val size get() = array.size

    override fun contains(element: T): Boolean = element in array

    override fun containsAll(elements: Collection<T>): Boolean = elements.all { it in this }

    override fun get(index: Int): T = array[index]

    override fun indexOf(element: T): Int = array.indexOf(element)

    override fun isEmpty(): Boolean = array.isEmpty()

    override fun iterator(): MutableIterator<T> = listIterator()

    override fun lastIndexOf(element: T) = array.lastIndexOf(element)

    override fun add(element: T) = throw cannotAddOrRemoveException

    override fun add(index: Int, element: T): Unit = throw cannotAddOrRemoveException

    override fun addAll(index: Int, elements: Collection<T>) = throw cannotAddOrRemoveException

    override fun addAll(elements: Collection<T>) = throw cannotAddOrRemoveException

    override fun clear(): Unit = throw cannotAddOrRemoveException

    override fun listIterator(): MutableListIterator<T> = listIterator(0)

    override fun listIterator(index: Int): MutableListIterator<T> = ArrayCollectionWrapperIterator(this, index)

    override fun remove(element: T): Boolean = throw cannotAddOrRemoveException

    override fun removeAll(elements: Collection<T>): Boolean = throw cannotAddOrRemoveException

    override fun removeAt(index: Int): T = throw cannotAddOrRemoveException

    override fun retainAll(elements: Collection<T>): Boolean = false

    override fun set(index: Int, element: T): T {
        val previous = array[index]
        array[index] = element
        return previous
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> =
            array.sliceArray(fromIndex until toIndex).wrap()

    companion object {
        private val cannotAddOrRemoveException
            get() =
                UnsupportedOperationException("Elements cannot be added or removed from the underlying array.")
    }

    private class ArrayCollectionWrapperIterator<T>(private val wrapper: ArrayCollectionWrapper<T>, index: Int) :
            MutableListIterator<T> {

        private var previousIndex: Int? = null

        private var index: Int = 0
            set(value) {
                previousIndex = index
                field = value
            }

        init {
            this.index = index
        }

        override fun nextIndex() = index

        override fun hasNext() = wrapper.indices.contains(nextIndex())

        override fun next(): T {
            val element = wrapper.getOrNull(nextIndex()) ?: throw NoSuchElementException()
            ++index
            return element
        }

        override fun previousIndex() = index - 1

        override fun hasPrevious() = wrapper.indices.contains(previousIndex())

        override fun previous(): T {
            index = previousIndex()
            return wrapper.getOrNull(index) ?: throw NoSuchElementException()
        }

        override fun set(element: T) = previousIndex.run {
            when {
                this == null -> throw UnsupportedOperationException("No elements have been returned by this iterator")
                wrapper.indices.contains(this) -> wrapper[this] = element
                else -> throw ArrayIndexOutOfBoundsException("The iterator is outside the bounds of the underlying array")
            }
        }

        override fun add(element: T) = throw cannotAddOrRemoveException

        override fun remove(): Unit = throw cannotAddOrRemoveException
    }
}