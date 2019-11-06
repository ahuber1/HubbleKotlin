package ahuber.kotlin.hubble.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MergeSortTest {

    @Test
    suspend fun testMergeSort() {
        val array = Array(10000) { it }.reversedArray()
        val actual = array.mergeSort(1)
        val expected = Array(10000) { it }
        assertThat(actual.contentEquals(expected)).isTrue()
    }

    @Test
    fun testMerge() {
        val array1 = Array(500) { it }
        val array2 = Array(500) { it + array1.size }
        val mergedArray1 = array1 mergeWith array2
        val mergedArray2 = array2 mergeWith array1
        val expected = Array(arrayOf(array1.size, array2.size).sum()) { it }
        assertThat(mergedArray1.contentEquals(expected)).isTrue()
        assertThat(mergedArray2.contentEquals(expected)).isTrue()
    }
}