package ahuber.kotlin.hubble.aws

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SatelliteConfiguration(val i: Int, val j: Int)