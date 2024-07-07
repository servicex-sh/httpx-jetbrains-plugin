package org.jetbrains.plugins.httpx.injector

import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.psi.HttpMessageBody
import com.intellij.httpClient.http.request.psi.HttpRequest
import com.intellij.lang.Language
import com.intellij.lang.injection.general.Injection
import com.intellij.lang.injection.general.LanguageInjectionContributor
import com.intellij.lang.injection.general.SimpleInjection
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType

/**
 * language injection for Redis EVAL method with Lua script
 */
class LuaScriptInjectionContributor : LanguageInjectionContributor {
    var luaLanguage: Language? = null

    companion object {
        //language=lua
        val prefix = """
            KEYS = { "a", "b", "c", "d" }
            ARGV = { "a", "b", "c", "d" }
            
            redis = {
                REDIS_VERSION = "7.0.0",
                REDIS_VERSION_NUM = 0x00070000,
                LOG_DEBUG = 0,
                LOG_VERBOSE = 1,
                LOG_NOTICE = 2,
                LOG_WARNING = 3,
                REPL_NONE = 0,
                REPL_AOF = 1,
                REPL_SLAVE = 2,
                REPL_REPLICA = 2,
                REPL_ALL = 3,
            
                call = function(command, ...)
            
                end,
                pcall = function(command, ...)
            
                end,
                error_reply = function(text)
            
                end,
                status_reply = function(text)
            
                end,
                sha1hex = function(text)
            
                end,
                log = function(level, message)
            
                end,
                set_repl = function()
            
                end,
                replicate_commands = function()
            
                end,
                breakpoint = function()
            
                end,
                debug = function(text)
            
                end,
                acl_check_cmd = function(command, ...)
            
                end,
                register_function = function(command, ...)
                
                end
            }
            
            struct = {
                pack = function(format, ...)
            
                end,
                unpack = function(format, ...)
            
                end,
                size = function(format)
            
                end
            }
            
            cjson = {
                encode = function(data)
            
                end,
                decode = function(text)
            
                end
            }
            
            cmsgpack = {
                pack = function(data)
            
                end,
                unpack = function(text)
            
                end
            }
            
            bit = {
                tobit = function(x)
            
                end,
                tohex = function(x)
            
                end,
                bnot = function(x)
            
                end,
                bor = function(x)
            
                end,
                band = function(x)
            
                end,
                bxor = function(x)
            
                end,
                lshift = function(x, n)
            
                end,
                rshift = function(x, n)
            
                end,
                arshift = function(x, n)
            
                end,
                rol = function(x, n)
            
                end,
                rol = function(x, n)
            
                end,
                ror = function(x, n)
            
                end,
                bswap = function(x)
            
                end
            }    
            """.trimIndent()

        //language=lua
        val neovimPrefix = """
        vim = {
           api = {
              
           },
           fn = {
           
           },
           keymap = {
           
           }
        }    
        """.trimIndent()
    }

    init {
        luaLanguage = Language.findLanguageByID("Lua")
    }

    override fun getInjection(context: PsiElement): Injection? {
        if (luaLanguage != null && context is HttpMessageBody) {
            val httpRequest = context.parentOfType<HttpRequest>(false)
            if (httpRequest != null) {
                if (httpRequest.httpMethod == "EVAL" || httpRequest.httpMethod == "LOAD") {
                    return SimpleInjection(luaLanguage!!, prefix, "", null);
                } else {
                    val contentTypeHeader = httpRequest.getHeaderField("Content-Type")
                    val contentType = contentTypeHeader?.getValue(HttpRequestVariableSubstitutor.getDefault(context.project, context.containingFile))
                    if (contentType == "text/x-lua") {
                        return if (httpRequest.requestTarget!!.text.contains("nvim_")) {
                            SimpleInjection(luaLanguage!!, neovimPrefix, "", null)
                        } else {
                            SimpleInjection(luaLanguage!!, "", "", null)
                        }
                    }
                }
            }
        }
        return null
    }
}