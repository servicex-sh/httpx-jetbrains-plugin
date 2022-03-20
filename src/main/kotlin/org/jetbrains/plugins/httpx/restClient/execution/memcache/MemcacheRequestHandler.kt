package org.jetbrains.plugins.httpx.restClient.execution.memcache

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class MemcacheRequestHandler : RequestHandler<MemcacheRequest> {

    override fun execute(request: MemcacheRequest, runContext: RunContext): CommonClientResponse {
        val memcacheRequestManager = runContext.project.getService(MemcacheRequestManager::class.java)
        return memcacheRequestManager.requestResponse(request)
    }

    override fun prepareExecutionEnvironment(request: MemcacheRequest, runContext: RunContext) {

    }
}