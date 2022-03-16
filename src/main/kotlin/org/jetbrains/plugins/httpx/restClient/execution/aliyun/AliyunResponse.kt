package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.json.JsonFileType
import com.intellij.openapi.fileTypes.FileType
import io.netty.handler.codec.http.HttpHeaders

@Suppress("UnstableApiUsage")
class AliyunResponse(
    private val headers: Map<String,String>?,
    private val responseBody: CommonClientResponseBody = CommonClientResponseBody.Empty(),
    private val status: String = "OK",
    private val error: String? = null,
    override var executionTime: Long? = 0
) : CommonClientResponse {
    override val body: CommonClientResponseBody
        get() = responseBody

    override fun suggestFileTypeForPresentation(): FileType? {
        return JsonFileType.INSTANCE
    }

    override val statusPresentation: String
        get() = status

    override val presentationHeader: String
        get() {
            val hint = if (status.contains("OK")) {
                "ALIYUN ${status}"
            } else {
                "ALIYUN ERROR\n${(error ?: "")}"
            }
            val lines = mutableListOf(hint)
            headers?.forEach {
                lines.add("${it.key}: ${it.value}")
            }
            return lines.joinToString("\n", postfix = "\n")
        }

    override val presentationFooter: String
        get() {
            return "Execution time: $executionTime ms"
        }
}
