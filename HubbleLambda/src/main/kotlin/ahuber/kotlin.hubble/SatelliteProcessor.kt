package ahuber.kotlin.hubble

import ahuber.kotlin.hubble.aws.LocalizedS3ObjectId
import ahuber.kotlin.hubble.aws.SparkJobConfiguration
import ahuber.kotlin.hubble.aws.stringUri
import ahuber.kotlin.hubble.aws.uploadJson
import ahuber.kotlin.hubble.utils.*
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder
import com.amazonaws.services.elasticmapreduce.model.*
import com.amazonaws.services.s3.model.PutObjectResult
import java.util.concurrent.Semaphore

class SatelliteProcessor(private val satelliteName: String,
        private val threshold: Int,
        private val emrRegion: Regions,
        private val logFolderLocation: LocalizedS3ObjectId,
        private val sparkJobConfigurationLocation: LocalizedS3ObjectId,
        private val sparkJobJarLocation: LocalizedS3ObjectId,
        private val sparkJobClass: String,
        vararg sparkJobJarArgs: String) : Processor<Array<Int>>, Runnable {

    private val sparkJobJarArgs: Array<String> = sparkJobJarArgs.toList().toTypedArray()
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
        val configuration = SparkJobConfiguration(satelliteName, threshold, data)
        uploadToAmazonS3(configuration, sparkJobConfigurationLocation)
        println("$configuration was successfully converted to JSON and uploaded to Amazon S3.")
        val result =
                startHadoopCluster(emrRegion, logFolderLocation, sparkJobJarLocation, sparkJobClass, sparkJobJarArgs)
        println("An Amazon ECR job request was submitted and was approved. Job Flow Id is ${result.jobFlowId}.")
        semaphore.release()
    }

    companion object {
        private fun uploadToAmazonS3(configuration: SparkJobConfiguration,
                location: LocalizedS3ObjectId): PutObjectResult {
            val adapter = moshiKotlin.adapter<SparkJobConfiguration>()
            val json = adapter.toJson(configuration)!!
            return location.uploadJson(json)
        }

        private fun startHadoopCluster(emrRegion: Regions, logLocationId: LocalizedS3ObjectId,
                sparkJobJarLocation: LocalizedS3ObjectId, sparkJobClass: String,
                sparkJobJarArgs: Array<String>): RunJobFlowResult {

            val emr = with(ProfileCredentialsProvider("default").credentials) {
                AmazonElasticMapReduceClientBuilder.standard().build {
                    this.credentials = AWSStaticCredentialsProvider(this@with)
                    this.correspondingRegion = emrRegion
                }
            }

            val allArgs = run {
                with(arrayOf("spark-submit", "--deploy-mode", "cluster", "--executor-memory", "1g", "--class",
                        sparkJobClass, sparkJobJarLocation.stringUri)) {
                    combineArrays(this, sparkJobJarArgs)
                }
            }

            return emr.runJobFlow {
                name = "Spark Cluster"
                releaseLabel = "emr-5.27.0"
                logUri = logLocationId.stringUri
                serviceRole = "EMR_DefaultRole"
                jobFlowRole = "EMR_EC2_DefaultRole"
                instances {
                    instanceCount = 3
                    keepJobFlowAliveWhenNoSteps = false
                    masterInstanceType = "m5.xlarge"
                    slaveInstanceType = "m5.xlarge"
                }
                applications {
                    yieldElement {
                        Application().configure { name = "Spark" }
                    }
                }
                steps {
                    withDebuggingEnabled {
                        StepConfig().configure {
                            name = "Process Data"
                            actionOnFailure = TERMINATE_CLUSTER_ACTION
                            hadoopJarStep = HadoopJarStepConfig().configure {
                                jar = "command-runner.jar"
                                args {
                                    yieldAll(allArgs)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
