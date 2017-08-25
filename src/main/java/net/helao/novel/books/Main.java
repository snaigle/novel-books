package net.helao.novel.books;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 抓取数据然后生成静态页面，
 * 页面dir
 * - index.html 首页每天更新的内容，和书架
 * - books/index.html 书架
 * - books/xxxx/index.html 每本书的章节目录
 * - books/xxxx/xxxx.html 每章详情
 *
 * @author wangcheng<wangcheng@mucang.cn>
 * @date 2017/3/20.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {

        String baseDir = System.getProperty("baseDir", "/data/books");
        String urlsFile = System.getProperty("urlsFile", "/data/books.txt");
        while (true) {
            try {
                logger.info("开始抓取");
                List<String> urls = FileUtils.readLines(new File(urlsFile));
                Novel novel = new Novel(urls, baseDir);
                novel.fetch();
                logger.info("抓取完成");
                Thread.sleep(TimeUnit.MINUTES.toMillis(5));
                logger.info("睡醒了");
            } catch (InterruptedException e) {
                logger.error("准备退出");
                break;
            } catch (Exception ex) {
                logger.error("抓取出错了", ex);
            }
        }
    }


}
