package net.helao.novel.books;

import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author wangcheng<wangcheng@mucang.cn>
 * @date 2017/8/29.
 */
@Component
public class NovelTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private Thread thread;

    @Value("${base.dir}")
    private String baseDir;
    @Value("${base.file}")
    private String urlsFile;
    @Autowired
    private Configuration conf;

    @PostConstruct
    void init() {
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @PreDestroy
    void destroy() {
        thread.interrupt();
    }

    public void run() {
        while (true) {
            try {
                logger.info("开始抓取");
                List<String> urls = FileUtils.readLines(new File(urlsFile), StandardCharsets.UTF_8);
                Novel novel = new Novel(urls, baseDir, conf);
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
