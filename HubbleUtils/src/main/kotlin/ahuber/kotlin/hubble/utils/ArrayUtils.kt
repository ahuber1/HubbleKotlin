package ahuber.kotlin.hubble.utils

val IntRange.length get() = endInclusive - start + 1

fun Array<*>.getSubArraySize(indexRange: IntRange) = calculateArraySize(indexRange, size)

fun calculateArraySize(indexRange: IntRange, arrayLength: Int): Int {
    require(arrayLength >= 0) { "arrayLength < 0 (Arguments: [indexRange: $indexRange, arrayLength: $arrayLength])" }
    return if (indexRange.first == 0 && indexRange.last == 0 && arrayLength == 0) {
        0
    } else {
        indexRange.length
    }
}

fun <T> Array<T>.swap(index1: Int, index2: Int) {
    val temp = this[index1]
    this[index1] = this[index2]
    this[index2] = temp
}

fun <T> combineArrays(vararg arrays: Array<T>): List<T> {
    val length = arrays.map { it.size }.sum()
    val combined = ArrayList<T>(length)

    for (array in arrays) {
        for (element in array) {
            combined.add(element)
        }
    }

    return combined.toList()
}