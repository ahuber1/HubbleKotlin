package ahuber.kotlin.hubble.utils

import java.awt.image.BufferedImage
import kotlin.math.sqrt

val Int.normalizedByteValue: Byte get() {
    val minInt = Int.MIN_VALUE.toDouble()
    val maxInt = Int.MAX_VALUE.toDouble()
    return ((this - minInt) / (maxInt - minInt) * (Byte.MAX_VALUE - Byte.MIN_VALUE) + Byte.MIN_VALUE).toByte()
}

fun Array<Int>.writeGreyscaleImage() = this.toIntArray().writeGreyscaleImage()

fun IntArray.writeGreyscaleImage() = this.normalize().writeGreyscaleImage()

fun Array<Byte>.writeGreyscaleImage() = this.toByteArray().writeGreyscaleImage()

fun ByteArray.writeGreyscaleImage(): BufferedImage {
    val length = sqrt(size.toDouble()).toInt()
    val image = BufferedImage(length, length, BufferedImage.TYPE_BYTE_GRAY)
    var index = 0

    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            image.raster.setPixel(i, j, intArrayOf(this[index++].toInt()))
        }
    }

    return image
}

fun IntArray.normalize(): ByteArray {
    val byteArray = ByteArray(size)
    for ((index, value) in this.withIndex()) {
        byteArray[index] = value.normalizedByteValue
    }
    return byteArray
}