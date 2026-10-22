<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.groupware.dto.BoardDTO" %>
<%@ page import="com.groupware.dto.BoardFileDTO" %>
<%@ page import="java.util.List" %>
<%
    BoardDTO board = (BoardDTO) request.getAttribute("board");
    List<BoardFileDTO> fileList = (List<BoardFileDTO>) request.getAttribute("fileList");
    Integer typeAttr = (Integer) request.getAttribute("type");
    int type = (typeAttr != null) ? typeAttr : (board != null ? board.getBoardType() : 1);
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<title><%= (board == null) ? "新規投稿" : "投稿編集" %> - GroupWare</title>
<link rel="stylesheet" href="css/board.css">
</head>
<body>
    <jsp:include page="header.jsp" />

    <div class="board-container">
        <div class="board-header">
            <h2>
                <% if (board == null) { %>
                    <% if (type == 1) out.print("社内ニュース作成");
                       else if (type == 2) out.print("フリートーク投稿作成");
                       else if (type == 3) out.print("改善要望作成");
                    %>
                <% } else { %>
                    投稿の編集
                <% } %>
            </h2>
        </div>

        <form action="<%= (board == null) ? "boardWrite.do" : "boardUpdate.do" %>" method="post" enctype="multipart/form-data" class="write-form">
            <input type="hidden" name="type" value="<%= type %>">
            <% if (board != null) { %>
                <input type="hidden" name="boardNo" value="<%= board.getBoardNo() %>">
            <% } %>

            <table>
                <tr>
                    <th>タイトル</th>
                    <td><input type="text" name="title" value="<%= (board != null) ? board.getTitle() : "" %>" required placeholder="タイトルを入力してください"></td>
                </tr>
                <tr>
                    <th>内容</th>
                    <td><textarea name="content" required placeholder="内容を入力してください"><%= (board != null) ? board.getContent() : "" %></textarea></td>
                </tr>
                <tr>
                    <th>添付ファイル</th>
                    <td>
                        <% if (fileList != null && !fileList.isEmpty()) { %>
                            <div style="margin-bottom:10px;">
                                <p style="margin:0 0 5px 0; font-size:13px; color:#666;"> 既存ファイル (削除する場合はチェック)</p>
                                <% for (BoardFileDTO file : fileList) { %>
                                    <div style="margin-bottom:5px;">
                                        <label style="font-size:14px;">
                                            <input type="checkbox" name="delFiles" value="<%= file.getFileNo() %>"> <%= file.getOrgName() %>
                                        </label>
                                    </div>
                                <% } %>
                            </div>
                        <% } %>
                        <input type="file" name="files" multiple>
                        <p style="margin:5px 0 0 0; font-size:12px; color:#999;"> 複数ファイルを選択できます。(最大10MB)</p>
                    </td>
                </tr>
            </table>

            <div style="margin-top:30px; text-align:center; display:flex; gap:10px; justify-content: center;">
                <button type="submit" class="btn btn-primary"><%= (board == null) ? "登録する" : "修正完了" %></button>
                <a href="javascript:history.back();" class="btn btn-outline">キャンセル</a>
            </div>
        </form>
    </div>
</body>
</html>
