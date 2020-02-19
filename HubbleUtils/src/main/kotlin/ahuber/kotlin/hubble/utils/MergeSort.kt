package ahuber.kotlin.hubble.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

suspend fun <T : Comparable<T>> Array<T>.mergeSort(threshold: Int = 2): Array<T> {
    return mergeSort(threshold, ascendingComparator())
}

suspend fun <T : Comparable<T>> Array<T>.mergeSortDescending(threshold: Int = 2): Array<T> {
    return mergeSort(threshold, descendingComparator())
}

infix fun <T : Comparable<T>> Array<T>.mergeWith(array2: Array<T>) =
        this.merge(array2, ascendingComparator())

infix fun <T : Comparable<T>> Array<T>.mergeWithDescending(array2: Array<T>) =
        merge(array2, descendingComparator())

private suspend fun <T : Comparable<T>> Array<T>.mergeSort(threshold: Int, comparator: Comparator<T>): Array<T> {
    require(threshold in 2..size) {
        "'threshold' must be greater than 1 and less than or equal to the size of the array. " +
                "(threshold = $threshold, array size = $size)"
    }

    return this.copyOf().mergeSort(this.indices, threshold, comparator)
}

private fun <T : Comparable<T>> Array<T>.merge(array2: Array<T>, comparator: Comparator<T>): List<T> {
    val combined = combineArrays(this, array2).toMutableList()
    return combined.merge(combined.indices, comparator, this.lastIndex)
}

private suspend fun <T : Comparable<T>> Array<T>.mergeSort(indexRange: IntRange, threshold: Int,
        comparator: Comparator<T>): Array<T> {
    val subArraySize = this.getSubArraySize(indexRange)

    if (subArraySize <= threshold) {
        insertionSort(indexRange, comparator)
        return this
    }

    val leftJob = GlobalScope.launch {
        mergeSort(indexRange.first..indexRange.middle, threshold, comparator)
    }
    val rightJob = GlobalScope.launch {
        mergeSort((indexRange.middle + 1)..indexRange.last, threshold, comparator)
    }

    joinAll(leftJob, rightJob)
    return this.wrap().merge(indexRange, comparator).unwrap()
}

private fun <L : MutableList<T>, T : Comparable<T>> L.merge(indexRange: IntRange, comparator: Comparator<T>? = null,
        middle: Int? = null): L {
    val temp = ArrayList<T>(indexRange.length)

    var left = indexRange.first

    @Suppress("NAME_SHADOWING")
    val middle = middle ?: indexRange.middle

    var right = middle + 1

    @Suppress("NAME_SHADOWING")
    val comparator = comparator ?: ascendingComparator()

    while (temp.size < indexRange.length) {
        val element = when {
            left == middle + 1 -> {
                this[right++]
            }
            right == indexRange.last + 1 -> {
                this[left++]
            }
            comparator.compare(this[left], this[right]) < 0 -> {
                this[left++]
            }
            else -> {
                this[right++]
            }
        }
        temp.add(element)
    }

    for ((tempIndex, receiverIndex) in indexRange.withIndex()) {
        indexRange.withIndex().first().component1()
        this[receiverIndex] = temp[tempIndex]
    }

    return this
}

private fun <T : Comparable<T>> Array<T>.insertionSort(indexRange: IntRange, comparator: Comparator<T>): Array<T> {
    for (i in indexRange) {
        for (j in (i + 1)..indexRange.last) {
            if (comparator.compare(this[i], this[j]) > 0) {
                this.swap(i, j)
            }
        }
    }

    return this
}