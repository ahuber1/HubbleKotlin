package ahuber.kotlin.hubble.utils

import com.amazonaws.services.lambda.runtime.Context
import com.squareup.moshi.Moshi

inline val IntRange.middle get() = this.first + this.length / 2;

fun getHubbleLogger(context: Context? = null): HubbleLogger = when (val logger = context?.logger) {
    null -> object: HubbleLogger {
        override fun log(str: String) {
            print(str)
        }
    }
    else -> object: HubbleLogger {
        override fun log(str: String) {
            logger.log(str)
        }

    }
}

inline fun <reified T> Moshi.adapter() = this.adapter(T::class.java)!!