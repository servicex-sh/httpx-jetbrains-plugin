package org.jetbrains.plugins.httpx.codeInsight

import org.jetbrains.plugins.httpx.restClient.execution.aliyun.Products
import org.junit.Test

class AliyunCliParserTest {

    @Test
    fun testCommonsCli() {
        val parser =
            AliyunCliParser("aliyun alidns AddGtmAddressPool --region cn-qingdao --InstanceId xxx --Name 'pool-1' --Type 1 --MinAvailableAddrNum 3 --Addr.1.Value 11 --Addr.1.LbaWeight 11 --Addr.1.Mode 11 --Addr.2.Value 22 --Addr.2.LbaWeight 22 --Addr.2.Mode 22")
        val product = Products.instance().findProduct("alidns")!!
        println(parser.httpRequestCode(product))
    }
}