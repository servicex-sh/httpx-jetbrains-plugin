package org.jetbrains.plugins.httpx.restClient.execution.msgpack

import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer


@Suppress("UnstableApiUsage")
class MsgpackRequestConverter : RequestConverter<MsgpackRequest>() {

    override val requestType: Class<MsgpackRequest> get() = MsgpackRequest::class.java

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): MsgpackRequest {
        var url = ""
        var requestType = "MSGPACK"
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
            "msgpack://${headers["Host"]}/${url.trim('/')}"
        } else {
            "msgpack://${url}"
        }
        return MsgpackRequest(url, requestType, requestBody, headers)
    }

    override fun toExternalFormInner(request: MsgpackRequest, fileName: String?): String {
        val builder = StringBuilder()
        builder.append("### msgpack request").append("\n")
        builder.append("MSGPACK ${request.URL}").append("\n")
        builder.append("Content-Type: application/json").append("\n")
        builder.append("\n")
        builder.append(request.textToSend ?: "")
        return builder.toString()
    }

}