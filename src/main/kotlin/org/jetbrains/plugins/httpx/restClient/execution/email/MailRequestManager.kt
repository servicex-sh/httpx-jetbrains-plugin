package org.jetbrains.plugins.httpx.restClient.execution.email

import com.intellij.httpClient.execution.common.CommonClientResponse
import com.intellij.httpClient.execution.common.CommonClientResponseBody
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Suppress("UnstableApiUsage")
class MailRequestManager(private val project: Project) : Disposable {

    override fun dispose() {
    }

    fun requestResponse(mailRequest: MailRequest): CommonClientResponse {
        val prop = Properties()
        prop["mail.transport.protocol"] = "smtp"
        prop["mail.smtp.host"] = mailRequest.smtpHost
        prop["mail.smtp.port"] = mailRequest.smtpPort
        prop["mail.smtp.socketFactory.port"] = mailRequest.smtpPort
        prop["mail.smtp.starttls.enable"] = "true"
        if (mailRequest.smtpSchema != null) {
            if (mailRequest.smtpSchema == "ssl") {
                prop["mail.smtp.socketFactory.class"] = "com.sun.mail.util.MailSSLSocketFactory"
                prop["mail.smtp.socketFactory.fallback"] = "false"
            }
        }
        var authenticator: Authenticator? = null
        if (mailRequest.username != null && mailRequest.password != null) {
            prop["mail.smtp.auth"] = "true"
            authenticator = object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(mailRequest.username, mailRequest.password)
                }
            }
        }
        try {
            val session: Session = Session.getInstance(prop, authenticator)
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(mailRequest.from))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailRequest.to))
            if (mailRequest.cc != null) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(mailRequest.cc))
            }
            if (mailRequest.bcc != null) {
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(mailRequest.bcc))
            }
            message.setSubject(mailRequest.subject, "UTF-8")
            val body: String = mailRequest.textToSend!!
            if (mailRequest.contentType.startsWith("text/html")) {
                message.setContent(body, mailRequest.contentType )
            } else {
                message.setText(body,"utf-8")
            }
            Transport.send(message)
            return MailResponse()
        } catch (e: Exception) {
            return MailResponse(CommonClientResponseBody.Empty(), "Error", e.message)
        }
    }

}