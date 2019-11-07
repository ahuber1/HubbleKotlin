package ahuber.kotlin.hubble.utils

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MergeSortTest {

    @Test
    fun testMergeSort() {
        val array = Array(10000) { it }.reversedArray()
        val actual = runBlocking { array.mergeSort() }
        val expected = Array(10000) { it }
        assertThat(actual.contentEquals(expected)).isTrue()
    }

    @Test
    fun testMergeSortDescending() {
        val array = Array(10000) { it }
        val actual = runBlocking { array.mergeSortDescending() }
        val expected = Array(10000) { it }.reversedArray()
        assertThat(actual.contentEquals(expected)).isTrue()
    }

    @Test
    fun testMerge() {
        val array1 = Array(500) { it }
        val array2 = Array(500) { it + array1.size }
        val mergedArray1 = array1 mergeWith array2
        val mergedArray2 = array2 mergeWith array1
        val expected = (0 until array1.size + array2.size).toList()
        assertThat(mergedArray1 == expected).isTrue()
        assertThat(mergedArray2 == expected).isTrue()
    }

    @Test
    fun testMergeDescending() {
        val array1 = Array(500) { it }.reversedArray()
        val array2 = Array(500) { it + array1.size }.reversedArray()
        val mergedArray1 = array1 mergeWithDescending array2
        val mergedArray2 = array2 mergeWithDescending array1
        val expected = (0 until array1.size + array2.size).toList().asReversed()
        assertThat(mergedArray1 == expected).isTrue()
        assertThat(mergedArray2 == expected).isTrue()
    }
}