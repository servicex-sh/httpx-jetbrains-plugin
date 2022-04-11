package org.jetbrains.plugins.httpx.restClient.execution.jsonrpc

import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import io.netty.buffer.Unpooled
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.jetbrains.plugins.httpx.json.JsonUtils
import org.jetbrains.plugins.httpx.restClient.execution.common.JsonBodyFileHint
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.SslProvider
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.URI
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
class JsonRpcRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun requestResponse(request: JsonRpcRequest): CommonClientResponse {
        try {
            val jsonRpcUri = request.uri
            var functionName = jsonRpcUri.path.substring(1)
            if (functionName.contains("/")) {
                functionName = functionName.substring(functionName.lastIndexOf('/') + 1)
            }
            val jsonRpcRequest = mutableMapOf<String, Any>()
            jsonRpcRequest["jsonrpc"] = "2.0"
            jsonRpcRequest["method"] = functionName
            jsonRpcRequest["id"] = 0
            var params: Any? = null
            var body = request.jsonArrayBodyWithArgsHeaders()
            if (body.isNotEmpty()) {
                if (!body.startsWith("{")) {
                    if (!body.startsWith("[")) {
                        body = "[$body]"
                    }
                    params = JsonUtils.objectMapper.readValue<List<Any>>(body)
                } else {
                    params = JsonUtils.objectMapper.readValue<Map<String, Any>>(body)
                }
            }
            if (params != null) {
                jsonRpcRequest["params"] = params
            }
            if (jsonRpcUri.scheme.startsWith("http")) {
                return jsonRpcOverHttp(jsonRpcUri, request, jsonRpcRequest)
            } else {
                return jsonRpcOverTcp(jsonRpcUri, request, jsonRpcRequest)
            }
        } catch (e: Exception) {
            return JsonRpcResponse(null, CommonClientResponseBody.Empty(), "Error", e.message)
        }
    }

    private fun jsonRpcOverHttp(requestUri: URI, httpRequest: JsonRpcRequest, jsonRpcRequest: Map<String, Any>): CommonClientResponse {
        val client = httpClient().headers { httpHeaders ->
            httpRequest.headers.entries.forEach {
                val name = it.key
                val value = it.value
                httpHeaders.add(name, value)
            }
        }
        val responseReceiver = client.post().uri(requestUri).send(Mono.just(Unpooled.wrappedBuffer(JsonUtils.objectMapper.writeValueAsBytes(jsonRpcRequest))))
        return responseReceiver.responseSingle { response, content ->
            content.asByteArray().map {
                val body = it.toString(StandardCharsets.UTF_8)
                JsonRpcResponse(
                    response.responseHeaders(),
                    CommonClientResponseBody.Text(body, JsonBodyFileHint.jsonBodyFileHint("jsonrpc-result.json")),
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

    private fun jsonRpcOverTcp(jsonRpcUri: URI, httpRequest: JsonRpcRequest, jsonRpcRequest: Map<String, Any>): CommonClientResponse {
        val content = JsonUtils.objectMapper.writeValueAsBytes(jsonRpcRequest)
        SocketChannel.open(InetSocketAddress(jsonRpcUri.host, jsonRpcUri.port)).use { socketChannel ->
            socketChannel.write(ByteBuffer.wrap(content))
            val data = extractData(socketChannel)
            val resultJson = String(data!!, StandardCharsets.UTF_8)
            return JsonRpcResponse(null, CommonClientResponseBody.Text(resultJson, JsonBodyFileHint.jsonBodyFileHint("graphql-result.json")))
        }
    }

    private fun extractData(socketChannel: SocketChannel): ByteArray? {
        val bos = ByteArrayOutputStream()
        val buf = ByteBuffer.allocate(4096)
        var readCount: Int
        do {
            readCount = socketChannel.read(buf)
            bos.write(buf.array(), 0, readCount)
        } while (readCount == 4096)
        return bos.toByteArray()
    }

}