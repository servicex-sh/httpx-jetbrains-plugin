package org.jetbrains.plugins.httpx.restClient.execution.email

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer


@Suppress("UnstableApiUsage")
class MailRequestConverter : RequestConverter<MailRequest>() {

    override val requestType: Class<MailRequest> get() = MailRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): MailRequest {
        var url = ""
        var requestType = "MAIL"
        var requestBody: String? = null
        lateinit var headers: Map<String, String>
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            url = httpRequest.getHttpUrl(substitutor)!!
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpRequest.httpMethod
            requestBody = httpRequest.requestBody?.text
        }
        return MailRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: MailRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### mail request").append("\n")
        builder.append("MAIL ${request.URL}").append("\n")
        builder.append("\n");
        builder.append(request.textToSend ?: "")
        return builder.toString()
    }

}