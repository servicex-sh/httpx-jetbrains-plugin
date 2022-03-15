package org.jetbrains.plugins.httpx.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.httpClient.http.request.psi.*
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.ElementPatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext

class AliyunRequestCompletionContributor : CompletionContributor() {
    companion object {
        val completionCapture: PsiElementPattern.Capture<LeafPsiElement> = PlatformPatterns.psiElement(LeafPsiElement::class.java)
            .withParent(object : ElementPattern<PsiElement?> {
                override fun accepts(o: Any?): Boolean {
                    return o is HttpPathAbsolute
                            || o is HttpHost
                            || o is HttpRequestTarget
                            || o is HttpQueryParameterValue
                }

                override fun accepts(o: Any?, context: ProcessingContext): Boolean {
                    return accepts(o)
                }

                override fun getCondition(): ElementPatternCondition<PsiElement?>? {
                    return null
                }
            })
    }

    init {
        extend(CompletionType.BASIC, completionCapture, AliyunRequestCompletionProvider())
    }

    private class AliyunRequestCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val element = parameters.position
            val parent = element.parent
            var prefix = trimDummy(parent.text)
            val httpRequest = element.parentOfType<HttpRequest>()
            if (httpRequest != null && "ALIYUN" == httpRequest.httpMethod) {
                if (parent is HttpHost) {
                    result.addElement(LookupElementBuilder.create("ecs.cn-huhehaote.aliyuncs.com"))
                    result.addElement(LookupElementBuilder.create("ecs.me-east-1.aliyuncs.com"))
                    result.addElement(LookupElementBuilder.create("ecs.ap-northeast-1.aliyuncs.com"))
                } else if (parent is HttpQueryParameterValue) {
                    result.addElement(LookupElementBuilder.create("CreateInstance"))
                    result.addElement(LookupElementBuilder.create("DeleteInstance"))
                }
            }
        }

        private fun trimDummy(value: String): String {
            return StringUtil.trim(value.replace(CompletionUtil.DUMMY_IDENTIFIER, "").replace(CompletionUtil.DUMMY_IDENTIFIER_TRIMMED, ""))
        }

    }
}