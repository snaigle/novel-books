package net.helao.utils;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author wangcheng<wangcheng@mucang.cn>
 * @date 2017/9/26.
 */
public class JsoupTest {

    @Test
    @Ignore
    public void testHead() throws IOException {
        Document doc = Jsoup.parse(IOUtils.toString(new FileInputStream("/Users/tt/Downloads/1234.htm"), StandardCharsets.UTF_8));
        Elements style = doc.head().select("style");
        if (!style.isEmpty()) {
            style.remove();
            doc.head().appendChild(new Element("link") {{
                attr("rel", "stylesheet");
                attr("type", "text/css");
                attr("href", "/main.css");
            }});
        }
        doc.body().removeAttr("style");
        System.out.println(doc.html());
    }
}
