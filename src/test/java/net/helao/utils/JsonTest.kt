package net.helao.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import org.junit.Test

class JsonTest {
    @Test
    fun testJackson() {
        val mapper = jacksonObjectMapper()
        val result = mapper.writeValueAsString(Abc("name", "password"))
        println(result)
        println(mapper.readValue<Abc>(result))
        println(mapper.readValue(result, Abc::class.java))
    }

    @Test(expected = InvalidDefinitionException::class)
    fun testOriginObjectMapper() {
        val mapper = ObjectMapper()
        val result = mapper.writeValueAsString(Abc("name", "password"))
        println(result)
        // 这里会报错
        println(mapper.readValue<Abc>(result))
        println(mapper.readValue(result, Abc::class.java))
    }

    @Test
    fun testGson() {
        val gson = Gson()
        val message = gson.toJson(Abc("name", "password"))
        println(message)
        println(gson.fromJson(message, Abc::class.java))
    }
}

data class Abc(val name: String, val password: String)
