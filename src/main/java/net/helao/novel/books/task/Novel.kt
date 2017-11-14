package net.helao.novel.books.task

import freemarker.template.Configuration
import freemarker.template.TemplateException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Pair
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.util.*

/**
 * @author snail
 * @date 2017/3/24.
 */
class Novel(books: List<String>?, baseDirPath: String, private val conf: Configuration) {
    private val ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36"
    private val books = ArrayList<String>()
    private val baseDir: File

    init {
        if (books != null) {
            this.books.addAll(books)
        }
        baseDir = File(baseDirPath)
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
    }

    @Throws(FileNotFoundException::class)
    fun fetch() {
        val booksModel = ArrayList<Map<String, Any>>()
        for (bookUrl in books) {
            val book = fetchBook(bookUrl)
            if (book != null) {
                booksModel.add(book)
            }
        }
        booksModel.sortByDescending {
            val file = File(baseDir, (it["items"] as List<Pair<String, String>>)[0].right)
            if (file.exists()) file.lastModified() else java.lang.Long.MAX_VALUE
        }
        renderIndex(booksModel)
    }

    private fun renderIndex(booksModel: List<Map<String, Any>>) {
        if (!booksModel.isEmpty()) {
            val model = HashMap<String, Any>()
            model.put("items", booksModel)
            write2File("index.ftl", File(baseDir, "index.html"), model)
        }
    }


    fun fetchBook(url: String): Map<String, Any>? {
        val client = OkHttpClient.Builder().build()
        val content = http(url, client) ?: return null
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
                val pair = Pair.of(etitle, epath)
                menus.add(0, pair)
                continue
            } else {
                logger.debug("正在抓取:" + etitle)
                fetchBookDetail(i == urls.size - 1, etitle, e.absUrl("href"), efile, client)
                val pair = Pair.of(etitle, epath)
                menus.add(0, pair)
            }
        }
        if (menus.size > 0) {
            val model = renderBookIndex(title, path, menus)
            model.put("items", subList(menus, 0, 5))
            return model
        } else {
            return null
        }
    }

    private fun renderBookIndex(title: String, path: String, menus: List<Pair<String, String>>): MutableMap<String, Any> {
        val model = HashMap<String, Any>()
        model.put("title", title)
        model.put("url", path)
        model.put("items", menus)
        write2File("book.ftl", File(baseDir, path), model)
        return model
    }

    fun fetchBookDetail(isLast: Boolean, title: String, url: String, file: File, client: OkHttpClient): Boolean {
        val content = http(url, client) ?: return false
        val doc = Jsoup.parse(content, url)
        val html = doc.select("#content").html()
        // 只是最后一张重新抓取
        if (StringUtils.contains(html, "章节内容正在手打中") && isLast) {
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
        var path = StringUtils.substringAfter(url, "/html")
        if (StringUtils.endsWith(path, "/")) {
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
        try {
            val temp = conf.getTemplate(tpl)
            temp.process(model, FileWriter(file))
        } catch (e: TemplateException) {
            // do nothing
            logger.error("渲染模板出错", e)
        } catch (e: IOException) {
            logger.error("渲染模板出错", e)
        }

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
