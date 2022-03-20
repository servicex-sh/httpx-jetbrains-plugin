package org.jetbrains.plugins.httpx.restClient.execution.memcache

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer


@Suppress("UnstableApiUsage")
class MemcacheRequestConverter : RequestConverter<MemcacheRequest>() {

    override val requestType: Class<MemcacheRequest> get() = MemcacheRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): MemcacheRequest {
        var key = ""
        var requestType = "MEMCACHE"
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
            "localhost:11211"
        }
        val requestLine = "${uri}/${key}"
        return MemcacheRequest(requestLine, requestType, requestBody, key, headers)
    }

    override fun toExternalFormInner(request: MemcacheRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### memcache request").append("\n")
        builder.append("MEMCACHE ${request.key}").append("\n")
        builder.append("URI: ${request.uri}").append("\n")
        builder.append("\n")
        return builder.toString()
    }

}