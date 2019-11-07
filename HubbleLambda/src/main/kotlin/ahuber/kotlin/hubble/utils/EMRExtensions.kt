package ahuber.kotlin.hubble.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder
import com.amazonaws.services.elasticmapreduce.model.*
import com.amazonaws.services.elasticmapreduce.util.StepFactory
import java.util.*

const val TERMINATE_CLUSTER_ACTION = "TERMINATE_CLUSTER"

val String.region get() = Regions.values().firstOrNull { it.name == this }

var AmazonElasticMapReduceClientBuilder.correspondingRegion: Regions?
    get() = if (region == null) null else region.region
    set(value) {
        this.region = value?.name
    }

fun AmazonElasticMapReduce.runJobFlow(block: () -> RunJobFlowRequest): RunJobFlowResult = this.runJobFlow(block())

fun stepConfig(block: StepConfig.() -> Unit) = StepConfig().apply(block)

fun hadoopJarStep(block: HadoopJarStepConfig.() -> Unit) = HadoopJarStepConfig().apply(block)

fun AmazonElasticMapReduceClientBuilder.configure(block: AmazonElasticMapReduceClientBuilder.() -> Unit):
        AmazonElasticMapReduce {
    this.apply(block)
    return this.build()
}

fun application(block: Application.() -> Unit) = Application().apply(block)

fun jobFlowInstancesConfig(block: JobFlowInstancesConfig.() -> Unit) = JobFlowInstancesConfig().apply(block)

fun runJobFlowRequest(block: RunJobFlowRequest.() -> Unit) = RunJobFlowRequest().apply(block)

fun RunJobFlowRequest.withApplications(block: Adder<Application>.() -> Unit) {
    val list = LinkedList<Application>()
    val adder = object : Adder<Application> {
        override fun add(element: Application) {
            list.add(element)
        }
    }
    adder.block()
    setApplications(list)
}

fun RunJobFlowRequest.withSteps(block: Adder<StepConfig>.() -> Unit) {
    val list = LinkedList<StepConfig>()
    val adder = object : Adder<StepConfig> {
        override fun add(element: StepConfig) {
            list.add(element)
        }
    }
    adder.block()
    setSteps(list)
}

fun Adder<StepConfig>.withDebuggingEnabled(block: Adder<StepConfig>.() -> Unit) {
    add(stepConfig {
        name = "Enable Debugging"
        actionOnFailure = TERMINATE_CLUSTER_ACTION
        hadoopJarStep = StepFactory().newEnableDebuggingStep()
    })
    block()
}