package org.jetbrains.plugins.httpx.restClient.execution.publish

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType

@Suppress("UnstableApiUsage")
class PublishResponse(
    private val responseBody: CommonClientResponseBody = CommonClientResponseBody.Empty(),
    private val status: String = "OK",
    private val error: String? = null,
    private val msgId: String? = null,
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
                if (msgId == null) {
                    "PUB 200 OK\n"
                } else {
                    "PUB 200 OK with ID ${msgId}\n"
                }
            } else {
                "PUB ERROR\n${(error ?: "")}\n"
            }
        }

    override val presentationFooter: String
        get() {
            return "Execution time: $executionTime ms"
        }
}
