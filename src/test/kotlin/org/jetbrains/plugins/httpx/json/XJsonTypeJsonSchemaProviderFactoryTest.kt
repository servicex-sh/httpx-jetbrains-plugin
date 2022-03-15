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
        val jsonType = "['vip'|'guest', [number, string], {id:number, nick:string, alias: [string, string]} ]"
        println(factory.convertTypeToSchema(jsonType))
    }

    @Test
    fun testComplex() {
        val factory = XJsonTypeJsonSchemaProviderFactory()
        val jsonType = "{ names: Set<string>, type: 'vip' | 'normal', flag: 100 | 200, age: 1..200 }"
        println(factory.convertTypeToSchema(jsonType))
    }

    @Test
    fun testRegex() {
        val factory = XJsonTypeJsonSchemaProviderFactory()
        val jsonType = "{ id: string, name: String, phone: /\\d{13}/ }"
        println(factory.convertTypeToSchema(jsonType))
    }


}