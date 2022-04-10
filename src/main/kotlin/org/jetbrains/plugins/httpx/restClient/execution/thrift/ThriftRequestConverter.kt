package org.jetbrains.plugins.httpx.restClient.execution.thrift

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer


@Suppress("UnstableApiUsage")
class ThriftRequestConverter : RequestConverter<ThriftRequest>() {

    override val requestType: Class<ThriftRequest> get() = ThriftRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): ThriftRequest {
        var url = ""
        var requestType = "THRIFT"
        var requestBody: String? = null
        lateinit var headers: Map<String, String>
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            url = httpRequest.getHttpUrl(substitutor)!!
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpRequest.httpMethod
            requestBody = httpRequest.requestBody?.text
        }
        url = if (headers.containsKey("Host")) {
            "thrift://${headers["Host"]}/${url.trim('/')}"
        } else {
            "thrift://${url}"
        }
        return ThriftRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: ThriftRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### thrift request").append("\n")
        builder.append("THRIFT ${request.URL}").append("\n")
        builder.append("Content-Type: application/json").append("\n")
        builder.append("\n");
        builder.append(request.textToSend ?: "")
        return builder.toString()
    }

}