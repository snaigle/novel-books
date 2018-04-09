package net.helao.novel.books.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import javax.servlet.http.HttpServletRequest

/**
 * 抓取数据然后生成静态页面，
 * 页面dir
 * - index.html 首页每天更新的内容，和书架
 * - books/index.html 书架
 * - books/xxxx/index.html 每本书的章节目录
 * - books/xxxx/xxxx.html 每章详情
 *
 * @author snail
 * @date 2017/9/12.
 */
@RestController
@RequestMapping
class IndexController {

    @GetMapping
    fun index(req: WebRequest, req2: HttpServletRequest): String {
        return "index:index:${req.isSecure},${req2.scheme}"
    }

    @GetMapping("index1")
    fun index1(name: String): String {
        return "index:index1:$name"
    }

    @GetMapping("/index3")
    fun index3(name: String): String {
        return "index:index1:$name"
    }


}

data class Abc(val name: String, val password: String)

@RestController
@RequestMapping("/admin/index2/")
class Index2Controller {

    @GetMapping
    fun index(name: String): Any {
        return Abc("你好", "password")

    }

    @GetMapping("/index1")
    fun index2(name: String): String {
        return "index2:index2:$name"
    }
}
