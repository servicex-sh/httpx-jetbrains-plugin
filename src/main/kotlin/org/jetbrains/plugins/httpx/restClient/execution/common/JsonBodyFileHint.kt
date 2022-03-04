package org.jetbrains.plugins.httpx.restClient.execution.common

import com.intellij.httpClient.execution.common.CommonClientBodyFileHint
import com.intellij.json.JsonFileType
import com.intellij.openapi.fileTypes.FileType

@Suppress("UnstableApiUsage")
class JsonBodyFileHint(override val fileExtensionHint: String?, override val fileNameHint: String?, override val fileTypeHint: FileType?) : CommonClientBodyFileHint {

    companion object {
        
        fun jsonBodyFileHint(fileName: String): JsonBodyFileHint {
            return if (fileName.endsWith(".json")) {
                JsonBodyFileHint("json", fileName, JsonFileType.INSTANCE)
            } else {
                JsonBodyFileHint("json", "${fileName}.json", JsonFileType.INSTANCE)
            }
        }
    }
}