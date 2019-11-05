package ahuber.kotlin.hubble.utils

import com.amazonaws.services.lambda.runtime.Context

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