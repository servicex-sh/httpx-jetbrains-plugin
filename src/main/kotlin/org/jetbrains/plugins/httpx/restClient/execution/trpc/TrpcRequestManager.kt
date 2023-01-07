package org.jetbrains.plugins.httpx.restClient.execution.trpc

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import io.netty.buffer.Unpooled
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.jetbrains.plugins.httpx.restClient.execution.common.JsonBodyFileHint
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.SslProvider
import java.net.URI
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
class TrpcRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun requestResponse(request: TrpcRequest): CommonClientResponse {
        try {
            val requestUri = request.getRealUri()
            return trpcOverHttp(requestUri, request)
        } catch (e: Exception) {
            return TrpcResponse(null, CommonClientResponseBody.Empty(), "Error", e.message)
        }
    }

    private fun trpcOverHttp(requestUri: URI, httpRequest: TrpcRequest): CommonClientResponse {
        val client = httpClient().headers { httpHeaders ->
            httpRequest.headers.entries.forEach {
                val name = it.key
                val value = it.value
                httpHeaders.add(name, value)
            }
        }
        val httpMethod = httpRequest.getRealHttpMethod()
        val responseReceiver = if (httpMethod == "POST") {
            client.post().uri(requestUri).send(Mono.just(Unpooled.wrappedBuffer(httpRequest.body.toByteArray())))
        } else {
            client.get().uri(requestUri)
        }
        return responseReceiver.responseSingle { response, content ->
            content.asByteArray().map {
                val body = it.toString(StandardCharsets.UTF_8)
                TrpcResponse(
                    response.responseHeaders(),
                    CommonClientResponseBody.Text(body, JsonBodyFileHint.jsonBodyFileHint("trpc-result.json")),
                    response.status().toString()
                )
            }
        }.block()!!
    }

    private fun httpClient(): HttpClient {
        return HttpClient.create().secure { sslContextSpec: SslProvider.SslContextSpec ->
            sslContextSpec.sslContext(SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build())
        }
    }

}