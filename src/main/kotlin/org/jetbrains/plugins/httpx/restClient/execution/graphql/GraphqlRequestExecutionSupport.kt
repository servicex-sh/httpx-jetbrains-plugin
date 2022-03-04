package org.jetbrains.plugins.httpx.restClient.execution.graphql

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class GraphqlRequestExecutionSupport : RequestExecutionSupport<GraphqlRequest> {

    override fun canProcess(requestContext: RequestContext): Boolean {
        return requestContext.method.startsWith("GRAPHQL")
    }

    override fun getRequestConverter(): RequestConverter<GraphqlRequest> {
        return GraphqlRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<GraphqlRequest> {
        return GraphqlRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return listOf("GRAPHQL", "GRAPHQLWS", "GRAPHQLWSS")
    }
}