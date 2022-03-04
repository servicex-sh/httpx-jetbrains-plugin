package org.jetbrains.plugins.httpx.restClient.execution.publish

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer


@Suppress("UnstableApiUsage")
class PublishRequestConverter : RequestConverter<PublishRequest>() {

    override val requestType: Class<PublishRequest> get() = PublishRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): PublishRequest {
        var url = ""
        var requestType = "PUB"
        var requestBody: String? = null
        lateinit var headers: Map<String, String>
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            url = httpRequest.getHttpUrl(substitutor)!!
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpRequest.httpMethod
            requestBody = httpRequest.requestBody?.text
        }
        return PublishRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: PublishRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### publish request").append("\n")
        builder.append("PUB ${request.topic}").append("\n")
        builder.append("Host: ${request.uri}").append("\n")
        builder.append("Content-Type: ${request.contentType}").append("\n")
        builder.append("\n");
        builder.append(request.textToSend ?: "")
        return builder.toString()
    }

}