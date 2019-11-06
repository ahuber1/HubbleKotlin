package ahuber.kotlin.hubble.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class UtilsTest {

    @Test
    fun testGetMiddle() {
        assertThat((0..10).middle).isEqualTo(5)
        assertThat((10..20).middle).isEqualTo(15)
    }
}