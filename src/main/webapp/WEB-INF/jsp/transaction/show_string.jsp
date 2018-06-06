<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
    <title>管理</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- 引入 Bootstrap -->
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/my.css" rel="stylesheet">
    <!-- HTML5 Shiv 和 Respond.js 用于让 IE8 支持 HTML5元素和媒体查询 -->
    <!-- 注意： 如果通过 file://  引入 Respond.js 文件，则该文件无法起效果 -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
    <![endif]-->
</head>
<body>
<div class="container pull-left" style="width: 100%;">
    <div class="row clearfix">
        <div class="col-md-12 column">
            <div class="row clearfix">
                <div class="col-md-12 column">
                    <ul class="breadcrumb">
                        <li>
                            <a href="#">Home</a>
                        </li>
                        <li>
                            <a href="#">Library</a>
                        </li>
                        <li class="active">
                            Data
                        </li>
                    </ul>
                </div>
            </div>
            <div class="row clearfix" style="width: 100%;height: 100%; margin-top: 0px">
                <div class="col-md-2 column"
                     style="border: 1px solid gray;height: 100%;background-color: #213039;margin-top: 0px">
                    <br>
                    <ul class="nav nav-stacked nav-pills">
                        <li>
                            <a href="${pageContext.request.contextPath}/tx/index"><span style="color: #dbdbdb; font-size: 20px; font-weight: bold">首页</span></a>
                        </li>
                        <!--
                        <li class="dropdown pull-right">
                            <a href="#" data-toggle="dropdown" class="dropdown-toggle">下拉<strong class="caret"></strong></a>
                            <ul class="dropdown-menu">
                                <li>
                                    <a href="#">操作</a>
                                </li>
                                <li>
                                    <a href="#">设置栏目</a>
                                </li>
                                <li>
                                    <a href="#">更多设置</a>
                                </li>
                                <li class="divider">
                                </li>
                                <li>
                                    <a href="#">分割线</a>
                                </li>
                            </ul>
                        </li>
                        -->
                    </ul>
                </div>
                <div class="col-md-10 column">
                    <h2>
                        信息
                    </h2>
                    <br>
                    <form action="${pageContext.request.contextPath}/record/save" method="post">
                        <table class="table table-bordered">
                            <tr>
                                <th>
                                    <span>交易单ID：</span>
                                </th>
                                <td>
                                    <input type="text" class="form-control" name="id" value="${tx.txId}" readonly="readonly">
                                </td>
                            </tr>
                            <tr>
                                <th>
                                    <span>所在区块ID：</span>
                                </th>
                                <td>
                                    <a href="${pageContext.request.contextPath}/block/search?blockId=${blockId}">
                                        <input type="text" class="form-control" name="id" value="${blockId}" readonly="readonly">
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <th>
                                    <span>数字签名：</span>
                                </th>
                                <td>
                                    <input type="text" class="form-control" name="id" value="${tx.signature}" readonly="readonly">
                                </td>
                            </tr>
                            <tr>
                                <th>
                                    <span>交易单类型：</span>
                                </th>
                                <td>
                                    <input type="text" class="form-control" name="id" value="${tx.txType}" readonly="readonly">
                                </td>
                            </tr>
                            <tr>
                                <th>
                                    <span>公钥：</span>
                                </th>
                                <td>
                                    <input type="text" class="form-control" name="id" value="${tx.pubKey}" readonly="readonly">
                                </td>
                            </tr>
                            <tr>
                                <th>
                                    <span>时间戳：</span>
                                </th>
                                <td>
                                    <input type="text" class="form-control" name="id" value="${tx.timestamp}" readonly="readonly">
                                </td>
                            </tr>

                            <tr>
                                <th>
                                    <span>内容：</span>
                                </th>
                                <td>
                                    <input type="text" class="form-control" name="id" value="${tx.content.string}" readonly="readonly">
                                </td>
                            </tr>

                        </table>
                        <button type="submit" class="btn btn-primary btn-lg">提交修改</button>
                    </form>


                </div>
            </div>
            <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle" data-toggle="collapse"
                            data-target="#bs-example-navbar-collapse-1"><span
                            class="sr-only">Toggle navigation</span><span class="icon-bar"></span><span
                            class="icon-bar"></span><span class="icon-bar"></span></button>
                    <a class="navbar-brand" href="#">Medical Chain</a>
                </div>

                <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                    <ul class="nav navbar-nav">
                        <li>
                            <a href="${pageContext.request.contextPath}/">首页</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/block/blockchain">区块链</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/block/index">区块</a>
                        </li>
                        <li class="active">
                            <a href="${pageContext.request.contextPath}/tx/index">交易单</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/node/show">节点</a>
                        </li>
                    </ul>
                    <form class="navbar-form navbar-left" role="search"
                          action="${pageContext.request.contextPath}/tx/search">
                        <div class="form-group">
                            <input type="text" class="form-control" placeholder="交易单ID" name="txId"/>
                        </div>
                        <button type="submit" class="btn btn-default">搜索</button>
                    </form>
                    <ul class="nav navbar-nav navbar-right">
                        <li>
                            <a href="#">&nbsp;</a>
                        </li>
                        <li class="dropdown">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">更多<strong class="caret"></strong></a>
                            <ul class="dropdown-menu">
                                <li>
                                    <a href="#">Action</a>
                                </li>
                                <li>
                                    <a href="#">Another action</a>
                                </li>
                                <li>
                                    <a href="#">Something else here</a>
                                </li>
                                <li class="divider">
                                </li>
                                <li>
                                    <a href="#">Separated link</a>
                                </li>
                            </ul>
                        </li>
                    </ul>
                </div>

            </nav>
        </div>
    </div>
</div>

<!-- jQuery (Bootstrap 的 JavaScript 插件需要引入 jQuery) -->
<script src="https://code.jquery.com/jquery.js"></script>
<!-- 包括所有已编译的插件 -->
<script src="js/bootstrap.min.js"></script>
</body>
</html>
