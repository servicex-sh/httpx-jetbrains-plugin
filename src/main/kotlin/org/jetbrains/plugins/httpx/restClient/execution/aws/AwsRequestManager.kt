package org.jetbrains.plugins.httpx.restClient.execution.aws

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.jetbrains.plugins.httpx.restClient.execution.common.JsonBodyFileHint
import org.jetbrains.plugins.httpx.restClient.execution.common.XmlBodyFileHint
import org.jetbrains.plugins.httpx.restClient.execution.publish.PublishResponse
import software.amazon.awssdk.auth.signer.Aws4Signer
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams
import software.amazon.awssdk.http.SdkHttpFullRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.regions.Region
import java.io.ByteArrayInputStream

@Suppress("UnstableApiUsage")
class AwsRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun execute(awsRequest: AwsRequest): CommonClientResponse {
        val requestUri = awsRequest.uri
        val host = requestUri.host
        val serviceName = host.substring(0, host.indexOf('.'))
        val awsBasicCredentials = AWS.awsBasicCredentials(awsRequest.getHeader("Authorization"))
        if (awsBasicCredentials == null) {
            return PublishResponse(CommonClientResponseBody.Empty(), "Error", "Cannot find AWS AK info, please check Authorization header!")
        }
        val method = awsRequest.getHttpRequestMethod()
        val requestBuilder = SdkHttpFullRequest.builder()
            .uri(requestUri)
            .method(SdkHttpMethod.valueOf(method))
        val headers: Map<String, String> = awsRequest.headers
        headers.forEach { (name: String, value: String?) ->
            if (!name.equals("Authorization", ignoreCase = true)) {
                requestBuilder.putHeader(name, value)
            }
        }
        if (!headers.containsKey("Host")) {
            requestBuilder.putHeader("Host", host)
        }
        // set default format: json
        if (!headers.containsKey("Accept")) {
            requestBuilder.putHeader("Accept", "application/json")
        }
        // set body
        val bodyBytes = awsRequest.bodyBytes()
        if (bodyBytes.isNotEmpty()) {
            requestBuilder.contentStreamProvider { ByteArrayInputStream(bodyBytes) }
        }
        val aws4SignerParams = Aws4SignerParams.builder()
            .awsCredentials(awsBasicCredentials)
            .signingRegion(Region.of(awsRequest.regionId))
            .signingName(serviceName)
            .build()

        val signedRequest = Aws4Signer.create().sign(requestBuilder.build(), aws4SignerParams)

        val okhttpRequestBuilder = if (method == "GET") {
            Request.Builder().get().url(requestUri.toURL())
        } else {
            val requestBody = RequestBody.create(MediaType.get(awsRequest.getHeader("Content-Type")!!), bodyBytes)
            Request.Builder().post(requestBody).url(requestUri.toURL())
        }
        signedRequest.headers().forEach { (name: String?, values: List<String?>) ->
            okhttpRequestBuilder.header(name, values[0])
        }
        OkHttpClient().newCall(okhttpRequestBuilder.build()).execute().use { response ->
            val responseHeaders = mutableMapOf<String, String>()
            var contentType = "application/json"
            response.headers().toMultimap().forEach { (name, values) ->
                responseHeaders[name] = values[0]
                if (name == "Content-Type") {
                    contentType = values[0]
                }
            }
            val bodyFileHint = if (contentType.contains("json")) {
                JsonBodyFileHint.jsonBodyFileHint("aws-result.json")
            } else {
                XmlBodyFileHint.xmlBodyFileHint("aws-result.xml")
            }
            return AwsResponse(responseHeaders, CommonClientResponseBody.Text(response.body().toString(), bodyFileHint))
        }
    }
}