package net.helao.novel.books.controller

import com.google.common.collect.ImmutableList
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.join
import org.apache.commons.lang3.math.NumberUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * 抓取数据然后生成静态页面，
 * 页面dir
 * - index.html 首页每天更新的内容，和书架
 * - books/index.html 书架
 * - books/xxxx/index.html 每本书的章节目录
 * - books/xxxx/xxxx.html 每章详情
 *
 * @author snail
 * @date 2017/9/12.
 */
@RestController
class IndexController {

    @Value("\${base.dir}")
    internal var baseDir: String? = null

    @GetMapping("/{groupId}/{bookId}/{chapterId:\\d+}.html")
    @Throws(IOException::class)
    fun chapter(@PathVariable groupId: String, @PathVariable bookId: String, @PathVariable chapterId: String): Any {
        val content = renderFile(join("/", groupId, "/", bookId, "/", chapterId, ".html"))
        val doc = Jsoup.parse(content)
        val styles = doc.head().select("style")
        if (!styles.isEmpty()) {
            styles.remove()
            //            <link rel="stylesheet" type="text/css" href="/main.css">
            doc.head().appendChild(object : Element("link") {
                init {
                    attr("rel", "stylesheet")
                    attr("type", "text/css")
                    attr("href", "/main.css")
                }
            })
        }
        doc.head().select("meta").remove()
        doc.body().removeAttr("style")
        val fileNameList = File(baseDir, groupId + "/" + bookId).list { file, name -> StringUtils.endsWith(name, ".html") }
        if (fileNameList != null) {
            val idList = fileNameList.map { name -> NumberUtils.toInt(StringUtils.substringBefore(name, ".html")) }.sorted()
            val title = Element("h3")
            title.text(doc.title())
            val index = idList.indexOf(NumberUtils.toInt(chapterId))
            val prev = if (index > 0) idList[index - 1] else -1
            val last = if (index < idList.size - 1) idList[index + 1] else -1
            val div = Element("div")
            if (prev > 0) {
                val a = Element("a").apply {
                    attr("href", join("/", groupId, "/", bookId, "/", prev, ".html"))
                    text("上一页")
                }
                div.appendChild(a)
            }
            div.appendChild(Element("a").apply {
                attr("href", "/")
                text("首页")
            })
            div.appendChild(Element("a").apply {
                attr("href", join("/", groupId, "/", bookId, "/index.html"))
                text("目录")
            })
            if (last > 0) {
                val a = Element("a").apply {
                    attr("href", join("/", groupId, "/", bookId, "/", last, ".html"))
                    text("下一页")
                }
                div.appendChild(a)
            }
            doc.body().insertChildren(0, ImmutableList.of(title))
            doc.body().appendChild(div)
        }
        return doc.html()
    }

    @Throws(IOException::class)
    private fun renderFile(path: String): String {
        val file = File(baseDir, path)
        val input = FileInputStream(file)
        val content = IOUtils.toString(input, StandardCharsets.UTF_8)
        IOUtils.closeQuietly(input)
        return content
    }

}
