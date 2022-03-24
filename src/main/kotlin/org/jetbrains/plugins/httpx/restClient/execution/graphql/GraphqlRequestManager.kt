package org.jetbrains.plugins.httpx.restClient.execution.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import io.netty.buffer.Unpooled
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.plugins.httpx.json.JsonUtils.objectMapper
import org.jetbrains.plugins.httpx.restClient.execution.common.JsonBodyFileHint
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.WebsocketClientSpec
import reactor.netty.http.websocket.WebsocketInbound
import reactor.netty.http.websocket.WebsocketOutbound
import reactor.netty.tcp.SslProvider.SslContextSpec
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*

@Suppress("UnstableApiUsage")
class GraphqlRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun execute(graphqlRequest: GraphqlRequest): CommonClientResponse {
        val client = httpClient().headers { httpHeaders ->
            graphqlRequest.headers.entries.forEach {
                val name = it.key
                val value = it.value
                if (name == "Content-Type" && !value.contains("json")) {
                    httpHeaders.add(name, "application/json") // convert application/graphql to application/json
                } else {
                    httpHeaders.add(name, value)
                }
            }
        }
        val uri = graphqlRequest.uri
        if (uri.scheme.startsWith("http")) {
            return httpPost(client, uri, graphqlRequest);
        } else if (uri.scheme.startsWith("ws")) {
            return httpWebSocket(client, uri, graphqlRequest)
        }
        return GraphqlResponse(
            null, CommonClientResponseBody.Empty(),
            "ERROR", "please http or ws protocol!"
        )
    }


    private fun httpPost(httpClient: HttpClient, requestUri: URI, httpRequest: GraphqlRequest): CommonClientResponse {
        val responseReceiver = httpClient.post().uri(requestUri).send(Mono.just(Unpooled.wrappedBuffer(httpRequest.bodyBytes())))
        return responseReceiver.responseSingle { response, content ->
            content.asByteArray().map {
                val body = it.toString(StandardCharsets.UTF_8)
                GraphqlResponse(
                    response.responseHeaders(),
                    CommonClientResponseBody.Text(body, JsonBodyFileHint.jsonBodyFileHint("graphql-result.json")),
                    response.status().toString()
                )
            }
        }.block()!!
    }

    private fun httpWebSocket(httpClient: HttpClient, requestUri: URI, httpRequest: GraphqlRequest): CommonClientResponse {
        val shared = MutableSharedFlow<CommonClientResponseBody.TextStream.Message>(
            replay = 1000,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        var reactiveWebSocket: reactor.core.Disposable? = null
        val disposeWebSocket = Disposable {
            if (reactiveWebSocket != null && !reactiveWebSocket!!.isDisposed)
                reactiveWebSocket?.dispose()
        }
        val textStream = CommonClientResponseBody.TextStream(shared, JsonBodyFileHint.jsonBodyFileHint("graphql-result.json")).withConnectionDisposable(disposeWebSocket)
        val id = UUID.randomUUID().toString()
        reactiveWebSocket = httpClient
            .websocket(WebsocketClientSpec.builder().protocols("graphql-transport-ws").build())
            .uri(requestUri)
            .handle { inbound: WebsocketInbound, outbound: WebsocketOutbound ->
                Flux.create { fluxSink: FluxSink<String> ->
                    inbound.receive().asString()
                        .handle { responseJsonText: String?, sink: SynchronousSink<Any?> ->
                            try {
                                val response = objectMapper.readValue(responseJsonText, Map::class.java)
                                val type = response["type"] as String?
                                if ("connection_ack" == type) { //send query
                                    val queryBytes: ByteArray = graphqlWsMessage(objectMapper, "subscribe", id, httpRequest.bodyBytes())
                                    outbound.send(Mono.just(Unpooled.wrappedBuffer(queryBytes))).then().subscribe()
                                } else if ("next" == type) { //result received
                                    val payload = response["payload"]
                                    val jsonText: String = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload)
                                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.Chunk(jsonText + "\n\n"))
                                    fluxSink.next(jsonText)
                                } else if ("complete" == type) {  // query completed
                                    outbound.sendClose().subscribe()
                                    shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.End)
                                    sink.complete()
                                    fluxSink.complete()
                                }
                            } catch (e: java.lang.Exception) {
                                shared.tryEmit(CommonClientResponseBody.TextStream.Message.ConnectionClosed.WithError(e))
                                sink.error(e)
                                fluxSink.error(e)
                            }
                        }.subscribe()
                    val connectionInitBytes: ByteArray = graphqlWsMessage(objectMapper, "connection_init", null, null)
                    outbound.send(Flux.just(Unpooled.wrappedBuffer(connectionInitBytes))).then().subscribe()
                }
            }.subscribe()
        return GraphqlResponse(null, textStream)
    }

    private fun graphqlWsMessage(objectMapper: ObjectMapper, type: String, id: String?, payload: ByteArray?): ByteArray {
        val msg: MutableMap<String, Any> = HashMap()
        msg["type"] = type
        return try {
            if (id != null) {
                msg["id"] = id
            }
            if (payload != null) {
                msg["payload"] = objectMapper.readValue(payload, MutableMap::class.java)
            }
            objectMapper.writeValueAsBytes(msg)
        } catch (e: java.lang.Exception) {
            byteArrayOf()
        }
    }


    private fun httpClient(): HttpClient {
        return HttpClient.create().secure { sslContextSpec: SslContextSpec ->
            sslContextSpec.sslContext(SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build())
        }
    }


}