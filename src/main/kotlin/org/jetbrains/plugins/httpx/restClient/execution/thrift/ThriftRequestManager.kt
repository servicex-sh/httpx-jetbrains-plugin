package org.jetbrains.plugins.httpx.restClient.execution.thrift

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.httpx.restClient.execution.common.JsonBodyFileHint

@Suppress("UnstableApiUsage")
class ThriftRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun requestResponse(thriftRequest: ThriftRequest): CommonClientResponse {
        try {
            //todo implement thrift call
            val body = "{}"
            return ThriftResponse(CommonClientResponseBody.Text(body, JsonBodyFileHint.jsonBodyFileHint("thrift-result.json")))
        } catch (e: Exception) {
            return ThriftResponse(CommonClientResponseBody.Empty(), "Error", e.message)
        }
    }

}