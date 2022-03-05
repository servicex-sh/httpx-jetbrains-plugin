package org.jetbrains.plugins.httpx.restClient.execution.graphql

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.RequestHandler
import com.intellij.httpClient.execution.common.RunContext

@Suppress("UnstableApiUsage")
class GraphqlRequestHandler : RequestHandler<GraphqlRequest> {

    override fun execute(request: GraphqlRequest, runContext: RunContext): CommonClientResponse {
        val graphqlRequestManager = runContext.project.getService(GraphqlRequestManager::class.java)
        return graphqlRequestManager.execute(request)
    }

    override fun prepareExecutionEnvironment(request: GraphqlRequest, runContext: RunContext) {

    }
}