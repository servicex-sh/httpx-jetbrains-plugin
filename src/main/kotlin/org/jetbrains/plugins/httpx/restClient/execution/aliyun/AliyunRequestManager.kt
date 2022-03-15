package org.jetbrains.plugins.httpx.restClient.execution.aliyun

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
import reactor.netty.tcp.SslProvider.SslContextSpec
import java.net.URI
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
class AliyunRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun execute(graphqlRequest: AliyunRequest): CommonClientResponse {
        val client = httpClient()
        val uri = graphqlRequest.uri
        return httpPost(client, uri, graphqlRequest);
    }


    private fun httpPost(httpClient: HttpClient, requestUri: URI, httpRequest: AliyunRequest): CommonClientResponse {
        val responseReceiver = httpClient.post().uri(requestUri).send(Mono.just(Unpooled.wrappedBuffer(httpRequest.bodyBytes())))
        return responseReceiver.responseSingle { response, content ->
            content.asByteArray().map {
                val body = it.toString(StandardCharsets.UTF_8)
                AliyunResponse(
                    response.responseHeaders(),
                    CommonClientResponseBody.Text(body, JsonBodyFileHint.jsonBodyFileHint("graphql-result.json")),
                    response.status().toString()
                )
            }
        }.block()!!
    }


    private fun httpClient(): HttpClient {
        return HttpClient.create().secure { sslContextSpec: SslContextSpec ->
            sslContextSpec.sslContext(SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build())
        }
    }


}