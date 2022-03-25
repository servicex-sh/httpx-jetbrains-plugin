package org.jetbrains.plugins.httpx.restClient.execution.aws

import com.intellij.httpClient.execution.common.CommonClientRequest
import java.net.URI


@Suppress("UnstableApiUsage")
class AwsRequest(override val URL: String?, override val httpMethod: String, override val textToSend: String?, val headers: Map<String, String>) :
    CommonClientRequest {
    val uri: URI
    val regionId: String

    init {
        uri = URI.create(URL!!)
        //resolve region id from X-Region-Id header or host name
        var tempRegionId: String? = headers["X-Region-Id"]
        if (tempRegionId == null) { //resolve region id from host
            tempRegionId = uri.host.replace(".amazonaws.com", "")
            if (tempRegionId.contains('.')) {
                tempRegionId = tempRegionId.substring(tempRegionId.indexOf('.') + 1)
            }
            if (!tempRegionId.contains('-')) { // illegal regionId, read from default CLI config
                tempRegionId = AWS.readDefaultRegionFromCLI()
            }
        }
        regionId = if (tempRegionId != null && tempRegionId.contains('-')) {
            tempRegionId
        } else {
            "us-east-1"
        }
    }

    fun bodyBytes(): ByteArray {
        return textToSend?.encodeToByteArray() ?: byteArrayOf()
    }

    fun getHttpRequestMethod(): String {
        return if (httpMethod == "AWS") {
            "GET"
        } else {
            return httpMethod.substring(3)
        }
    }

    fun getHeader(name: String): String? {
        return headers[name]
    }

}