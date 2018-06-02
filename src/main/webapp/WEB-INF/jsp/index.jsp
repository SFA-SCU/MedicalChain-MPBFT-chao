<%--
  Created by IntelliJ IDEA.
  User: chao
  Date: 2018/5/25
  Time: 10:22
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>主页</title>
</head>
<body>
    <input type="button" value="添加" onclick="window.location.href='record/add'"/> <br>
    <form action="record/query" method="get">
        <input type="text" name="reportCardId"/>
        <input type="submit" value="查询"/>
    </form>
</body>
</html>
