package org.jetbrains.plugins.httpx.restClient.execution.common

import com.intellij.httpClient.execution.common.CommonClientBodyFileHint
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.json.JsonFileType
import com.intellij.openapi.fileTypes.FileType

@Suppress("UnstableApiUsage")
class XmlBodyFileHint(override val fileExtensionHint: String?, override val fileNameHint: String?, override val fileTypeHint: FileType?) : CommonClientBodyFileHint {

    companion object {
        
        fun xmlBodyFileHint(fileName: String): XmlBodyFileHint {
            return if (fileName.endsWith(".xml")) {
                XmlBodyFileHint("xml", fileName, XmlFileType.INSTANCE)
            } else {
                XmlBodyFileHint("xml", "${fileName}.xml", XmlFileType.INSTANCE)
            }
        }
    }
}