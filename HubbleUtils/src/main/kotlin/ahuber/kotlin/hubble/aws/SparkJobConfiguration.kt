package ahuber.kotlin.hubble.aws

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SparkJobConfiguration(val name: String, val threshold: Int, val data: Array<Int>) {

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is SparkJobConfiguration -> false
            name != other.name -> false
            threshold != other.threshold -> false
            !data.contentEquals(other.data) -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + threshold
        result = 31 * result + data.contentHashCode()
        return result
    }
}