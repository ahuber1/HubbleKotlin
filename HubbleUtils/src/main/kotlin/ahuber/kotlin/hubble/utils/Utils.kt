package ahuber.kotlin.hubble.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.Comparator

inline val IntRange.middle get() = this.first + this.length / 2

val moshiKotlin: Moshi get() = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

inline fun <T> T.configure(block: T.() -> Unit) = apply(block)

fun String.convertToRegion() = Regions.values().firstOrNull { it.name == this }

fun getHubbleLogger(context: Context? = null): HubbleLogger = when (val logger = context?.logger) {
    null -> object : HubbleLogger {
        override fun log(str: String) {
            print(str)
        }
    }
    else -> object : HubbleLogger {
        override fun log(str: String) {
            logger.log(str)
        }
    }
}

inline fun <reified T> Moshi.adapter() = this.adapter(T::class.java)!!

inline fun <reified T> className() = T::class.java.name!!

fun <T : Comparable<T>> ascendingComparator() = Comparator<T> { o1, o2 -> compareValues(o1, o2) }

fun <T : Comparable<T>> descendingComparator() = Comparator<T> { o1, o2 -> compareValues(o2, o1) }