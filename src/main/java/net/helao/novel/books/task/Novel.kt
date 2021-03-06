package net.helao.novel.books.task

import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author snail
 * @date 2017/3/24.
 */
class Novel(books: List<String>, baseDirPath: String) {
    private val ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36"
    private val books = ArrayList<String>()
    private val baseDir: File

    init {
        this.books.addAll(books)
        baseDir = File(baseDirPath)
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
    }

    @Throws(FileNotFoundException::class)
    fun fetch() {
        books.map { fetchBook(it) }.filter { it.isNotEmpty() }.sortedByDescending {
            File(baseDir, (it["items"] as ArrayList<Pair<String, String>>)[0].first).let {
                if (it.exists()) it.lastModified() else Long.MAX_VALUE
            }
        }.let { renderIndex(it) }
    }

    private fun renderIndex(booksModel: List<Map<String, Any>>) {
        if (booksModel.isNotEmpty()) {
            write2File("index.ftl", File(baseDir, "index.html"), mapOf("items" to booksModel))
        }
    }

    private fun fetchBook(url: String): Map<String, Any> {
        val client = OkHttpClient.Builder().build()
        val content = http(url, client) ?: return emptyMap()
        val path = convert2Path(url)
        val doc = Jsoup.parse(content, url)
        val title = doc.select("#info h1").text()
        logger.info("正在抓取:{},{}", title, url)
        val menus = ArrayList<Pair<String, String>>()
        val urls = doc.select("#list dd a")
        for (i in urls.indices) {
            val e = urls[i]
            val etitle = e.text()
            val eurl = e.attr("href")
            val epath = convert2Path(eurl)
            val efile = File(baseDir, epath)
            if (efile.exists()) {
                val pair = Pair(etitle, epath)
                menus.add(0, pair)
                continue
            } else {
                logger.info("正在抓取:$etitle")
                fetchBookDetail(i == urls.size - 1, etitle, e.absUrl("href"), efile, client)
                val pair = Pair(etitle, epath)
                menus.add(0, pair)
            }
        }
        return if (menus.size > 0) renderBookIndex(title, path, menus).apply {
            put("items", subList(menus, 0, 5))
        } else emptyMap()
    }

    private fun renderBookIndex(title: String, path: String, menus: List<Pair<String, String>>): MutableMap<String, Any> {
        val model = HashMap<String, Any>()
        model.put("title", title)
        model.put("url", path)
        model.put("items", menus)
        write2File("book.ftl", File(baseDir, path), model)
        return model
    }

    private fun fetchBookDetail(isLast: Boolean, title: String, url: String, file: File, client: OkHttpClient): Boolean {
        val content = http(url, client) ?: return false
        val doc = Jsoup.parse(content, url)
        val html = doc.select("#content").html()
        // 只是最后一张重新抓取
        if (html.contains("章节内容正在手打中") && isLast) {
            // 没有抓取成功的，就不生成目录了
            return false
        }
        renderBookDetail(title, html, file)
        logger.info("抓取{}成功", title)
        return true
    }

    private fun renderBookDetail(title: String, content: String, file: File) {
        val model = HashMap<String, Any>()
        model.put("title", title)
        model.put("content", content)
        write2File("detail.ftl", file, model)
    }


    private fun convert2Path(url: String): String {
        var path = url.substringAfter("/html")
        if (path.endsWith("/")) {
            path += "index.html"
        }
        return path
    }

    private fun <T> subList(list: List<T>, fromIndex: Int, length: Int): List<T> {
        val subList = ArrayList<T>()
        for (i in fromIndex until fromIndex + length) {
            if (i < list.size) {
                subList.add(list[i])
            } else {
                break
            }
        }
        return subList
    }


    private fun write2File(tpl: String, file: File, model: Map<String, Any>) {
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        // todo render
    }


    private fun http(url: String, client: OkHttpClient): String? {
        try {
            val resp = client.newCall(Request.Builder().addHeader("User-Agent", ua).url(url).get().build()).execute()
            if (resp.isSuccessful) {
                val content = IOUtils.toString(resp.body().bytes(), "gbk")
                resp.body().close()
                return content
            }
        } catch (e: Exception) {
            logger.error("抓取页面出错了:{}", url, e)
        }

        return null
    }

    companion object {

        private val logger = LoggerFactory.getLogger(Novel::class.java)
    }

}
