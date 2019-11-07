package ahuber.kotlin.hubble.aws

import ahuber.kotlin.hubble.Receiver
import ahuber.kotlin.hubble.Satellite
import ahuber.kotlin.hubble.SatelliteProcessor
import ahuber.kotlin.hubble.adt.Buffer
import ahuber.kotlin.hubble.utils.*
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.event.S3EventNotification
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.pow
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

class App : RequestHandler<S3Event, String> {
    private val satelliteName: String
    private val logFolderId: LocalizedS3ObjectId
    private val sparkJobConfigId: LocalizedS3ObjectId
    private val sparkJobJarId: LocalizedS3ObjectId
    private val sparkJobJarArgs: Array<String>

    init {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
        val id = UUID.randomUUID()
        val dateString = formatter.format(now)

        satelliteName = "${dateString}_${id}"
        logFolderId = createHadoopLogFolderId(satelliteName)
        sparkJobConfigId = createSparkJobConfigId(satelliteName)
        sparkJobJarId = crateSparkJobJarId("java/HubbleSpark-1.0.jar") // TODO Make sure project is named accordingly
        sparkJobJarArgs = arrayOf(sparkJobConfigId.stringUri, Regions.US_EAST_1.name)
    }

    override fun handleRequest(input: S3Event?, context: Context?): String {
        val hubbleLogger = getHubbleLogger(context)

        if (input == null) {
            return "No input was received"
        }

        val records = input.records ?: return "Records are null"

        if (records.isEmpty()) {
            return "No records present"
        }

        hubbleLogger.logLine("${className<S3Event>()} contains ${records.size} " +
                (if (records.size == 1) "event" else "events"))

        // Process each S3 Event
        val resultMapping = arrayOfNulls<String?>(records.size)
        val errorMapping = Array(records.size) { true }
        processRecords(records, resultMapping, errorMapping, hubbleLogger)

        // Construct output string
        val output = getOutput(resultMapping, errorMapping)
        hubbleLogger.logLine(output)
        val errorOccurred = errorMapping.any { it }

        if (errorOccurred) {
            exitProcess(-1)
        }

        return output
    }

    private fun getOutput(resultMapping: Array<String?>, errorMapping: Array<Boolean>): String {
        val builder = StringBuilder()

        for (index in resultMapping.indices) {
            val result = resultMapping[index]
            val error = errorMapping[index]

            builder.append("[$index]: ${(if (error) "ERROR" else "SUCCESS")}")
            builder.append(result)

            if (index + 1 >= resultMapping.size) {
                continue
            }

            builder.append('\n')
            builder.append('\n')
        }

        return builder.toString()
    }

    private fun processRecords(records: List<S3EventNotification.S3EventNotificationRecord?>,
            resultMapping: Array<String?>, errorMapping: Array<Boolean>, hubbleLogger: HubbleLogger) {

        for ((index, record) in records.withIndex()) {
            hubbleLogger.logLine("Processing ${className<S3EventNotification.S3EventNotificationRecord>()} ${(index + 1)} of ${records.size}")

            if (record == null) {
                resultMapping[index] = createUnableToGetMessage<S3EventNotification.S3EventNotificationRecord>()
                continue
            }

            val s3Entity = record.s3

            if (s3Entity == null) {
                resultMapping[index] = createUnableToGetMessage<S3EventNotification.S3Entity>()
            }

            val key = extractKey(s3Entity, index, resultMapping) ?: continue
            val bucket = extractBucket(s3Entity, index, resultMapping) ?: continue
            val result = processS3Entity(this, bucket, key, hubbleLogger)
            resultMapping[index] = result
            errorMapping[index] = result != null
        }
    }

    private fun extractKey(s3Entity: S3EventNotification.S3Entity?, index: Int,
            resultMapping: Array<String?>): String? {
        val objectEntity = s3Entity?.`object`

        if (objectEntity == null) {
            resultMapping[index] = createUnableToGetMessage<S3EventNotification.S3ObjectEntity>()
            return null
        }

        val key = objectEntity.key

        if (key == null) {
            resultMapping[index] = "Key was null. Skipping..."
            return null
        }

        if (!key.endsWith(".json")) {
            resultMapping[index] = "S3 Object with key '$key' does not end in '.json'. Skipping..."
            return null
        }

        return key
    }

    private fun extractBucket(s3Entity: S3EventNotification.S3Entity?, index: Int,
            resultMapping: Array<String?>): String? {
        val bucketEntity = s3Entity?.bucket

        if (bucketEntity == null) {
            resultMapping[index] = createUnableToGetMessage<S3EventNotification.S3BucketEntity>()
            return null
        }

        val bucket = bucketEntity.name

        if (bucket == null) {
            resultMapping[index] = "Bucket was null. Skipping..."
            return null
        }

        return bucket
    }

    companion object {
        private val EMR_REGION = Regions.US_EAST_1
        private const val SPARK_JOB_CLASS = "ahuber.hubble.spark.SparkDriver"

        private inline fun <reified T> createUnableToGetMessage() = "Unable to get ${className<T>()}"

        private fun process(app: App, configuration: SatelliteConfiguration, hubbleLogger: HubbleLogger): String? {
            val n = 2.0.pow(configuration.i).toInt()
            val t = 10.0.pow(configuration.j).toInt()
            val receiverThreshold = n.toDouble().pow(2).toInt()
            val bufferSize = receiverThreshold * 2

            hubbleLogger.logLine("Running simulation: '${app.satelliteName}', n: '$n', t: '$t', bufferSize: '$bufferSize', receiverThreshold: '$receiverThreshold'")

            // Create the buffer, satellite, processor, and receiver
            val buffer = Buffer<Int>(bufferSize)
            val satellite = Satellite(buffer)
            val processor = SatelliteProcessor(app.satelliteName, t, EMR_REGION, app.logFolderId, app.sparkJobConfigId,
                    app.sparkJobJarId, SPARK_JOB_CLASS, *app.sparkJobJarArgs)
            val receiver = Receiver(buffer, processor, receiverThreshold)

            // Create the jobs
            val elapsedMilliseconds = measureTimeMillis {
                val satelliteJob = GlobalScope.launch { satellite.run() }
                val processorJob = GlobalScope.launch { processor.run() }
                val receiverJob = GlobalScope.launch { receiver.run() }
                runBlocking { satelliteJob.join() }
                processorJob.cancel()
                receiverJob.cancel()
            }

            return String.format("Satellite has been shut down. Satellite ran for %,d milliseconds.", elapsedMilliseconds)
        }

        private fun processS3Entity(app: App, bucket: String, key: String, hubbleLogger: HubbleLogger): String? {
            hubbleLogger.logLine("Downloading S3 object located in bucket '$bucket' and that has key '$key'")
            val text = AmazonS3ClientBuilder.defaultClient().download(bucket, key).use { it.stringContent } ?: return null
            hubbleLogger.logLine("JSON has been read: $text")
            val jsonAdapter = moshiKotlin.adapter<SatelliteConfiguration>()
            val configuration = jsonAdapter.fromJson(text) ?: return null
            return process(app, configuration, hubbleLogger)
        }
    }
}