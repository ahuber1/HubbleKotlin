package ahuber.kotlin.hubble.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

suspend fun <T: Comparable<T>> Array<T>.mergeSort(threshold: Int): Array<T> {
    require(threshold <= size) {
        "'threshold' must be less than or equal to the size of the array. (threshold = $threshold, array size = $size)"
    }

    return this.copyOf().mergeSort(this.indices, threshold)
}

inline infix fun <reified T: Comparable<T>> Array<T>.mergeWith(array2: Array<T>): Array<T> {
    val combined = combine(this, array2).toTypedArray()
    return combined.mergeWith(combined.indices, this.lastIndex)
}

private suspend fun <T: Comparable<T>> Array<T>.mergeSort(indexRange: IntRange, threshold: Int): Array<T> {
    val subArraySize = this.getSubArraySize(indexRange)

    if (subArraySize < threshold) {
        insertionSort(indexRange)
        return this
    }

    val leftJob = GlobalScope.launch {
        mergeSort(indexRange.first..indexRange.middle, threshold)
    }
    val rightJob = GlobalScope.launch {
        mergeSort((indexRange.middle + 1)..indexRange.last, threshold)
    }

    joinAll(leftJob, rightJob)
    return mergeWith(indexRange)
}

fun <T: Comparable<T>> Array<T>.mergeWith(indexRange: IntRange, middle: Int? = null): Array<T> {
    val temp = ArrayList<T>(indexRange.length)

    var left = indexRange.first

    @Suppress("NAME_SHADOWING")
    val middle = middle ?: indexRange.middle

    var right = middle + 1

    while (temp.size < indexRange.length) {
         val element = when {
            left == middle + 1 -> {
                this[right++]
            }
            right == indexRange.last + 1 -> {
                this[left++]
            }
            this[left] < this[right] -> {
                this[left++]
            }
            else -> {
                this[right++]
            }
        }
        temp.add(element)
    }

    temp.forEachIndexed { i, element -> this[i] = element }
    return this
}

private fun <T: Comparable<T>> Array<T>.insertionSort(indexRange: IntRange): Array<T> {
    for (i in indexRange) {
        for (j in (i + 1)..indexRange.last) {
            if (this[i] > this[j]) {
                this.swap(i, j)
            }
        }
    }

    return this
}