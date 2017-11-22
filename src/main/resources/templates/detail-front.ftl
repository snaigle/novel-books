<html>
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>${title}</title>
        <link rel="stylesheet" type="text/css" href="/main.css">
    </head>
    <body>
        <h3>${title}</h3>
        <div>
        ${content}
        </div>
        <div>
            <#if page.prev gt 0>
            <a class="button is-primary" href="/${groupId}/${bookId}/${prev?string["#"]}.html">上一页</a>
            </#if>
            <a class="button is-primary" href="/">首页</a>
            <a class="button is-primary" href="/${groupId}/${bookId}/">目录</a>
            <#if page.last gt 0>
            <a class="button is-primary" href="/${groupId}/${bookId}/${last?string["#"]}.html">下一页</a>
            </#if>
        </div>
    </body>
<html>