package net.helao.utils

import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.junit.Ignore
import org.junit.Test
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * @author snail
 * @date 2017/9/26.
 */
class JsoupTest {

    @Test
    @Ignore
    @Throws(IOException::class)
    fun testHead() {
        val doc = Jsoup.parse(IOUtils.toString(FileInputStream(System.getenv("HOME") + "/Downloads/1234.htm"), StandardCharsets.UTF_8))
        val style = doc.head().select("style")
        if (!style.isEmpty()) {
            style.remove()
            doc.head().appendChild(object : Element("link") {
                init {
                    attr("rel", "stylesheet")
                    attr("type", "text/css")
                    attr("href", "/main.css")
                }
            })
        }
        doc.body().removeAttr("style")
        println(doc.html())
    }
}
