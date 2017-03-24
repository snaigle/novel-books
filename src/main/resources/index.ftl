<html>
    <head>
    <title>首页</title>
    <style>
            body {
                font-size:3em;
            }
    </style>
    </head>
    <body style="font-size:3em">
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