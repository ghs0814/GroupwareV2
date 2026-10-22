<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.groupware.dto.BoardDTO" %>
<%@ page import="com.groupware.dto.EmployeeDTO" %>
<%
    EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
    List<BoardDTO> boardList = (List<BoardDTO>) request.getAttribute("boardList");
    int type = (Integer) request.getAttribute("type");
    int currentPage = (Integer) request.getAttribute("currentPage");
    int totalPages = (Integer) request.getAttribute("totalPages");
    String searchType = (String) request.getAttribute("searchType");
    String keyword = (String) request.getAttribute("keyword");
    if (searchType == null) searchType = "";
    if (keyword == null) keyword = "";
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<title>掲示板 - GroupWare</title>
<link rel="stylesheet" href="css/board.css">
</head>
<body>
    <jsp:include page="header.jsp" />
	
    <div class="board-container">
        <div class="board-header">
            <h2>掲示板</h2>
            <div class="board-tabs">
                <a href="boardList.do?type=1" class="board-tab <%= (type == 1) ? "active" : "" %>">社内ニュース</a>
                <a href="boardList.do?type=2" class="board-tab <%= (type == 2) ? "active" : "" %>">フリートーク</a>
                <a href="boardList.do?type=3" class="board-tab <%= (type == 3) ? "active" : "" %>">改善要望掲示板</a>
            </div>
        </div>

        <table class="board-table">
            <thead>
                <tr>
                    <th width="80">No.</th>
                    <th>タイトル</th>
                    <th width="120">投稿者</th>
                    <th width="150">投稿日</th>
                    <th width="80">閲覧数</th>
                </tr>
            </thead>
            <tbody>
                <% if (boardList != null && !boardList.isEmpty()) { 
                    for (BoardDTO board : boardList) { %>
                    <tr>
                        <td><%= board.getBoardNo() %></td>
                        <td class="title-cell">
                            <a href="boardDetail.do?boardNo=<%= board.getBoardNo() %>"><%= board.getTitle() %></a>
                            <% if (board.getFileCount() > 0) { %>
                                <span style="font-size:12px; color:#999;">[<%= board.getFileCount() %>]</span>
                            <% } %>
                        </td>
                        <td><%= board.getEmpName() %></td>
                        <td><%= board.getRegDate() %></td>
                        <td><%= board.getHit() %></td>
                    </tr>
                <% } } else { %>
                    <tr>
                    <td colspan="5" style="padding:50px 0; color:#999;">登録された投稿がありません。</td>
                    </tr>
                <% } %>
            </tbody>
        </table>

        <div class="pagination">
            <% if (currentPage > 1) { %>
                <a href="boardList.do?type=<%= type %>&page=<%= currentPage - 1 %>&searchType=<%= searchType %>&keyword=<%= keyword %>">&laquo;</a>
            <% } %>
            <% for (int i = 1; i <= totalPages; i++) { %>
                <a href="boardList.do?type=<%= type %>&page=<%= i %>&searchType=<%= searchType %>&keyword=<%= keyword %>" class="<%= (currentPage == i) ? "active" : "" %>"><%= i %></a>
            <% } %>
            <% if (currentPage < totalPages) { %>
                <a href="boardList.do?type=<%= type %>&page=<%= currentPage + 1 %>&searchType=<%= searchType %>&keyword=<%= keyword %>">&raquo;</a>
            <% } %>
        </div>

        <div class="board-footer">
            <form action="boardList.do" method="get" class="search-form">
                <input type="hidden" name="type" value="<%= type %>">
                <select name="searchType">
                    <option value="title" <%= "title".equals(searchType) ? "selected" : "" %>>タイトル</option>
                    <option value="content" <%= "content".equals(searchType) ? "selected" : "" %>>内容</option>
                    <option value="author" <%= "author".equals(searchType) ? "selected" : "" %>>投稿者</option>
                </select>
                <input type="text" name="keyword" value="<%= keyword %>" placeholder="検索キーワードを入力">
                <button type="submit" class="btn btn-outline">検索</button>
            </form>

            <% if (type == 1) { %>
                <% if (loginEmp != null && "Y".equals(loginEmp.getManager())) { %>
                    <a href="boardWrite.do?type=<%= type %>" class="btn btn-primary">新規投稿</a>
                <% } %>
            <% } else { %>
                <a href="boardWrite.do?type=<%= type %>" class="btn btn-primary">新規投稿</a>
            <% } %>
        </div>
    </div>
</body>
</html>
