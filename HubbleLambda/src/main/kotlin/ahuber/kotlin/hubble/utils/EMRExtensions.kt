package ahuber.kotlin.hubble.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder
import com.amazonaws.services.elasticmapreduce.model.*
import com.amazonaws.services.elasticmapreduce.util.StepFactory
import java.util.*

const val TERMINATE_CLUSTER_ACTION = "TERMINATE_CLUSTER"

var AmazonElasticMapReduceClientBuilder.correspondingRegion: Regions?
    get() = if (region == null) null else region.convertToRegion()
    set(value) {
        this.region = value?.name
    }

inline fun AmazonElasticMapReduce.runJobFlow(block: () -> RunJobFlowRequest): RunJobFlowResult = this.runJobFlow(block())

inline fun stepConfig(block: StepConfig.() -> Unit) = StepConfig().apply(block)

inline fun hadoopJarStep(block: HadoopJarStepConfig.() -> Unit) = HadoopJarStepConfig().apply(block)

inline fun AmazonElasticMapReduceClientBuilder.configure(block: AmazonElasticMapReduceClientBuilder.() -> Unit):
        AmazonElasticMapReduce {
    this.apply(block)
    return this.build()
}

inline fun application(block: Application.() -> Unit) = Application().apply(block)

inline fun jobFlowInstancesConfig(block: JobFlowInstancesConfig.() -> Unit) = JobFlowInstancesConfig().apply(block)

inline fun runJobFlowRequest(block: RunJobFlowRequest.() -> Unit) = RunJobFlowRequest().apply(block)

inline fun RunJobFlowRequest.applications(block: Adder<Application>.() -> Unit) {
    val list = LinkedList<Application>()
    val adder = list.createAdder()
    adder.block()
    setApplications(list)
}

inline fun RunJobFlowRequest.steps(block: Adder<StepConfig>.() -> Unit) {
    val list = LinkedList<StepConfig>()
    val adder = list.createAdder()
    adder.block()
    setSteps(list)
}

inline fun Adder<StepConfig>.withDebuggingEnabled(block: Adder<StepConfig>.() -> Unit) {
    add {
        stepConfig {
            name = "Enable Debugging"
            actionOnFailure = TERMINATE_CLUSTER_ACTION
            hadoopJarStep = StepFactory().newEnableDebuggingStep()
        }
    }
    block()
}

inline fun HadoopJarStepConfig.args(block: Adder<String>.() -> Unit) {
    val list = LinkedList<String>()
    val adder = list.createAdder()
    adder.block()
    setArgs(list)
}