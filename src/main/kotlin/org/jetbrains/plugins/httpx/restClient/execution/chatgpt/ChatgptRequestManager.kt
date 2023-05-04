package org.jetbrains.plugins.httpx.restClient.execution.chatgpt

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
class ChatgptRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun execute(chatgptRequest: ChatgptRequest): CommonClientResponse {
        val client = httpClient().headers { httpHeaders ->
            chatgptRequest.headers.entries.forEach {
                val name = it.key
                val value = it.value
                if (name == "Content-Type" && !value.contains("json")) {
                    httpHeaders.add(name, "application/json")
                } else if (!name.startsWith("X-")) {
                    httpHeaders.add(name, value)
                }
            }
            if (!chatgptRequest.headers.contains("Content-Type")) {
                httpHeaders.add("Content-Type", "application/json")
            }
            if (!chatgptRequest.headers.contains("Authorization")) {
                val openAIToken =
                    chatgptRequest.headers.getOrDefault("X-OPENAI-API-KEY", chatgptRequest.headers["X-OPENAI_API_KEY"])
                        ?: System.getenv("OPENAI_API_KEY")
                httpHeaders.add("Authorization", "Bearer $openAIToken")
            }
        }
        return httpPost(client, chatgptRequest.uri, chatgptRequest);

    }

    private fun httpPost(httpClient: HttpClient, requestUri: URI, httpRequest: ChatgptRequest): CommonClientResponse {
        val responseReceiver =
            httpClient.post().uri(requestUri).send(Mono.just(Unpooled.wrappedBuffer(httpRequest.bodyBytes())))
        return responseReceiver.responseSingle { response, content ->
            content.asByteArray().map {
                val body = it.toString(StandardCharsets.UTF_8)
                ChatgptResponse(
                    response.responseHeaders(),
                    CommonClientResponseBody.Text(body, JsonBodyFileHint.jsonBodyFileHint("chatgpt-result.json")),
                    body,
                    response.status().toString()
                )
            }
        }.block()!!
    }

    private fun httpClient(): HttpClient {
        return HttpClient.create().secure { sslContextSpec: SslContextSpec ->
            sslContextSpec.sslContext(
                SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build()
            )
        }
    }


}