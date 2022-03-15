package org.jetbrains.plugins.httpx.json

import org.junit.Test

class XJsonTypeJsonSchemaProviderFactoryTest {

    @Test
    fun convertJsonTypeToSchema() {
        val factory = XJsonTypeJsonSchemaProviderFactory()
        val jsonType = "{id:number, name:string, info: {age: number}, alias?: [number, string] }"
        println(factory.convertTypeToSchema(jsonType))
    }

    @Test
    fun convertJsonArrayToSchema() {
        val factory = XJsonTypeJsonSchemaProviderFactory();
        val jsonType = "[number, [number, string], {id:number, nick:string, alias: [string, string]} ]"
        println(factory.convertTypeToSchema(jsonType))
    }

    @Test
    fun testArray() {
        val factory = XJsonTypeJsonSchemaProviderFactory()
        val jsonType = "{names: Set<string>}"
        println(factory.convertTypeToSchema(jsonType))
    }


}