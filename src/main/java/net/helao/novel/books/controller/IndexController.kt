package net.helao.novel.books.controller

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.join
import org.apache.commons.lang3.math.NumberUtils
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView
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
@Controller
class IndexController constructor(@Value("\${base.dir}") val baseDir: String) {

    @GetMapping("/{groupId}/{bookId}/{chapterId:\\d+}.html")
    @Throws(IOException::class)
    fun chapter(@PathVariable groupId: String, @PathVariable bookId: String, @PathVariable chapterId: String): Any {
        val content = readFile(join("/", groupId, "/", bookId, "/", chapterId, ".html"))
        val doc = Jsoup.parse(content)
        val fileNameList = File(baseDir, groupId + "/" + bookId).list { _, name -> StringUtils.endsWith(name, ".html") }
        val model = hashMapOf<String, Any>()
        model["title"] = doc.title()
        model["content"] = doc.body().select("div").first().html()
        model["groupId"] = groupId
        model["bookId"] = bookId
        model["page"] = mapOf("prev" to -1, "last" to -1)
        if (fileNameList != null) {
            val idList = fileNameList.map { name -> NumberUtils.toInt(StringUtils.substringBefore(name, ".html")) }.sorted()
            val index = idList.indexOf(NumberUtils.toInt(chapterId))
            val prev = if (index > 0) idList[index - 1] else -1
            val last = if (index < idList.size - 1) idList[index + 1] else -1
            model["page"] = mapOf("prev" to prev, "last" to last)
        }
        return ModelAndView("detail-front", model)
    }

    @Throws(IOException::class)
    private fun readFile(path: String): String {
        val file = File(baseDir, path)
        val input = FileInputStream(file)
        val content = IOUtils.toString(input, StandardCharsets.UTF_8)
        IOUtils.closeQuietly(input)
        return content
    }

}
