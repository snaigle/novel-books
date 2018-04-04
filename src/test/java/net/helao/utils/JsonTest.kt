package net.helao.utils

import com.google.gson.Gson
import org.junit.Test

class JsonTest {

    @Test
    fun testGson() {
        val gson = Gson()
        val message = gson.toJson(Abc("name", "password"))
        println(message)
        println(gson.fromJson(message, Abc::class.java))
    }
}

data class Abc(val name: String, val password: String)
