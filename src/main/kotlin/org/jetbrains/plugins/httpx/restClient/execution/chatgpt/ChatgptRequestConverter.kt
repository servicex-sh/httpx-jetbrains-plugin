package org.jetbrains.plugins.httpx.restClient.execution.chatgpt

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer


@Suppress("UnstableApiUsage")
class ChatgptRequestConverter : RequestConverter<ChatgptRequest>() {

    override val requestType: Class<ChatgptRequest> get() = ChatgptRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): ChatgptRequest {
        var url = ""
        var requestType = "CHATGPT" //7 chars
        var requestBody: String? = null
        lateinit var headers: Map<String, String>
        ApplicationManager.getApplication().runReadAction {
            val httpRequest = requestPsiPointer.element!!
            val httpMethod = httpRequest.httpMethod
            val schema = if (httpMethod.length > 7) {
                httpMethod.substring(7).toLowerCase()
            } else {
                httpRequest.requestTarget?.scheme?.text ?: "https"
            }
            url = httpRequest.getHttpUrl(substitutor)!!
            headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
            requestType = httpMethod
            requestBody = httpRequest.requestBody?.text
        }
        return ChatgptRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: ChatgptRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### ChatGPT request").append("\n")
        builder.append("CHATGPT ${request.URL}").append("\n")
        builder.append("\n");
        builder.append(request.textToSend ?: "")
        return builder.toString()
    }

}