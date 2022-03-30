package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import com.intellij.httpClient.execution.common.CommonClientRequest
import com.intellij.util.queryParameters
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*


@Suppress("UnstableApiUsage")
class AliyunRequest(override val URL: String?, override val httpMethod: String, override val textToSend: String?, val headers: Map<String, String>) :
    CommonClientRequest {
    val uri: URI
    var regionId: String? = null
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
            if (tempRegionId.contains('-')) {
                for (globalRegion in Aliyun.GLOBAL_REGIONS) {
                    if (tempRegionId.contains(globalRegion)) {
                        regionId = globalRegion
                        break
                    }
                }
            }
        } else {
            regionId = tempRegionId
        }
    }

    fun bodyBytes(): ByteArray {
        return textToSend?.encodeToByteArray() ?: byteArrayOf()
    }

    fun getBasicAuthorization(): List<String>? {
        val header = headers["Authorization"]
        if (header != null && header.startsWith("Basic ")) {
            var clearText = header.substring(6).trim()
            if (!(clearText.contains(' ') || clearText.contains(':'))) {
                clearText = String(Base64.getDecoder().decode(clearText), StandardCharsets.UTF_8)
            }
            return clearText.split("[:\\s]".toRegex())
        }
        return null
    }

}