package org.jetbrains.plugins.httpx.restClient.execution.common

class CloudAccount {
    var accessKeyId: String? = null
    var accessKeySecret: String? = null
    var regionId: String? = null

    constructor() {

    }

    constructor(accessKeyId: String?, accessKeySecret: String?, regionId: String?) {
        this.accessKeyId = accessKeyId
        this.accessKeySecret = accessKeySecret
        this.regionId = regionId
    }
}