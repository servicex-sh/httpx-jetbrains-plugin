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
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.plugins.httpx.restClient.execution.aliyun.Aliyun
import org.jetbrains.plugins.httpx.restClient.execution.aliyun.Products

class AliyunRequestCompletionContributor : CompletionContributor() {
    companion object {
        val globalParamNames = listOf("Action", "Version", "RegionId")
        val completionCapture: PsiElementPattern.Capture<LeafPsiElement> = PlatformPatterns.psiElement(LeafPsiElement::class.java)
            .withParent(object : ElementPattern<PsiElement?> {
                override fun accepts(o: Any?): Boolean {
                    return o is HttpPathAbsolute
                            || o is HttpHost
                            || o is HttpRequestTarget
                            || o is HttpQueryParameterValue
                            || o is HttpQueryParameterKey
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
            val prefix = trimDummy(parent.text)
            val httpRequest = element.parentOfType<HttpRequest>()
            if (httpRequest != null && "ALIYUN" == httpRequest.httpMethod) {
                if (parent is HttpHost) {
                    if (prefix.contains('.')) { // completion for endpoints
                        val productCode = prefix.substring(0, prefix.indexOf('.'))
                        val product = Products.instance().findProduct(productCode)
                        if (product != null) {
                            for (regionalEndpoint in product.getRegionalEndpoints()) {
                                result.addElement(LookupElementBuilder.create(regionalEndpoint.value))
                            }
                            if (product.hasGlobalEndpoint()) {
                                result.addElement(LookupElementBuilder.create(product.global_endpoint!!))
                            }
                        }
                    } else { // product code names
                        Products.instance().products.forEach {
                            val productCode = it.code.toLowerCase()
                            result.addElement(LookupElementBuilder.create("${productCode}.").withPresentableText(productCode))
                        }
                    }
                } else if (parent is HttpQueryParameterKey) {
                    val httpQuery = parent.getParentOfType<HttpQuery>(true)
                    if (httpQuery != null) {
                        val paramNames = httpQuery.queryParameterList.map { it.queryParameterKey.text }
                        globalParamNames.filter { paramNames.isEmpty() || !paramNames.contains(it) }
                            .forEach {
                                result.addElement(LookupElementBuilder.create("${it}=").withPresentableText(it))
                            }
                    }
                } else if (parent is HttpQueryParameterValue) {
                    //action or version
                    val httpQueryParameter = parent.parent
                    val paramName = httpQueryParameter.firstChild.text
                    if (globalParamNames.contains(paramName)) {
                        val httpRequestTarget = httpQueryParameter.getParentOfType<HttpRequestTarget>(true)
                        val host = httpRequestTarget?.host?.text
                        if (host != null && host.contains('.')) {
                            val product = Products.instance().findProductByHost(host)
                            if (product != null) {
                                when (paramName) {
                                    "Action" -> {
                                        for (action in product.apis) {
                                            result.addElement(LookupElementBuilder.create(action))
                                        }
                                    }
                                    "Version" -> {
                                        result.addElement(LookupElementBuilder.create(product.version))
                                    }
                                    "RegionId" -> {
                                        for (region in Aliyun.GLOBAL_REGIONS) {
                                            result.addElement(LookupElementBuilder.create(region))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun trimDummy(value: String): String {
            return StringUtil.trim(value.replace(CompletionUtil.DUMMY_IDENTIFIER, "").replace(CompletionUtil.DUMMY_IDENTIFIER_TRIMMED, ""))
        }

    }
}