package org.jetbrains.plugins.httpx.spellchecker

import com.intellij.spellchecker.BundledDictionaryProvider

class HttpxBundledDictionaryProvider : BundledDictionaryProvider {
    override fun getBundledDictionaries(): Array<String> {
        return arrayOf("httpx.dic")
    }
}