package org.jetbrains.plugins.httpx.restClient.execution.redis

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.json.JsonFileType
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType

@Suppress("UnstableApiUsage")
class RedisResponse(
    private val responseBody: CommonClientResponseBody = CommonClientResponseBody.Empty(),
    private val status: String = "OK",
    private val error: String? = null,
    override var executionTime: Long? = 0
) : CommonClientResponse {
    override val body: CommonClientResponseBody
        get() = responseBody

    override fun suggestFileTypeForPresentation(): FileType? {
        return PlainTextFileType.INSTANCE
    }

    override val statusPresentation: String
        get() = status

    override val presentationHeader: String
        get() {
            return if (status == "OK") {
                "REDIS 200 OK\n"
            } else {
                "REDIS ERROR\n${(error ?: "")}\n"
            }
        }

    override val presentationFooter: String
        get() {
            return "Execution time: $executionTime ms"
        }
}
