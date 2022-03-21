package org.jetbrains.plugins.httpx.restClient.execution.redis

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer
import org.jetbrains.plugins.httpx.restClient.execution.memcache.MemcacheRequest


@Suppress("UnstableApiUsage")
class RedisRequestConverter : RequestConverter<RedisRequest>() {

    override val requestType: Class<RedisRequest> get() = RedisRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): RedisRequest {
        var key = ""
        var requestType = "RSET"
        var requestBody: String? = null
        lateinit var headers: Map<String, String>
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            key = httpRequest.getHttpUrl(substitutor)!!
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpRequest.httpMethod
            requestBody = httpRequest.requestBody?.text
        }
        val uri = if (headers.containsKey("URI")) {
            headers["URI"]
        } else if (headers.containsKey("Host")) {
            headers["Host"]
        } else {
            "localhost:6379"
        }
        val requestLine = "${uri}/${key}"
        return RedisRequest(requestLine, requestType, requestBody, key, headers)
    }

    override fun toExternalFormInner(request: RedisRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### Redis request").append("\n")
        builder.append("${request.httpMethod} ${request.key}").append("\n")
        builder.append("HOST: ${request.uri}").append("\n")
        builder.append("\n")
        return builder.toString()
    }

}