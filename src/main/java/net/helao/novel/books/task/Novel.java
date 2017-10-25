package net.helao.novel.books.task;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangcheng<wangcheng@mucang.cn>
 * @date 2017/3/24.
 */
public class Novel {
    private final String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

    private static final Logger logger = LoggerFactory.getLogger(Novel.class);
    private List<String> books = new ArrayList<>();
    private Configuration conf;
    private File baseDir;

    public Novel(List<String> books, String baseDirPath, Configuration conf) {
        if (books != null) {
            this.books.addAll(books);
        }
        baseDir = new File(baseDirPath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        this.conf = conf;
    }

    public void fetch() throws FileNotFoundException {
        List<Map<String, Object>> booksModel = new ArrayList<>();
        for (String bookUrl : books) {
            Map<String, Object> book = fetchBook(bookUrl);
            if (book != null) {
                booksModel.add(book);
            }
        }
        booksModel.sort((item1, item2) -> {
            File file1 = new File(baseDir, ((List<Pair<String, String>>) item1.get("items")).get(0).getRight());
            File file2 = new File(baseDir, ((List<Pair<String, String>>) item2.get("items")).get(0).getRight());
            long time1 = file1.exists() ? file1.lastModified() : Long.MAX_VALUE;
            long time2 = file2.exists() ? file2.lastModified() : Long.MAX_VALUE;
            return Long.compare(time2, time1);
        });
        renderIndex(booksModel);
    }

    private void renderIndex(List<Map<String, Object>> booksModel) {
        if (!booksModel.isEmpty()) {
            Map<String, Object> model = new HashMap<>();
            model.put("items", booksModel);
            write2File("index.ftl", new File(baseDir, "index.html"), model);
        }
    }


    public Map<String, Object> fetchBook(String url) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        String content = http(url, client);
        if (content == null) {
            return null;
        }
        String path = convert2Path(url);
        Document doc = Jsoup.parse(content, url);
        String title = doc.select("#info h1").text();
        logger.info("正在抓取:{},{}", title, url);
        List<Pair<String, String>> menus = new ArrayList<>();
        Elements urls = doc.select("#list dd a");
        for (int i = 0; i < urls.size(); i++) {
            Element e = urls.get(i);
            String etitle = e.text();
            String eurl = e.attr("href");
            String epath = convert2Path(eurl);
            File efile = new File(baseDir, epath);
            if (efile.exists()) {
                Pair<String, String> pair = Pair.of(etitle, epath);
                menus.add(0, pair);
                continue;
            } else {
                logger.debug("正在抓取:" + etitle);
                fetchBookDetail(i == urls.size() - 1, etitle, e.absUrl("href"), efile, client);
                Pair<String, String> pair = Pair.of(etitle, epath);
                menus.add(0, pair);
            }
        }
        if (menus.size() > 0) {
            Map<String, Object> model = renderBookIndex(title, path, menus);
            model.put("items", subList(menus, 0, 5));
            return model;
        } else {
            return null;
        }
    }

    private Map<String, Object> renderBookIndex(String title, String path, List<Pair<String, String>> menus) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", title);
        model.put("url", path);
        model.put("items", menus);
        write2File("book.ftl", new File(baseDir, path), model);
        return model;
    }

    public boolean fetchBookDetail(boolean isLast, String title, String url, File file, OkHttpClient client) {
        String content = http(url, client);
        if (content == null) {
            return false;
        }
        Document doc = Jsoup.parse(content, url);
        String html = doc.select("#content").html();
        // 只是最后一张重新抓取
        if (StringUtils.contains(html, "章节内容正在手打中") && isLast) {
            // 没有抓取成功的，就不生成目录了
            return false;
        }
        renderBookDetail(title, html, file);
        logger.info("抓取{}成功", title);
        return true;
    }

    private void renderBookDetail(String title, String content, File file) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", title);
        model.put("content", content);
        write2File("detail.ftl", file, model);
    }


    private String convert2Path(String url) {
        String path = StringUtils.substringAfter(url, "/html");
        if (StringUtils.endsWith(path, "/")) {
            path += "index.html";
        }
        return path;
    }

    private <T> List<T> subList(List<T> list, int fromIndex, int length) {
        List<T> subList = new ArrayList<>();
        for (int i = fromIndex; i < (fromIndex + length); i++) {
            if (i < list.size()) {
                subList.add(list.get(i));
            } else {
                break;
            }
        }
        return subList;
    }


    private void write2File(String tpl, File file, Map<String, Object> model) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            Template temp = conf.getTemplate(tpl);
            temp.process(model, new FileWriter(file));
        } catch (TemplateException | IOException e) {
            // do nothing
            logger.error("渲染模板出错", e);
        }
    }


    private String http(String url, OkHttpClient client) {
        try {
            Response resp = client.newCall(new Request.Builder().addHeader("User-Agent", ua).url(url).get().build()).execute();
            if (resp.isSuccessful()) {
                String content = IOUtils.toString(resp.body().bytes(), "gbk");
                resp.body().close();
                return content;
            }
        } catch (Exception e) {
            logger.error("抓取页面出错了:{}", url, e);
        }
        return null;
    }

}
