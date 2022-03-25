package org.jetbrains.plugins.httpx.restClient.execution.aws

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer
import org.jetbrains.plugins.httpx.restClient.execution.common.getRequestURL


@Suppress("UnstableApiUsage")
class AwsRequestConverter : RequestConverter<AwsRequest>() {

    override val requestType: Class<AwsRequest> get() = AwsRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): AwsRequest {
        var url = ""
        var requestType = "AWS"
        var requestBody: String? = null
        lateinit var headers: Map<String, String>
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            val httpMethod = httpRequest.httpMethod
            url = getRequestURL(httpRequest, substitutor, "https")
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpMethod
            requestBody = httpRequest.requestBody?.text
        }
        return AwsRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: AwsRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### AWS request").append("\n")
        builder.append("AWS ${request.URL}").append("\n")
        builder.append("\n");
        builder.append(request.textToSend ?: "")
        return builder.toString()
    }

}