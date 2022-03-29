package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import org.jetbrains.plugins.httpx.json.JsonUtils
import java.nio.file.Path

object Aliyun {
    val GLOBAL_REGIONS = listOf(
        "me-east-1",
        "us-east-1",
        "ap-northeast-1",
        "ap-southeast-5",
        "cn-hongkong",
        "cn-shenzhen",
        "ap-southeast-3",
        "ap-southeast-2",
        "ap-south-1",
        "cn-huhehaote",
        "cn-qingdao",
        "cn-beijing",
        "cn-shanghai",
        "cn-hangzhou",
        "ap-southeast-1",
        "us-west-1",
        "eu-central-1",
        "cn-zhangjiakou",
        "cn-chengdu",
        "eu-west-1"
    )

    fun getServiceName(host: String): String {
        if (host.endsWith(".fc.aliyuncs.com")) {
            return "fc"
        }
        if (host.endsWith(".oas.aliyuncs.com")) {
            return "oas"
        }
        val serviceName = host.substring(0, host.indexOf("."))
        return if (serviceName.contains("-")) {
            if (serviceName.contains("r-kvstore")) {
                "redisa"
            } else if (serviceName.contains("domain-intl")) {
                "domain-intl"
            } else if (serviceName.contains("yundun-bastionhost")) {
                "yundun-bastionhost"
            } else if (serviceName.contains("httpdns-api")) {
                "httpdns"
            } else if (serviceName.contains("dms-enterprise")) {
                "dms-enterprise"
            } else {
                serviceName.substring(0, serviceName.indexOf('-'))
            }
        } else serviceName
    }

    fun readAliyunAccessToken(keyIdAndSecret: List<String>?): List<String>? {
        var keyIdAndSecret2: List<String>? = keyIdAndSecret
        if (keyIdAndSecret2 == null) { // read default profile
            keyIdAndSecret2 = readAccessFromAliyunCli(null)
        } else if (keyIdAndSecret2.size == 2 && keyIdAndSecret2[1].length <= 4) { // id match
            keyIdAndSecret2 = readAccessFromAliyunCli(keyIdAndSecret2[0])
        }
        return keyIdAndSecret2
    }


    private fun readAccessFromAliyunCli(partOfId: String?): List<String>? {
        val aliyunConfigJsonFile = Path.of(System.getProperty("user.home")).resolve(".aliyun").resolve("config.json").toAbsolutePath().toFile()
        if (aliyunConfigJsonFile.exists()) {
            val config = JsonUtils.objectMapper.readValue(aliyunConfigJsonFile, MutableMap::class.java)
            val profileName = config["current"] as String?
            val profiles = config["profiles"] as List<Map<String, Any>>?
            if (profileName != null && profiles != null) {
                val profile = profiles
                    .filter { profile ->
                        "AK" == profile["mode"] && profile.containsKey("access_key_id") && profile.containsKey("access_key_secret")
                    }.firstOrNull() { profile ->
                        if (partOfId != null) {
                            (profile["access_key_id"] as String).contains(partOfId)
                        } else {
                            profileName == profile["name"]
                        }
                    }
                if (profile != null) {
                    return listOf(profile["access_key_id"] as String, profile["access_key_secret"] as String)
                }
            }
        }
        return null
    }
}