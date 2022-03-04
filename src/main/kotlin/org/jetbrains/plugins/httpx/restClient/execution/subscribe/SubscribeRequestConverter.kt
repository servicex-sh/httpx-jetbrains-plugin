package org.jetbrains.plugins.httpx.restClient.execution.subscribe

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer

@Suppress("UnstableApiUsage")
class SubscribeRequestConverter : RequestConverter<SubscribeRequest>() {

    override val requestType: Class<SubscribeRequest> get() = SubscribeRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): SubscribeRequest {
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
        return SubscribeRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: SubscribeRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### subscribe request").append("\n")
        builder.append("SUB ${request.topic}").append("\n")
        builder.append("Host: ${request.uri}").append("\n")
        builder.append("\n")
        return builder.toString()
    }

}