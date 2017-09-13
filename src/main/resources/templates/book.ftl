<html>
    <head>
    <title>${title}</title>
    <link rel="stylesheet" type="text/css" href="/main.css">
    </head>
    <body>
        <ul>
            <#list items as it>
                <li><a href="${it.value}">${it.key}</a></li>
            </#list>
        </ul>
    </body>
<html>