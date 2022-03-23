package org.jetbrains.plugins.httpx.restClient.execution.ssh

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer

@Suppress("UnstableApiUsage")
class SSHRequestConverter : RequestConverter<SSHRequest>() {

    override val requestType: Class<SSHRequest> get() = SSHRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): SSHRequest {
        var url = ""
        val requestType = "SSH" // 6 chars
        var requestBody: String? = null
        lateinit var headers: Map<String, String>
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            url = "ssh://" + httpRequest.getHttpUrl(substitutor)!!
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestBody = httpRequest.requestBody?.text
        }
        return SSHRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: SSHRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### SSH request").append("\n")
        builder.append("SSH ${request.uri.host}").append("\n")
        builder.append("\n");
        builder.append(request.textToSend ?: "")
        return builder.toString()
    }

}