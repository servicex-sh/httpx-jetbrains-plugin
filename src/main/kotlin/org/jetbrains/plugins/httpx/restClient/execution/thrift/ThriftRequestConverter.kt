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
        var requestType = "THRIFT" // 6 chars
        var requestBody: String? = null
        lateinit var headers: Map<String, String>
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            val httpMethod = httpRequest.httpMethod
            val schema = if (httpMethod.length > 6) {
                httpMethod.substring(6).toLowerCase()
            } else {
                httpRequest.requestTarget?.scheme?.text ?: "thrift"
            }
            url = "${schema}://" + httpRequest.getHttpUrl(substitutor)!!
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpRequest.httpMethod
            requestBody = httpRequest.requestBody?.text
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