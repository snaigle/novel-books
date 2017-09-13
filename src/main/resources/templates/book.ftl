<html>
    <head>
    <title>${title}</title>
    <script script src="index.js"></script>

    </head>
    <body>
        <ul>
            <#list items as it>
                <li><a href="${it.value}">${it.key}</a></li>
            </#list>
        </ul>
    </body>
<html>