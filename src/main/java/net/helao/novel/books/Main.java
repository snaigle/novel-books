package net.helao.novel.books;

import freemarker.cache.ClassTemplateLoader;
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

import java.io.*;
import java.util.*;

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

    public static final String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
    public static File base = new File("/data/books/");

    public static void main(String[] args) throws IOException {
        List<String> books = IOUtils.readLines(new FileInputStream("/data/books.txt"));
        List<Map<String, Object>> booksModel = new ArrayList<>();
        for (String bookUrl : books) {
            Map<String, Object> book = fetchBook(bookUrl);
            if (book != null) {
                booksModel.add(book);
            }
        }
        Map<String, Object> model = new HashMap<>();
        model.put("items", booksModel);
        write2File("index.ftl", new File(base, "index.html"), model);
    }

    public static Map<String, Object> fetchBook(String url) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        String content = http(url, client);
        if (content == null) {
            return null;
        }
        String path = convert2Path(url);
        Document doc = Jsoup.parse(content, url);
        String title = doc.select("#info h1").text();
        System.out.println("正在抓取:" + title);
        List<Pair<String, String>> menus = new ArrayList<>();
        for (Element e : doc.select("#list dd a")) {
            String etitle = e.text();
            String eurl = e.attr("href");
            String epath = convert2Path(eurl);
            File efile = new File(base, epath);
            Pair<String, String> pair = Pair.of(etitle, epath);
            menus.add(0, pair);
            if (efile.exists()) {
                continue;
            }
            System.out.println("正在抓取:" + etitle);
            fetchBookDetail(etitle, e.absUrl("href"), efile, client);
        }
        if (menus.size() > 0) {
            Map<String, Object> model = new HashMap<>();
            model.put("title", title);
            model.put("url", path);
            model.put("items", menus);
            write2File("book.ftl", new File(base, path), model);
            model.put("items", subList(menus, 0, 5));
            return model;
        } else {
            return null;
        }
    }

    private static <T> List<T> subList(List<T> list, int fromIndex, int length) {
        List<T> subList = new ArrayList<>();
        for (int i = fromIndex; i < (fromIndex + length); i++) {
            if (fromIndex < list.size()) {
                subList.add(list.get(i));
            } else {
                break;
            }
        }
        return subList;
    }

    private static Configuration conf = null;

    private static void write2File(String tpl, File file, Map<String, Object> model) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (conf == null) {
            conf = new Configuration();
            conf.setTemplateLoader(new ClassTemplateLoader(Main.class, "/"));
        }
        try {
            Template temp = conf.getTemplate(tpl);
            temp.process(model, new FileWriter(file));
        } catch (TemplateException | IOException e) {
            // do nothing
            e.printStackTrace();
        }
    }

    public static boolean fetchBookDetail(String title, String url, File file, OkHttpClient client) {
        String content = http(url, client);
        if (content == null) {
            return false;
        }
        Document doc = Jsoup.parse(content, url);
        Map<String, Object> model = new HashMap<>();
        model.put("title", title);
        model.put("content", doc.select("#content").html());
        write2File("detail.ftl", file, model);
        return true;
    }

    public static String convert2Path(String url) {
        String path = StringUtils.substringAfter(url, "/html");
        if (StringUtils.endsWith(path, "/")) {
            path += "index.html";
        }
        return path;
    }

    private static String http(String url, OkHttpClient client) {
        try {
            Response resp = client.newCall(new Request.Builder().addHeader("User-Agent", ua).url(url).get().build()).execute();
            if (resp.isSuccessful()) {
                String content = IOUtils.toString(resp.body().bytes(), "gbk");
                resp.body().close();
                return content;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
