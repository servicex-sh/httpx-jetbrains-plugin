package org.jetbrains.plugins.httpx.codeInsight

import com.intellij.codeInsight.editorActions.CopyPastePreProcessor
import com.intellij.httpClient.http.request.HttpRequestPsiFile
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RawText
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.httpx.restClient.execution.aliyun.Products

class AliyunCliCopyPastePreProcessor : CopyPastePreProcessor {

    override fun preprocessOnCopy(file: PsiFile?, startOffsets: IntArray?, endOffsets: IntArray?, text: String?): String? {
        return null
    }

    override fun preprocessOnPaste(project: Project, file: PsiFile, editor: Editor, text: String, rawText: RawText?): String {
        if (file !is HttpRequestPsiFile) {
            return text
        } else if (!isAliyunCli(text)) {
            return text
        } else {  // convert aliyun cli to http request code
            val parser = AliyunCliParser(text)
            val product = Products.instance().findProduct(parser.serviceCode)
            return parser.httpRequestCode(product!!)
        }
    }

    private fun isAliyunCli(text: String): Boolean {
        if (text.startsWith("aliyun ")) {
            val parts = text.split(' ', ignoreCase = false, limit = 4)
            if (parts.size >= 3) {
                val productCode = parts[1]
                val product = Products.instance().findProduct(productCode)
                return product != null && product.apis.contains(parts[2])
            }
        }
        return false
    }

}