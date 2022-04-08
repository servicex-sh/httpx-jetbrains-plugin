package org.jetbrains.plugins.httpx.restClient.execution.tarpc

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer


@Suppress("UnstableApiUsage")
class TarpcRequestConverter : RequestConverter<TarpcRequest>() {

    override val requestType: Class<TarpcRequest> get() = TarpcRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): TarpcRequest {
        var url = ""
        var requestType = "TARPC"
        var requestBody: String? = null
        lateinit var headers: Map<String, String>
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            url = "tarpc://" + httpRequest.getHttpUrl(substitutor)!!
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpRequest.httpMethod
            requestBody = httpRequest.requestBody?.text
        }
        return TarpcRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: TarpcRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### tarpc request").append("\n")
        builder.append("TARPC ${request.URL}").append("\n")
        builder.append("Content-Type: application/json").append("\n")
        builder.append("\n")
        builder.append(request.textToSend ?: "")
        return builder.toString()
    }

}