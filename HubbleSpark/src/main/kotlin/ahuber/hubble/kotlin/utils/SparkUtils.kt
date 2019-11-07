package ahuber.hubble.kotlin.utils

import org.apache.spark.sql.SparkSession

inline fun sparkSession(block: SparkSession.Builder.() -> Unit) : SparkSession {
    val builder = SparkSession.builder()
    builder.block()

    // Kotlin misinterpreted this method. It is not a traditional getter,
    // meaning the property "orCreate" is syntactically odd.
    @Suppress("UsePropertyAccessSyntax")
    return builder.getOrCreate();
}

inline fun <T> SparkSession.start(block: SparkSession.() -> T) : T {
    val result = this.block()
    this.stop()
    return result
}