package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import com.aliyuncs.CommonRequest
import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.IAcsClient
import com.aliyuncs.profile.DefaultProfile
import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.util.queryParameters
import org.jetbrains.plugins.httpx.json.JsonUtils
import org.jetbrains.plugins.httpx.restClient.execution.common.CloudAccount
import org.jetbrains.plugins.httpx.restClient.execution.common.JsonBodyFileHint
import org.jetbrains.plugins.httpx.restClient.execution.common.XmlBodyFileHint

@Suppress("UnstableApiUsage")
class AliyunRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun execute(aliyunRequest: AliyunRequest): CommonClientResponse {
        val cloudAccount: CloudAccount? = Aliyun.readAliyunAccessToken(aliyunRequest.getBasicAuthorization())
        if (cloudAccount == null) {
            return AliyunResponse(null, CommonClientResponseBody.Empty(), "Error", "No Aliyun AK found!")
        }
        val host: String = aliyunRequest.uri.host
        val regionId = aliyunRequest.regionId ?: cloudAccount.regionId
        val profile = DefaultProfile.getProfile(
            regionId,
            cloudAccount.accessKeyId,
            cloudAccount.accessKeySecret
        )
        val queries = aliyunRequest.uri.queryParameters
        val client: IAcsClient = DefaultAcsClient(profile)
        val request = CommonRequest()
        request.sysDomain = host
        request.sysAction = queries["Action"]
        val version = if (queries.containsKey("Version")) {
            queries["Version"]
        } else {
            Products.instance().findProductByHost(host)?.version
        }
        request.sysVersion = version
        var format: String? = "JSON"
        if (queries.containsKey("Format")) {
            format = queries["Format"]!!
        } else {
            val acceptHeader: String? = aliyunRequest.headers["Accept"]
            if (acceptHeader != null && acceptHeader.contains("xml")) {
                format = "XML"
            }
        }
        request.putQueryParameter("Format", format)
        val bodyBytes: ByteArray = aliyunRequest.bodyBytes()
        if (bodyBytes.isNotEmpty()) {
            val requestData = JsonUtils.objectMapper.readValue(bodyBytes, Map::class.java)
            for ((key, value) in requestData) { // sub_params
                if (value is List<*>) {
                    value.forEachIndexed { index, item ->
                        if (item is Map<*, *>) {
                            item.forEach {
                                val paramName = "${key}[${index}].${it.key}"
                                request.putQueryParameter(paramName, it.value.toString())
                            }
                        }
                    }
                } else {
                    request.putQueryParameter(key.toString(), value.toString())
                }
            }
        }
        val response = client.getCommonResponse(request)
        val sysHeaders = response.httpResponse.sysHeaders
        val text = response.data
        val bodyFileHint = if (format == "JSON") {
            JsonBodyFileHint.jsonBodyFileHint("aliyun-result.json")
        } else {
            XmlBodyFileHint.xmlBodyFileHint("aliyun-result.xml")
        }
        return AliyunResponse(sysHeaders, CommonClientResponseBody.Text(text, bodyFileHint))
    }

}