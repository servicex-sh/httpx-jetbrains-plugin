package org.jetbrains.plugins.httpx.restClient.execution.common

import com.intellij.httpClient.execution.common.CommonClientBodyFileHint
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType

@Suppress("UnstableApiUsage")
class TextBodyFileHint(override val fileExtensionHint: String?, override val fileNameHint: String?, override val fileTypeHint: FileType?) : CommonClientBodyFileHint {

    companion object {

        fun textBodyFileHint(fileName: String): TextBodyFileHint {
            return if (fileName.endsWith(".txt")) {
                TextBodyFileHint("txt", fileName, PlainTextFileType.INSTANCE)
            } else {
                TextBodyFileHint("txt", "${fileName}.txt", PlainTextFileType.INSTANCE)
            }
        }
    }
}