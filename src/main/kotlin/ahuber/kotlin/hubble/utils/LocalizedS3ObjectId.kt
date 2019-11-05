package ahuber.kotlin.hubble.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3URI
import com.amazonaws.services.s3.model.S3ObjectId
import com.amazonaws.services.s3.model.S3ObjectIdBuilder
import java.util.*

class LocalizedS3ObjectId: S3ObjectId {
    val region: Regions

    constructor(region: Regions, bucket: String, key: String, versionId: String? = null): super(bucket, key, versionId) {
        this.region = region
    }

    constructor(region: Regions, builder: S3ObjectIdBuilder): super(builder) {
        this.region = region
    }

    constructor(id: LocalizedS3ObjectId): this(id.region, id.bucket, id.key, id.versionId)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is LocalizedS3ObjectId) {
            return false
        }

        return region == other.region && bucket == other.bucket && key == other.key && versionId == other.versionId
    }

    override fun hashCode(): Int = Objects.hash(region, bucket, key, versionId)
}

val LocalizedS3ObjectId.stringUri get() = createS3Uri(bucket, key)
val LocalizedS3ObjectId.uri get() = AmazonS3URI(stringUri)