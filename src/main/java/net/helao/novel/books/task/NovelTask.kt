package net.helao.novel.books.task

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * @author snail
 * @date 2017/8/29.
 */
@Component
class NovelTask constructor() {

    val logger: Logger = LoggerFactory.getLogger(NovelTask::class.java)

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    fun run() {
        try {
            logger.info("开始抓取")
            logger.info("抓取完成")
        } catch (ex: Exception) {
            logger.error("抓取出错了", ex)
        }
    }

}
