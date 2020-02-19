package ahuber.kotlin.hubble.utils

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder
import com.amazonaws.services.elasticmapreduce.model.*
import com.amazonaws.services.elasticmapreduce.util.StepFactory

const val TERMINATE_CLUSTER_ACTION = "TERMINATE_CLUSTER"

var AmazonElasticMapReduceClientBuilder.correspondingRegion: Regions?
    get() = if (region == null) null else region.convertToRegion()
    set(value) {
        this.region = value?.name
    }

inline fun <BuilderType, TypeToBuild> BuilderType.build(block: BuilderType.() -> Unit) : TypeToBuild
        where BuilderType : AwsClientBuilder<*, TypeToBuild> {

    this.apply(block)
    return this.build()
}

inline fun AmazonElasticMapReduce.runJobFlow(block: RunJobFlowRequest.() -> Unit): RunJobFlowResult =
        this.runJobFlow(RunJobFlowRequest().apply(block))

inline operator fun JobFlowInstancesConfig.invoke(function: JobFlowInstancesConfig.() -> Unit) = this.function()

suspend inline fun SequenceScope<StepConfig>.withDebuggingEnabled(block: SequenceScope<StepConfig>.() -> Unit) {

    this.yieldElement {
        StepConfig().configure {
            name = "Enable Debugging"
            actionOnFailure = TERMINATE_CLUSTER_ACTION
            hadoopJarStep = StepFactory().newEnableDebuggingStep()
        }
    }
    this.block()
}