package org.jetbrains.plugins.httpx.restClient.execution.jsonrpc

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer


@Suppress("UnstableApiUsage")
class JsonRpcRequestConverter : RequestConverter<JsonRpcRequest>() {

    override val requestType: Class<JsonRpcRequest> get() = JsonRpcRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): JsonRpcRequest {
        var url = ""
        var schema = ""
        var requestType = "JSONRPC"
        var requestBody: String? = null
        lateinit var headers: Map<String, String>
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            schema = httpRequest.requestTarget?.scheme?.text ?: "jsonrpc"
            url = httpRequest.getHttpUrl(substitutor)!!
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpRequest.httpMethod
            requestBody = httpRequest.requestBody?.text
        }
        url = if (headers.containsKey("Host")) {
            "${schema}://${headers["Host"]}/${url.trim('/')}"
        } else {
            "${schema}://${url}"
        }
        return JsonRpcRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: JsonRpcRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### jsonrpc request").append("\n")
        builder.append("JSONRPC ${request.URL}").append("\n")
        builder.append("Content-Type: application/json").append("\n")
        builder.append("\n")
        builder.append(request.textToSend ?: "")
        return builder.toString()
    }

}