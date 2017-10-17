package net.helao.novel.books.task;

import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author wangcheng<wangcheng@mucang.cn>
 * @date 2017/8/29.
 */
@Component
public class NovelTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NovelTask.class);

    @Value("${base.dir}")
    private String baseDir;
    @Value("${base.file}")
    private String urlsFile;
    @Autowired
    private Configuration conf;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void run() {
        try {
            logger.debug("开始抓取");
            List<String> urls = FileUtils.readLines(new File(urlsFile), StandardCharsets.UTF_8);
            Novel novel = new Novel(urls, baseDir, conf);
            novel.fetch();
            logger.debug("抓取完成");
        } catch (Exception ex) {
            logger.error("抓取出错了", ex);
        }
    }
}
