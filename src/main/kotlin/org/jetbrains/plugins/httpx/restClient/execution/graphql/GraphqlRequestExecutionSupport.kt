package org.jetbrains.plugins.httpx.restClient.execution.graphql

import com.intellij.httpClient.execution.common.RequestContext
import com.intellij.httpClient.execution.common.RequestConverter
import com.intellij.httpClient.execution.common.RequestExecutionSupport
import com.intellij.httpClient.execution.common.RequestHandler

@Suppress("UnstableApiUsage")
class GraphqlRequestExecutionSupport : RequestExecutionSupport<GraphqlRequest> {

    companion object {
        val GRAPHQL_METHODS = listOf("GRAPHQL", "GRAPHQLWS", "GRAPHQLWSS")
    }

    override fun canProcess(requestContext: RequestContext): Boolean {
        return GRAPHQL_METHODS.contains(requestContext.method)
    }

    override fun getRequestConverter(): RequestConverter<GraphqlRequest> {
        return GraphqlRequestConverter()
    }

    override fun getRequestHandler(): RequestHandler<GraphqlRequest> {
        return GraphqlRequestHandler()
    }

    override fun supportedMethods(): Collection<String> {
        return GRAPHQL_METHODS
    }
}