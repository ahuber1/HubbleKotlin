package ahuber.kotlin.hubble.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ArrayUtilsTests {

    @Test
    fun testGetNumberOfValues() {
        assertThat((0..0).length).isEqualTo(1)
        assertThat((0..1).length).isEqualTo(2)
    }

    @Test
    fun testGetSubArraySize() {
        val values = Array(10) { it }
        val indexRange = 5..9
        val subArraySize = values.getSubArraySize(indexRange)
        assertThat(subArraySize).isEqualTo(5)
    }

    @Test
    fun testCalculateArraySize() {
        val subArraySize = calculateArraySize(5..9, 10)
        assertThat(subArraySize).isEqualTo(5)
    }

    @Test
    fun swap() {
        val array = arrayOf(1, 2)
        array.swap(0, 1)
        assertThat(array.contentEquals(arrayOf(2, 1))).isTrue()
    }

    @Test
    fun testCombineArrays() {
        val array1 = Array(5) { it }
        val array2 = Array(5) { it + array1.size }
        val array3 = Array(5) { it + array1.size + array2.size }
        val array4 = Array(5) { it + array1.size + array2.size + array3.size }
        val combined = combine(array1, array2, array3, array4)
        assertThat(combined == (0 until 20).asSequence().toList())
    }
}