package ahuber.kotlin.hubble.utils

interface HubbleLogger {
    fun log(str: String = "")

    fun logLine(str: String = "") {
        log(str + "\n");
    }

    fun log(format: String, vararg args: Any) {
        log(String.format(format, args))
    }

    fun logLine(format: String, vararg args: Any) {
        logLine(String.format(format, *args))
    }
}