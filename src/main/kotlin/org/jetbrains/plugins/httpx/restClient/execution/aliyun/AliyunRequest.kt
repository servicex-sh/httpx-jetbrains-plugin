package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import com.intellij.httpClient.execution.common.CommonClientRequest
import com.intellij.util.queryParameters
import java.net.URI


@Suppress("UnstableApiUsage")
class AliyunRequest(override val URL: String?, override val httpMethod: String, override val textToSend: String?, val headers: Map<String, String>) :
    CommonClientRequest {
    val uri: URI
    val regionId: String
    val format: String
    val params: Map<String, String>

    init {
        uri = URI.create(URL!!)
        params = uri.queryParameters
        format = if (params.containsKey("Format")) {
            params["Format"]!!
        } else {
            val acceptHeader = headers.getOrDefault("Accept", "application/json")
            if (acceptHeader.contains("xml")) {
                "XML"
            } else {
                "JSON"
            }
        }
        //resolve region id from X-Region-Id header or host name
        var tempRegionId: String? = headers["X-Region-Id"]
        if (tempRegionId == null) { //resolve region id from host
            tempRegionId = uri.host.replace(".aliyuncs.com", "")
            if (tempRegionId.contains('.')) {
                tempRegionId = tempRegionId.substring(tempRegionId.indexOf('.') + 1)
            } else if (tempRegionId.contains('-')) {
                tempRegionId = tempRegionId.substring(tempRegionId.indexOf('-') + 1)
            }
        }
        regionId = tempRegionId
    }

    fun bodyBytes(): ByteArray {
        return textToSend?.encodeToByteArray() ?: byteArrayOf()
    }
}