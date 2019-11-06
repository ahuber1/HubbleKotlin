package ahuber.kotlin.hubble

import com.amazonaws.regions.Regions
import java.util.concurrent.Semaphore

//
class SatelliteProcessor(private val satelliteName: String,
                         private val threshold: Int,
                         private val emrRegion: Regions,
                         private val logFolderLocation: LocalizedS3ObjectId,
                         private val sparkJobConfigurationLocation: LocalizedS3ObjectId,
                         private val sparkJobJarLocation: LocalizedS3ObjectId,
                         private val sparkJobClass: String,
                         vararg sparkJobJarArgs: String) : Processor<Array<Int>>, Runnable {
    private val sparkJobJarArgs: Array<out String> = sparkJobJarArgs
    private val semaphore = Semaphore(1)

    init {
        semaphore.acquireUninterruptibly()
    }

    override fun run() {
        try {
            semaphore.acquire()
        } finally {
            semaphore.release()
        }
    }

    override fun onReceived(data: Array<Int>) {
        val configuration = SparkJobConfiguration(satelliteName, threshold, data.toIntArray())
        uploadToAmazonS3(configuration, sparkJobConfigurationLocation)
        println("$configuration was successfully converted to JSON and uploaded to Amazon S3.")
    }

    companion object {
        private fun uploadToAmazonS3(configuration: SparkJobConfiguration, location: LocalizedS3ObjectId): PutObjectResult {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter<SparkJobConfiguration>()
            val json = adapter.toJson(configuration)!!
            return location.uploadJson(json)
        }

        private fun startHadoopCluster(emrRegion: Regions, logLocationId: LocalizedS3ObjectId,
                                       sparkJobJarLocation: LocalizedS3ObjectId, sparkJobClass: String,
                                       vararg sparkJobJarArgs: String) {
            val credentials = ProfileCredentialsProvider("default").credentials
            val emr = AmazonElasticMapReduceClientBuilder.standard()
                    .withCredentials(AWSStaticCredentialsProvider(credentials))
                    .withRegion(emrRegion)
                    .build()
            val commandRunnerArgs = arrayOf("spark-submit", "--deploy-mode", "cluster", "--executor-memory", "1g", "--class", sparkJobClass)
        }
    }
}