package net.helao.novel.books.task

import freemarker.template.Configuration
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * @author snail
 * @date 2017/8/29.
 */
@Component
class NovelTask constructor(@Value("\${base.dir}") private val baseDir: String,
                            @Value("\${base.file}") private val urlsFile: String,
                            private val conf: Configuration) {

    @Scheduled(fixedDelay = (5 * 60 * 1000).toLong())
    fun run() {
        try {
            logger.info("开始抓取")
            val urls = FileUtils.readLines(File(urlsFile), StandardCharsets.UTF_8).filter {
                StringUtils.isNotBlank(it) && !StringUtils.startsWith(it,"#")
            }
            val novel = Novel(urls, baseDir, conf)
            novel.fetch()
            logger.info("抓取完成")
        } catch (ex: Exception) {
            logger.error("抓取出错了", ex)
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(NovelTask::class.java)
    }
}
