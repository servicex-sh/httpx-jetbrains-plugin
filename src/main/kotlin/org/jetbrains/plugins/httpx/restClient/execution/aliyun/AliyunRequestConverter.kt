package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer


@Suppress("UnstableApiUsage")
class AliyunRequestConverter : RequestConverter<AliyunRequest>() {

    override val requestType: Class<AliyunRequest> get() = AliyunRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): AliyunRequest {
        var url = ""
        var requestType = "ALIYUN"
        var requestBody: String? = null
        lateinit var headers: Map<String, String>
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            val httpMethod = httpRequest.httpMethod
            url = httpRequest.getHttpUrl(substitutor)!!
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpMethod
            requestBody = httpRequest.requestBody?.text
        }
        return AliyunRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: AliyunRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### aliyun request").append("\n")
        builder.append("ALIYUN ${request.URL}").append("\n")
        builder.append("\n");
        builder.append(request.textToSend ?: "")
        return builder.toString()
    }

}