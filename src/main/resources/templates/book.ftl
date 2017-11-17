<html>
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
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