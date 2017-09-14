package net.helao.novel.books.controller;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.join;

/**
 * @author wangcheng<wangcheng@mucang.cn>
 * @date 2017/9/12.
 */
@RestController
public class IndexController {

    @Value("${base.dir}")
    String baseDir;

    @GetMapping("/{groupId}/{bookId}/{chapterId:\\d+}.html")
    public Object chapter(@PathVariable String groupId, @PathVariable String bookId, @PathVariable String chapterId) throws IOException {
        String content = renderFile(join("/", groupId, "/", bookId, "/", chapterId, ".html"));
        Document doc = Jsoup.parse(content);
        String[] fileNameList = new File(baseDir, groupId + "/" + bookId).list((file, name) -> StringUtils.endsWith(name, ".html"));
        if (fileNameList != null) {
            List<Integer> idList = Arrays.stream(fileNameList)
                    .mapToInt(name -> NumberUtils.toInt(StringUtils.substringBefore(name, ".html"))).boxed().sorted().collect(Collectors.toList());
            Element title = new Element("h3");
            title.text(doc.title());
            int index = idList.indexOf(NumberUtils.toInt(chapterId));
            int prev = index > 0 ? idList.get(index - 1) : -1;
            int last = index < idList.size() - 1 ? idList.get(index + 1) : -1;
            Element div = new Element("div");
            if (prev > 0) {
                Element a = new Element("a");
                a.attr("href", join("/", groupId, "/", bookId, "/", prev, ".html"));
                a.text("上一页");
                div.appendChild(a);
            }
            div.appendChild(new Element("a") {{
                attr("href", "/");
                text("首页");
            }});
            div.appendChild(new Element("a") {{
                attr("href", join("/", groupId, "/", bookId, "/index.html"));
                text("目录");
            }});
            if (last > 0) {
                Element a = new Element("a");
                a.attr("href", join("/", groupId, "/", bookId, "/", last, ".html"));
                a.text("下一页");
                div.appendChild(a);
            }
            doc.body().insertChildren(0, ImmutableList.of(title));
            doc.body().appendChild(div);
        }
        return doc.html();
    }

    private String renderFile(String path) throws IOException {
        File file = new File(baseDir, path);
        FileInputStream input = new FileInputStream(file);
        String content = IOUtils.toString(input, StandardCharsets.UTF_8);
        IOUtils.closeQuietly(input);
        return content;
    }

}
