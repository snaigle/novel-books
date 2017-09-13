package net.helao.novel.books;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}