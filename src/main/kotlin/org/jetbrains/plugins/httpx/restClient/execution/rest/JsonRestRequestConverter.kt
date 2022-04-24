package org.jetbrains.plugins.httpx.restClient.execution.rest

import com.intellij.httpClient.execution.RestClientRequest
import com.intellij.httpClient.execution.impl.HttpRequestConverter
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.SmartPsiElementPointer
import org.jetbrains.plugins.httpx.json.JsonUtils


@Suppress("UnstableApiUsage")
class JsonRestRequestConverter : HttpRequestConverter() {

    override fun psiToCommonRequest(requestPsiPointer: SmartPsiElementPointer<HttpRequest>, substitutor: HttpRequestVariableSubstitutor): RestClientRequest {
        val request = super.psiToCommonRequest(requestPsiPointer, substitutor)
        ApplicationManager.getApplication().runReadAction {
            request.httpMethod = request.getHeaderValue("X-Method", "POST")
            if (!request.urlBase.contains("://")) {
                val schema = requestPsiPointer.element!!.requestTarget?.scheme?.text ?: "http"
                request.urlBase = "${schema}://${request.urlBase}"
            }
            val contentType = request.getHeaderValue("Content-Type", "application/json");
            val httpHeaders = request.headers
            val jsonObjectType = httpHeaders.any { it.key == "X-Body-Name" }
            if (jsonObjectType) { // convert to json object
                val builder = StringBuilder()
                builder.append("{")
                httpHeaders.filter { it.key.startsWith("X-Args-") }
                    .forEach {
                        val name = it.key.substring(7)
                        val value = it.value
                        builder.append('"').append(name).append('"').append(':').append(JsonUtils.wrapJsonValue(value)).append(",")
                    }
                val bodyName = request.getHeaderValue("X-Body-Name", "")
                builder.append('"').append(bodyName).append('"').append(":")
                if (contentType.contains("json")) {
                    builder.append(request.textToSend)
                } else {
                    builder.append(JsonUtils.convertToDoubleQuoteString(request.textToSend))
                }
                builder.append("}")
                request.textToSend = builder.toString()
            } else {
                val jsonArrayType = httpHeaders.any { it.key.startsWith("X-Args-") }
                if (jsonArrayType) { // convert to array
                    val argsHeaders = httpHeaders.filter { it.key.startsWith("X-Args-") }
                    if (argsHeaders.isNotEmpty()) {
                        val newBody = if (contentType.contains("json")) {
                            request.textToSend
                        } else {
                            JsonUtils.convertToDoubleQuoteString(request.textToSend)
                        }
                        val argLines = mutableListOf<String>()
                        for (i in 0..argsHeaders.size) {
                            val key = "X-Args-$i"
                            val headerValue = request.getHeaderValue(key, "")
                            if (headerValue.isNotEmpty()) {
                                argLines.add(JsonUtils.wrapJsonValue(headerValue))
                            } else {
                                argLines.add(newBody)
                            }
                        }
                        request.textToSend = "[" + java.lang.String.join(",", argLines) + "]"
                    }
                }
            }
            request.headers.add(RestClientRequest.KeyValuePair("Content-Type", "application/json"))
            request.headers.removeIf { it.key.startsWith("X-Args-") || it.key.startsWith("X-Body-Name") }
        }
        return request
    }

}