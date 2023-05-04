package org.jetbrains.plugins.httpx.restClient.execution.chatgpt

import com.intellij.httpClient.execution.common.CommonClientRequest
import org.jetbrains.plugins.httpx.json.JsonUtils
import java.net.URI


@Suppress("UnstableApiUsage")
class ChatgptRequest(
    override val URL: String?,
    override val httpMethod: String,
    override val textToSend: String?,
    val headers: Map<String, String>
) :
    CommonClientRequest {
    val contentType: String
    val uri: URI
    val body: String

    init {
        uri = URI.create(URL!!)
        contentType = headers.getOrDefault("Content-Type", "text/markdown")
        body = textToSend ?: ""
    }

    fun getHeadValue(name: String): String? {
        for (header in headers) {
            if (header.key.toLowerCase() == name) {
                return header.value
            }
        }
        return null
    }

    fun bodyBytes(): ByteArray {
        val chatRequest = mutableMapOf<String, Any>()
        chatRequest["model"] = getHeadValue("X-Model") ?: "gpt-3.5-turbo"
        chatRequest["temperature"] = (getHeadValue("X-Temperature") ?: "1.0").toDouble()
        chatRequest["messages"] = convertBodyToMessages()
        return JsonUtils.objectMapper.writeValueAsBytes(chatRequest)
    }

    private fun convertBodyToMessages(): List<Map<String, Any>> {
        val messages = mutableListOf<Map<String, Any>>()
        var userMsgContent = body
        //system message
        val systemMsgPattern = Regex("(\\S.+\\n)*.+\\{\\.system}")
        systemMsgPattern.find(userMsgContent)?.let {
            it.groups[0]?.let { group ->
                val matchedText = group.value
                userMsgContent = userMsgContent.replace(matchedText, "").trim()
                val systemMsgContent = matchedText.replace("{.system}", "").trim()
                messages.add(mapOf("role" to "system", "content" to systemMsgContent))
            }
        }
        //assistant messages
        val assistantMessages = mutableListOf<Map<String, Any>>()
        val assistantMsgPattern = Regex("(\\S.+\\n)*.+\\{\\.assistant}")
        assistantMsgPattern.findAll(userMsgContent).forEach {
            val matchedText = it.value
            userMsgContent = userMsgContent.replace(matchedText, "").trim()
            val assistantMsgContent = matchedText.replace("{.assistant}", "").trim()
            assistantMessages.add(mapOf("role" to "assistant", "content" to assistantMsgContent))
        }
        // user message
        messages.add(mapOf("role" to "user", "content" to userMsgContent))
        // append assistant messages
        if (assistantMessages.isNotEmpty()) {
            messages.addAll(assistantMessages)
        }
        return messages
    }


}