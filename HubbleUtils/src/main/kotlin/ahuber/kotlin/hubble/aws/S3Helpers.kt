package ahuber.kotlin.hubble.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.AmazonS3URI
import com.amazonaws.services.s3.model.*
import com.amazonaws.util.StringInputStream
import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO

fun Regions.createS3Client(): AmazonS3 =
        //This code expects that you have AWS credentials set up per:
        // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html
        AmazonS3ClientBuilder.standard()
                .withRegion(this)
                .build()

//region uploadImage
fun LocalizedS3ObjectId.uploadImage(image: BufferedImage, client: AmazonS3? = null) =
    (client ?: region.createS3Client()).uploadImage(image, bucket, key)

fun AmazonS3.uploadImage(image: BufferedImage, bucketName: String, key: String): PutObjectResult {
    val bytes = ByteArrayOutputStream().let {
        ImageIO.write(image, "jpg", it)
        it.toByteArray()
    }
    return upload(ByteArrayInputStream(bytes), bucketName, key, "image/jpeg")
}
//endregion

//region uploadJson
fun LocalizedS3ObjectId.uploadJson(json: String, client: AmazonS3? = null) =
    (client ?: region.createS3Client()).uploadJson(json, bucket, key)

fun AmazonS3.uploadJson(json: String, bucketName: String, key: String) =
    uploadText(json, bucketName, key, "application/json")
//endregion

//region uploadText
fun LocalizedS3ObjectId.uploadText(text: String, contentType: String, client: AmazonS3? = null) =
    (client ?: region.createS3Client()).uploadText(text, bucket, key, contentType)

fun AmazonS3.uploadText(text: String, bucketName: String, key: String, contentType: String) =
    upload(StringInputStream(text), bucketName, key, contentType)
//endregion

//region upload
fun LocalizedS3ObjectId.upload(inputStream: InputStream, bucketName: String, key: String, contentType: String) =
    region.createS3Client().upload(inputStream, bucketName, key, contentType)

fun AmazonS3.upload(inputStream: InputStream, bucketName: String, key: String, contentType: String): PutObjectResult {
    val metadata = ObjectMetadata().apply {
        setContentType(contentType)
        addUserMetadata("x-amz-meta-title", key)
    }
    val request = PutObjectRequest(bucketName, key, inputStream, metadata)
    return putObject(request)
}
//endregion

//region download
fun LocalizedS3ObjectId.download(client: AmazonS3? = null) =
        (client ?: region.createS3Client()).download(bucket, key)

fun AmazonS3.download(uri: AmazonS3URI) = download(uri.bucket, uri.key, uri.key)

fun AmazonS3.download(bucketName: String, key: String, versionId: String? = null) =
        download(GetObjectRequest(bucketName, key, versionId))

fun AmazonS3.download(request: GetObjectRequest): S3Object = getObject(request)
//endregion

//region read

fun S3Object.getText(): String? = when (objectContent) {
    null -> null
    else -> InputStreamReader(objectContent).use { inputStreamReader ->
        BufferedReader(inputStreamReader).use { it.readText() }
    }
}

//endregion

fun createS3Uri(bucketName: String, key: String) = "s3://$bucketName/$key"

fun createHadoopLogFolderId(satelliteName: String) =
        LocalizedS3ObjectId(Regions.US_EAST_1, "ahuber-hadoop-logs", "kotlin/$satelliteName/")

fun createSparkJobConfigId(satelliteName: String) =
        LocalizedS3ObjectId(Regions.US_EAST_1, "ahuber-spark-job-configs", "kotlin/$satelliteName.json")

fun crateSparkJobJarId(jarPath: String) =
        LocalizedS3ObjectId(Regions.US_EAST_1, "danshi", jarPath)

fun createImageId(satelliteName: String) =
        LocalizedS3ObjectId(Regions.US_EAST_1, "ahuber-satellite-images", "kotlin/$satelliteName.jpg")