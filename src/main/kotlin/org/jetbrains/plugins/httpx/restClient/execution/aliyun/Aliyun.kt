package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import org.ini4j.Ini
import org.ini4j.Profile
import org.jetbrains.plugins.httpx.json.JsonUtils
import org.jetbrains.plugins.httpx.restClient.execution.common.CloudAccount
import java.nio.file.Path

object Aliyun {
    val GLOBAL_REGIONS = listOf(
        "cn-qingdao",
        "cn-beijing",
        "cn-zhangjiakou",
        "cn-huhehaote",
        "cn-hangzhou",
        "cn-shanghai",
        "cn-shenzhen",
        "cn-hongkong",
        "ap-southeast-1",
        "ap-southeast-2",
        "ap-southeast-3",
        "ap-southeast-4",
        "ap-southeast-5",
        "ap-southeast-6",
        "ap-south-1",
        "ap-northeast-1",
        "us-west-1",
        "us-east-1",
        "eu-central-1",
        "me-east-1",
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

    fun readAliyunAccessToken(keyIdAndSecret: List<String>?): CloudAccount? {
        var cloudAccount: CloudAccount?
        val keyIdAndSecret2: List<String>? = keyIdAndSecret
        if (keyIdAndSecret2 == null) { // read default profile
            cloudAccount = readAccessFromAliyunCli(null)
            if (cloudAccount == null) {
                cloudAccount = readAccessFromCredentials(null);
            }
        } else if (keyIdAndSecret2.size == 2 && keyIdAndSecret2[1].length <= 4) { // id match
            cloudAccount = readAccessFromAliyunCli(keyIdAndSecret2[0])
            if (cloudAccount == null) {
                cloudAccount = readAccessFromCredentials(keyIdAndSecret2[0])
            }
        } else {
            cloudAccount = CloudAccount().apply {
                accessKeyId = keyIdAndSecret2[0];
                accessKeyId = keyIdAndSecret2[1];
            }
        }
        return cloudAccount
    }


    private fun readAccessFromAliyunCli(partOfId: String?): CloudAccount? {
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
                    return CloudAccount(
                        profile["access_key_id"] as String,
                        profile["access_key_secret"] as String,
                        profile["region_id"] as String
                    )
                }
            }
        }
        return null
    }

    /**
     * read AK from $HOME/.alibabacloud/credentials.ini
     *
     * @param partOfId part of id
     * @return ak
     */
    private fun readAccessFromCredentials(partOfId: String?): CloudAccount? {
        val configPath = Path.of(System.getProperty("user.home"), ".alibabacloud", "credentials.ini")
        if (configPath.toFile().exists()) {
            try {
                val config = Ini(configPath.toFile())
                var profile: Profile.Section? = null
                if (partOfId != null) {
                    for (tempProfile in config.values) {
                        if (tempProfile.containsKey("access_key_id")) {
                            if (tempProfile["access_key_id"]!!.contains(partOfId)) {
                                profile = tempProfile
                                break
                            }
                        }
                    }
                } else {
                    profile = config["default"]
                }
                if (profile != null) {
                    return CloudAccount(profile["access_key_id"], profile["access_key_secret"], profile["region_id"])
                }
            } catch (ignore: Exception) {
            }
        }
        return null
    }
}