package ahuber.kotlin.hubble.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

suspend fun <T: Comparable<T>> Array<T>.mergeSort(threshold: Int): Array<T> {
    require(threshold <= size) {
        "'threshold' must be less than or equal to the size of the array. (threshold = $threshold, array size = $size)"
    }

    return this.mergeSort(this.indices, threshold)
}

inline infix fun <reified T: Comparable<T>> Array<T>.merge(array2: Array<T>): Array<T> {
    val combined = combineArrays(this, array2).toTypedArray()
    return combined.merge(combined.indices)
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
    return merge(indexRange)
}

fun <T: Comparable<T>> Array<T>.merge(indexRange: IntRange): Array<T> {
    val temp = this.copyOf()
    var left = indexRange.first
    var right = indexRange.middle + 1
    val index = 0

    while (index < temp.size) {
        temp[index] = when {
            left == indexRange.middle + 1 -> {
                left--
                this[right]
            }
            right == indexRange.last + 1 -> {
                right--
                this[left]
            }
            this[left] < this[right] -> {
                right--
                this[left]
            }
            else -> {
                left--
                this[right]
            }
        }
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