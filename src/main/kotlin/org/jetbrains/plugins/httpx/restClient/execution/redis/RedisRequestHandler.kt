package org.jetbrains.plugins.httpx.restClient.execution.redis

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class RedisRequestHandler : RequestHandler<RedisRequest> {

    override fun execute(request: RedisRequest, runContext: RunContext): CommonClientResponse {
        val redisRequestManager = runContext.project.getService(RedisRequestManager::class.java)
        return redisRequestManager.requestResponse(request)
    }

    override fun prepareExecutionEnvironment(request: RedisRequest, runContext: RunContext) {

    }
}