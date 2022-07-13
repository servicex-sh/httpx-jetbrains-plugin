package org.jetbrains.plugins.httpx.navigation

import com.intellij.httpClient.http.request.psi.HttpHeaderField
import com.intellij.httpClient.http.request.psi.HttpHeaderFieldValue
import com.intellij.navigation.DirectNavigationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.java.stubs.index.JavaFullClassNameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType

@Suppress("UnstableApiUsage")
class XJavaTypeNavigation : DirectNavigationProvider {

    override fun getNavigationElement(element: PsiElement): PsiElement? {
        if (element is HttpHeaderFieldValue) {
            val httpHeader = element.parentOfType<HttpHeaderField>()!!
            if (httpHeader.name == "X-Java-Type") {
                val javaType = element.text
                val targetPsiClasses = JavaFullClassNameIndex.getInstance().get(javaType, element.project, GlobalSearchScope.allScope(element.project))
                if (targetPsiClasses != null && targetPsiClasses.isNotEmpty()) {
                    return targetPsiClasses.first()
                }
            }
        }
        return null
    }

}