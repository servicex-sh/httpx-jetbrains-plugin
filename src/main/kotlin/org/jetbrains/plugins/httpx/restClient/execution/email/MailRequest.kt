package org.jetbrains.plugins.httpx.restClient.execution.email

import com.intellij.httpClient.execution.common.CommonClientRequest
import java.net.URI
import java.net.URL


@Suppress("UnstableApiUsage")
class MailRequest(override val URL: String?, override val httpMethod: String?, override val textToSend: String?, private val headers: Map<String, String>) :
    CommonClientRequest {
    val contentType: String
    var smtpHost: String? = null
    var smtpPort: Int? = null
    var smtpSchema: String? = null
    var username: String? = null
    var password: String? = null
    var subject: String? = null
    var from: String? = null
    var to: String? = null
    var cc: String? = null
    var bcc: String? = null
    var replyTo: String? = null

    init {
        subject = headers["Subject"]
        from = headers["From"]
        replyTo = headers["Reply-To"]
        contentType = headers.getOrDefault("Content-Type", "text/plain").let {
            if (it.contains("charset")) {
                it
            } else {
                "${it}; charset=utf-8"
            }
        }
        val mailUrl = URL(URL)
        this.to = mailUrl.path
        val authorization = headers["Authorization"]
        if (authorization != null && authorization.startsWith("Basic")) {
            val pair = authorization.substring(6)
            val parts = pair.split(":")
            username = parts[0]
            password = parts[1]
        }
        val hostHeader = headers["Host"]
        if (hostHeader != null) {
            if (hostHeader.startsWith("ssl://") || hostHeader.startsWith("tls://")) {
                val smtpUri = URI.create(hostHeader)
                this.smtpHost = smtpUri.host
                this.smtpSchema = smtpUri.scheme
                if (smtpUri.port <= 0) {
                    this.smtpPort = 465
                } else {
                    this.smtpPort = smtpUri.port
                }
            } else if (hostHeader.contains(':')) {
                val parts = hostHeader.split(':')
                this.smtpHost = parts[0]
                this.smtpPort = parts[1].toInt()
            } else {
                this.smtpHost = hostHeader
                this.smtpPort = 25
            }
        }
    }

}