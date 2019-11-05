package ahuber.kotlin.hubble.utils

val IntRange.numberOfValues get() = endInclusive - start + 1

fun Array<*>.getSubArraySize(startAndEnd: IntRange) = calculateArraySize(startAndEnd, size)

fun calculateArraySize(startAndEnd: IntRange, arrayLength: Int): Int {
    val argumentDescription: String by lazy {
        "Arguments: [startAndEnd: $startAndEnd, arrayLength: $arrayLength]"
    }
    require(arrayLength >= 0) { "arrayLength < 0 ($argumentDescription)" }
    return if (startAndEnd.first == 0 && startAndEnd.last == 0 && arrayLength == 0) {
        0
    } else {
        startAndEnd.numberOfValues
    }
}

fun <T> Array<T>.swap(index1: Int, index2: Int) {
    val temp = this[index1]
    this[index1] = this[index2]
    this[index2] = temp
}

inline fun <reified T> combineArrays(vararg arrays: Array<T>): Array<T> {
    val length = arrays.map { it.size }.sum()
    val combined = ArrayList<T>(length)

    for (array in arrays) {
        for (element in array) {
            combined.add(element)
        }
    }

    return combined.toTypedArray()
}