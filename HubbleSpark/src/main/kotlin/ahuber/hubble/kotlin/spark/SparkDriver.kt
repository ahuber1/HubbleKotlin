@file:JvmName("SparkDriver")

package ahuber.hubble.kotlin.spark

import ahuber.hubble.kotlin.utils.sparkSession
import ahuber.hubble.kotlin.utils.start
import ahuber.kotlin.hubble.aws.*
import ahuber.kotlin.hubble.utils.*
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3URI
import kotlinx.coroutines.runBlocking
import org.apache.spark.api.java.JavaRDD
import org.apache.spark.api.java.JavaSparkContext

fun main(args: Array<String>) {
    sparkSession { appName("Hubble_Kotlin_Spark") }.start {
        val context = JavaSparkContext(this.sparkContext())
        val jobConfiguration =
                extractSparkJobConfiguration(args) ?: throw RuntimeException(
                        "${className<SparkJobConfiguration>()} could not be parsed from JSON")
        val sortedData = parallelMergeSort(context, jobConfiguration)
        val image = sortedData.writeGreyscaleImage()
        Regions.US_EAST_1.createS3Client().uploadImage(image, "ahuber-satellite-images",
                "kotlin/${jobConfiguration.name}")
    }
}

private fun extractSparkJobConfiguration(args: Array<String>): SparkJobConfiguration? {
    require(args.isNotEmpty()) { "S3 URI for JSON configuration not specified" }
    val uri = AmazonS3URI(args.first())
    val region = args.elementAtOrNull(1)?.convertToRegion() ?: Regions.US_EAST_1
    region.createS3Client().download(uri).use {
        val json = it.getText() ?: return null
        return moshiKotlin.adapter<SparkJobConfiguration>().fromJson(json)
    }
}

private fun parallelMergeSort(context: JavaSparkContext, jobConfiguration: SparkJobConfiguration): Array<Int> {
    val unsortedData = jobConfiguration.data
    val threshold = jobConfiguration.threshold
    val end = unsortedData.size
    val middle = end / 2
    val leftHalf = unsortedData.sliceArray(0 until middle)
    val rightHalf = unsortedData.sliceArray(middle until end)
    val dataSet: JavaRDD<Array<Int>> = context.parallelize(listOf(leftHalf, rightHalf), 2)
    return dataSet.map {
        runBlocking {
            it.mergeSort(threshold)
        }
    }.reduce { array1, array2 -> (array1 mergeWith array2).toTypedArray()
    }
}