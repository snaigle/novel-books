<html>
    <head>
    <title>首页</title>
    <link rel="stylesheet" type="text/css" href="/main.css">
    </head>
    <body>
        <#list items as book>
            <div>
                <h3><a href="${book.url}">${book.title}</a></h3>
                <ul>
                 <#list book.items as it>
                     <li><a href="${it.value}">${it.key}</a></li>
                 </#list>
                </ul>
            </div>

        </#list>
    </body>
<html>