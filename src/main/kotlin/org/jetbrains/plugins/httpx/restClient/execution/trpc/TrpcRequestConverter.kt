package org.jetbrains.plugins.httpx.restClient.execution.trpc

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer


@Suppress("UnstableApiUsage")
class TrpcRequestConverter : RequestConverter<TrpcRequest>() {

    override val requestType: Class<TrpcRequest> get() = TrpcRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): TrpcRequest {
        var url = ""
        var requestType = "TRPC"
        var requestBody: String? = null
        lateinit var headers: Map<String, String>
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            url = httpRequest.getHttpUrl(substitutor)!!
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpRequest.httpMethod
            requestBody = httpRequest.requestBody?.text
        }
        return TrpcRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: TrpcRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### trpc request").append("\n")
        builder.append("TRPC ${request.URL}").append("\n")
        builder.append("Content-Type: application/json").append("\n")
        builder.append("\n")
        builder.append(request.textToSend ?: "")
        return builder.toString()
    }

}