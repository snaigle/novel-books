<html>
    <head>
    <title>${title}</title>
    <style>
        body {
            font-size:3em;
        }
    </style>
    </head>
    <body>
        <ul>
            <#list items as it>
                <li><a href="${it.value}">${it.key}</a></li>
            </#list>
        </ul>
    </body>
<html>