package org.jetbrains.plugins.httpx.codeInsight

import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.queryParameters
import org.jetbrains.plugins.httpx.json.JsonUtils
import org.jetbrains.plugins.httpx.restClient.execution.aliyun.Aliyun
import org.jetbrains.plugins.httpx.restClient.execution.aliyun.AliyunRequest
import org.jetbrains.plugins.httpx.restClient.execution.aliyun.Products
import org.jetbrains.plugins.httpx.restClient.execution.common.getRequestURL
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class AliyunRequestConvertToCliIntention : BaseElementAtCaretIntentionAction() {
    override fun getFamilyName(): String {
        return "Convert to aliyun cli and copy to clipboard"
    }

    override fun getText(): String {
        return this.familyName
    }

    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        val httpRequest = PsiTreeUtil.getParentOfType(element, HttpRequest::class.java)
        return httpRequest != null && "ALIYUN" == httpRequest.httpMethod
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val httpRequest = PsiTreeUtil.getParentOfType(element, HttpRequest::class.java)!!
        val substitutor = HttpRequestVariableSubstitutor.getDefault(project)
        val headers = httpRequest.headerFieldList.associate { it.name to it.getValue(substitutor) }
        val requestType = httpRequest.httpMethod
        val url = getRequestURL(httpRequest, substitutor, "https")
        val aliyunRequest = AliyunRequest(url, requestType, httpRequest.requestBody?.text, headers)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val testData = StringSelection(convertToAliyunCli(aliyunRequest))
        clipboard.setContents(testData, testData)
    }

    private fun convertToAliyunCli(aliyunRequest: AliyunRequest): String {
        val builder = StringBuilder()
        builder.append("aliyun ")
        val host = aliyunRequest.uri.host
        val queryParams = aliyunRequest.uri.queryParameters
        val product = Products.instance().findProductByHost(host)
        if (product != null) {
            builder.append(product.code.toLowerCase()).append(' ')
            builder.append(queryParams["Action"] ?: "UnknownAction").append(' ')
            val headers = aliyunRequest.headers
            if (headers.containsKey("X-Region-Id")) {
                builder.append("--region ").append(headers["X-Region-Id"]).append(' ')
            } else {
                for (globalRegion in Aliyun.GLOBAL_REGIONS) {
                    if (host.contains(globalRegion)) {
                        builder.append("--region ").append(globalRegion).append(' ')
                        break
                    }
                }
            }
            val bodyBytes = aliyunRequest.bodyBytes()
            if (bodyBytes.isNotEmpty()) {
                val params = JsonUtils.objectMapper.readValue<Map<String, Any>>(bodyBytes)
                for ((key, value) in params) { // sub_params
                    if (value is List<*>) {
                        value.forEachIndexed { index, item ->
                            if (item is Map<*, *>) {
                                item.forEach {
                                    val paramName = "${key}.${index + 1}.${it.key}"
                                    builder.append("--").append(paramName).append(' ').append(value.toString())
                                }
                            }
                        }
                    } else {
                        builder.append("--").append(key).append(' ').append(value.toString())
                    }
                }
            }
        }
        return builder.toString().trim()
    }
}