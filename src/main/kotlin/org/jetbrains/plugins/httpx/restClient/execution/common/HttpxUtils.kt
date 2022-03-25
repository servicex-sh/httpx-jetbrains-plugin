package org.jetbrains.plugins.httpx.restClient.execution.common

import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.httpClient.http.request.psi.impl.HttpRequestPsiImplUtil
import com.intellij.util.io.URLUtil
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun getRequestURL(httpRequest: HttpRequest, substitutor: HttpRequestVariableSubstitutor, defaultSchema: String): String {
    var url = httpRequest.getHttpUrl(substitutor)!!
    val requestTarget = httpRequest.requestTarget!!
    if (!URLUtil.containsScheme(url)) {
        val scheme = requestTarget.scheme
        url = if (scheme != null) {
            "${scheme.text}://${url}"
        } else {
            "${defaultSchema}://${url}"
        }
    }
    if (!url.contains('?')) {
        val httpQuery = requestTarget.query
        if (httpQuery != null) {
            val queryParameterList = httpQuery.queryParameterList
            if (queryParameterList.isNotEmpty()) {
                val params = mutableListOf<String>()
                queryParameterList.forEach {
                    val paramName = it.queryParameterKey.text
                    if (paramName != null) {
                        val value = HttpRequestPsiImplUtil.getValue(it, substitutor)
                        if (value != null) {
                            params.add(paramName + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8))
                        }
                    }
                }
                url = "${url}?${params.joinToString("&")}"
            }
        }
    }
    return url
}