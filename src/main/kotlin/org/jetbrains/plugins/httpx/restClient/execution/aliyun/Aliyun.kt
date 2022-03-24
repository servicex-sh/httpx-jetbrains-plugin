package org.jetbrains.plugins.httpx.restClient.execution.aliyun

import org.jetbrains.plugins.httpx.json.JsonUtils
import java.nio.file.Path

object Aliyun {
    private val API_VERSIONS = mutableMapOf<String, String>()

    fun getApiVersion(productCode: String?): String? {
        if (API_VERSIONS.isEmpty()) {
            API_VERSIONS["domain"] = "2018-01-29"
            API_VERSIONS["cdn"] = "2018-05-10"
            API_VERSIONS["ram"] = "2015-05-01"
            API_VERSIONS["cbn"] = "2017-09-12"
            API_VERSIONS["drds"] = "2019-01-23"
            API_VERSIONS["emr"] = "2016-04-08"
            API_VERSIONS["sts"] = "2015-04-01"
            API_VERSIONS["cs"] = "2015-12-15"
            API_VERSIONS["cr"] = "2018-12-01"
            API_VERSIONS["hbase"] = "2019-01-01"
            API_VERSIONS["ros"] = "2019-09-10"
            API_VERSIONS["ess"] = "2018-08-28"
            API_VERSIONS["gpdb"] = "2016-05-03"
            API_VERSIONS["dds"] = "2015-12-01"
            API_VERSIONS["mongodb"] = "2015-12-01"
            API_VERSIONS["cloudauth"] = "2020-06-18"
            API_VERSIONS["live"] = "2016-11-01"
            API_VERSIONS["hpc"] = "2018-04-12"
            API_VERSIONS["ehpc"] = "2018-04-12"
            API_VERSIONS["ddos"] = "2017-05-18"
            API_VERSIONS["ddosbasic"] = "2017-05-18"
            API_VERSIONS["ddospro"] = "2017-05-18"
            API_VERSIONS["antiddos"] = "2017-05-18"
            API_VERSIONS["ddosbgp"] = "2018-07-20"
            API_VERSIONS["dm"] = "2015-11-23"
            API_VERSIONS["domain-intl"] = "2018-01-29"
            API_VERSIONS["cloudwf"] = "2017-12-07"
            API_VERSIONS["ecs"] = "2014-05-26"
            API_VERSIONS["ecs-cn-hangzhou"] = "2014-05-26"
            API_VERSIONS["vpc"] = "2016-04-28"
            API_VERSIONS["redisa"] = "2015-01-01"
            API_VERSIONS["r-kvstore"] = "2015-01-01"
            API_VERSIONS["codepipeline"] = "2021-06-25"
            API_VERSIONS["cds"] = "2021-06-25"
            API_VERSIONS["rds"] = "2014-08-15"
            API_VERSIONS["httpdns"] = "2016-02-01"
            API_VERSIONS["httpdns-api"] = "2016-02-01"
            API_VERSIONS["green"] = "2018-05-09"
            API_VERSIONS["alidns"] = "2014-05-15"
            API_VERSIONS["push"] = "2016-08-01"
            API_VERSIONS["cloudpush"] = "2016-08-01"
            API_VERSIONS["cms"] = "2019-01-01"
            API_VERSIONS["metrics"] = "2019-01-01"
            API_VERSIONS["slb"] = "2014-05-15"
            API_VERSIONS["apigateway"] = "2016-07-14"
            API_VERSIONS["cloudapi"] = "2016-07-14"
            API_VERSIONS["sas"] = "2018-12-03"
            API_VERSIONS["sas-api"] = "2018-12-03"
            API_VERSIONS["beebot"] = "2017-10-11"
            API_VERSIONS["chatbot"] = "2017-10-11"
            API_VERSIONS["iot"] = "2018-01-20"
            API_VERSIONS["arms"] = "2019-08-08"
            API_VERSIONS["polardb"] = "2017-08-01"
            API_VERSIONS["ccc"] = "2017-07-05"
            API_VERSIONS["bastionhost"] = "2019-12-09"
            API_VERSIONS["yundun-bastionhost"] = "2019-12-09"
            API_VERSIONS["rtc"] = "2018-01-01"
            API_VERSIONS["nlp"] = "2019-11-11"
            API_VERSIONS["nlp-automl"] = "2019-11-11"
            API_VERSIONS["trademark"] = "2019-12-09"
            API_VERSIONS["sca"] = "2019-01-15"
            API_VERSIONS["qualitycheck"] = "2019-01-15"
            API_VERSIONS["iovcc"] = "2018-05-01"
            API_VERSIONS["ons"] = "2019-02-14"
            API_VERSIONS["onsvip"] = "2019-02-14"
            API_VERSIONS["pts"] = "2020-10-20"
            API_VERSIONS["waf"] = "2019-09-10"
            API_VERSIONS["wafopenapi"] = "2019-09-10"
            API_VERSIONS["cloudfirewall"] = "2017-12-07"
            API_VERSIONS["cloudfw"] = "2017-12-07"
            API_VERSIONS["baas"] = "2018-12-21"
            API_VERSIONS["imm"] = "2017-09-06"
            API_VERSIONS["ims"] = "2019-08-15"
            API_VERSIONS["oss"] = "2019-05-17"
            API_VERSIONS["ddoscoo"] = "2020-01-01"
            API_VERSIONS["smartag"] = "2018-03-13"
            API_VERSIONS["actiontrail"] = "2020-07-06"
            API_VERSIONS["ots"] = "2016-06-20"
            API_VERSIONS["cas"] = "2020-04-07"
            API_VERSIONS["mts"] = "2014-06-18"
            API_VERSIONS["pvtz"] = "2018-01-01"
            API_VERSIONS["ensdisk"] = "2017-11-10"
            API_VERSIONS["ens"] = "2017-11-10"
            API_VERSIONS["vod"] = "2017-03-21"
            API_VERSIONS["imagesearch"] = "2020-12-14"
            API_VERSIONS["market"] = "2015-11-01"
            API_VERSIONS["pcdn"] = "2017-04-11"
            API_VERSIONS["nas"] = "2017-06-26"
            API_VERSIONS["kms"] = "2016-01-20"
            API_VERSIONS["eci"] = "2018-08-08"
            API_VERSIONS["fc"] = "2021-04-06"
            API_VERSIONS["openanalytics"] = "2018-06-19"
            API_VERSIONS["dcdn"] = "2018-01-15"
            API_VERSIONS["elasticsearch"] = "2017-06-13"
            API_VERSIONS["dts"] = "2020-01-01"
            API_VERSIONS["dysmsapi"] = "2017-05-25"
            API_VERSIONS["dybaseapi"] = "2017-05-25"
            API_VERSIONS["bssopenapi"] = "2017-12-14"
            API_VERSIONS["business"] = "2017-12-14"
            API_VERSIONS["dmsenterprise"] = "2018-11-01"
            API_VERSIONS["dms-enterprise"] = "2018-11-01"
            API_VERSIONS["alikafka"] = "2019-09-16"
            API_VERSIONS["foas"] = "2018-11-11"
            API_VERSIONS["alidfs"] = "2018-06-20"
            API_VERSIONS["dfs"] = "2018-06-20"
            API_VERSIONS["airec"] = "2020-11-26"
            API_VERSIONS["scdn"] = "2017-11-15"
            API_VERSIONS["saf"] = "2017-03-31"
            API_VERSIONS["linkwan"] = "2019-03-01"
            API_VERSIONS["linkedmall"] = "2018-01-16"
            API_VERSIONS["vs"] = "2018-12-12"
            API_VERSIONS["aiccs"] = "2018-10-15"
            API_VERSIONS["ccs"] = "2018-10-15"
            API_VERSIONS["hitsdb"] = "2020-06-15"
            API_VERSIONS["alimt"] = "2018-10-12"
            API_VERSIONS["mt"] = "2018-10-12"
            API_VERSIONS["dbs"] = "2019-03-06"
            API_VERSIONS["xxx"] = "2019-12-09"
        }
        return API_VERSIONS[productCode]
    }

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