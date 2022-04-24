package org.jetbrains.plugins.httpx.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang.StringUtils


object JsonUtils {
    val objectMapper: ObjectMapper = ObjectMapper()
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
        .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
        .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    fun convertToDoubleQuoteString(text: String): String {
        var escapedText = StringUtils.replace(text, "\"", "\\\"")
        escapedText = StringUtils.replace(escapedText, "\n", "\\n")
        escapedText = StringUtils.replace(escapedText, "\r", "")
        return "\"${escapedText}\""
    }

    /**
     * JSON Type object, array, number, string, Boolean ( true or false ), or null
     */
    fun wrapJsonValue(value: String): String {
        return if (value == "true" || value == "false" || value == "null") {
            value
        } else if (value.startsWith('\"') || value.startsWith('[') || value.startsWith('{')) {
            value
        } else if (value.contains('\"')) {
            convertToDoubleQuoteString(value)
        } else { // text or number
            if (value.toDoubleOrNull() != null) {
                value
            } else {
                "\"${value}\""
            }
        }
    }
}