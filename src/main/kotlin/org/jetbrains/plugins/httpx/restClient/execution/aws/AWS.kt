package org.jetbrains.plugins.httpx.restClient.execution.aws

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

object AWS {

    fun awsBasicCredentials(authorizationHeader: String?): AwsBasicCredentials? {
        val credential = readAwsAccessToken(authorizationHeader)
        return if (credential != null && credential.size > 1) {
            AwsBasicCredentials.create(credential[0], credential[1])
        } else null
    }

    private fun readAwsAccessToken(authHeader: String?): List<String?>? {
        var awsCredential: List<String?>? = null
        if (authHeader != null) {
            if (authHeader.startsWith("AWS ")) { // VS Code REST Client plugin
                awsCredential = authHeader.substring(4).trim { it <= ' ' }.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }
            } else if (authHeader.startsWith("Basic")) {
                return getBasicAuthorization(authHeader)
            }
        }
        if (awsCredential == null) { // read default profile
            awsCredential = readAccessFromAwsCli(null)
        } else if (awsCredential.size > 1 && awsCredential[0]!!.length <= 4) { // id match
            awsCredential = readAccessFromAwsCli(awsCredential[0])
        }
        return awsCredential
    }

    private fun getBasicAuthorization(header: String): List<String>? {
        if (header.startsWith("Basic ")) {
            var clearText = header.substring(6).trim()
            if (!(clearText.contains(' ') || clearText.contains(':'))) {
                clearText = String(Base64.getDecoder().decode(clearText), StandardCharsets.UTF_8)
            }
            return clearText.split("[:\\s]".toRegex())
        }
        return null
    }

    private fun readAccessFromAwsCli(partOfId: String?): List<String?>? {
        val awsCredentialsFile = Path.of(System.getProperty("user.home")).resolve(".aws").resolve("credentials").toAbsolutePath()
        if (awsCredentialsFile.toFile().exists()) {
            try {
                val lines = Files.readAllLines(awsCredentialsFile)
                val store: MutableMap<String, MutableMap<String, String>> = HashMap()
                var profileName = "default"
                for (line in lines) {
                    if (line.startsWith("[")) {
                        profileName = line.substring(1, line.indexOf(']'))
                    } else if (line.contains("=")) {
                        val parts = line.split("=".toRegex(), limit = 2).toTypedArray()
                        val profile = store.computeIfAbsent(profileName) { k: String? -> HashMap() }
                        profile[parts[0].trim { it <= ' ' }] = parts[1].trim { it <= ' ' }
                    }
                }
                if (partOfId == null && store.containsKey("default")) {
                    return extractAccessToken(store["default"]!!)
                } else if (partOfId != null && store.isNotEmpty()) {
                    for ((key, value) in store) {
                        val keyId: String? = value["aws_access_key_id"]
                        if (keyId != null && keyId.contains(partOfId)) {
                            return extractAccessToken(store[key]!!)
                        }
                    }
                }
            } catch (ignore: Exception) {
            }
        }
        return null
    }

    private fun extractAccessToken(profile: Map<String, String>): List<String?>? {
        return if (profile.containsKey("aws_access_key_id") && profile.containsKey("aws_access_key_id")) {
            java.util.List.of(profile["aws_access_key_id"], profile["aws_secret_access_key"])
        } else null
    }
}