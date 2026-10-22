<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.groupware.dto.EmployeeDTO" %>
<%@ page import="com.groupware.dto.BoardDTO" %>
<%@ page import="com.groupware.dto.BoardFileDTO" %>
<%@ page import="com.groupware.dto.CommentDTO" %>
<%@ page import="java.util.List" %>
<%
    EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
    BoardDTO board = (BoardDTO) request.getAttribute("board");
    List<BoardFileDTO> fileList = (List<BoardFileDTO>) request.getAttribute("fileList");
    List<CommentDTO> commentList = (List<CommentDTO>) request.getAttribute("commentList");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<title><%= (board != null) ? board.getTitle() : "" %> - GroupWare</title>
<link rel="stylesheet" href="css/board.css">
</head>
<body>
    <jsp:include page="header.jsp" />

    <div class="board-container">
        <div class="board-header">
            <h2>
                <% 
                if (board != null) {
                    int bType = board.getBoardType();
                    if (bType == 1) out.print("社内ニュース");
                    else if (bType == 2) out.print("フリートーク");
                    else if (bType == 3) out.print("改善要望掲示板");
                }
                %>
            </h2>
        </div>

        <% if (board != null) { %>
        <div class="detail-view">
            <h3 style="padding:20px 15px; margin:0; border-bottom:1px solid #ddd;"><%= board.getTitle() %></h3>
            <div class="detail-info">
                <span>投稿者: <b><%= board.getEmpName() %></b></span>
                <span>投稿日: <%= board.getRegDate() %> |
                閲覧数: <%= board.getHit() %></span>
            </div>
            
            <div class="detail-content"><%= board.getContent() %></div>

            <% if (fileList != null && !fileList.isEmpty()) { %>
                <div class="file-list">
                    <p style="margin-top:0; font-weight:600;">添付ファイル</p>
                    <% for (BoardFileDTO file : fileList) { %>
                        <div class="file-item">
                            <span class="icon">📁</span>
                            <a href="fileDownload.do?fileNo=<%= file.getFileNo() %>"><%= file.getOrgName() %></a>
                            <span style="color:#999; font-size:12px;">(<%= file.getFileSize() / 1024 %> KB)</span>
                        </div>
                    <% } %>
                </div>
            <% } %>

            <div style="margin-top:30px; display:flex; justify-content: space-between;">
                <a href="boardList.do?type=<%= board.getBoardType() %>" class="btn btn-outline">一覧に戻る</a>
                
                <div>
                    <% if (loginEmp != null && (board.getEmpNo() == loginEmp.getEmpNo() ||
                        "Y".equals(loginEmp.getManager()))) { %>
                        <a href="boardUpdate.do?boardNo=<%= board.getBoardNo() %>" class="btn btn-outline">修正</a>
                        <a href="javascript:void(0);"
                           onclick="deleteConfirm(<%= board.getBoardNo() %>);" class="btn btn-outline" style="color:#e74c3c;">削除</a>
                    <% } %>
                </div>
            </div>

            <div class="comment-section">
                <div class="comment-count">コメント <%= (commentList != null) ? commentList.size() : 0 %>件</div>
                
                <% if (loginEmp != null) { %>
                <form action="commentInsert.do" method="post" class="comment-form">
                    <input type="hidden" name="boardNo" value="<%= board.getBoardNo() %>">
                    <textarea name="content" placeholder="コメントを入力してください" required></textarea>
                    <div style="text-align:right;">
                        <button type="submit" class="btn btn-primary">登録</button>
                    </div>
                </form>
                <% } %>

                <div class="comment-list">
                    <% if (commentList != null && !commentList.isEmpty()) { 
                        for (CommentDTO comment : commentList) { %>
                            <div class="comment-item depth-<%= comment.getDepth() %>">
                            <div class="comment-info">
                                <span class="comment-author"><%= comment.getEmpName() %></span>
                                <span class="comment-date"><%= comment.getRegDate() %></span>
                            </div>
                            <div class="comment-content" id="comment-text-<%= comment.getCommentNo() %>"><%= comment.getContent() %></div>
                            
                            <div class="comment-actions">
                                <% if (loginEmp != null) { %>
                                    <a href="javascript:void(0);"
                                       onclick="toggleReplyForm(<%= comment.getCommentNo() %>)">返信</a>
                                    <% if (comment.getEmpNo() == loginEmp.getEmpNo() || "Y".equals(loginEmp.getManager())) { %>
                                        <a href="javascript:void(0);"
                                           onclick="toggleEditForm(<%= comment.getCommentNo() %>)">修正</a>
                                        <a href="javascript:void(0);"
                                           onclick="deleteComment(<%= comment.getCommentNo() %>, <%= board.getBoardNo() %>)" style="color:#e74c3c;">削除</a>
                                    <% } %>
                                <% } %>
                            </div>

                            <div id="reply-form-<%= comment.getCommentNo() %>" class="reply-form">
                                <form action="commentInsert.do" method="post" class="comment-form" style="margin:0; padding:10px; background:transparent;">
                                    <input type="hidden" name="boardNo" value="<%= board.getBoardNo() %>">
                                    <input type="hidden" name="parentNo" value="<%= comment.getCommentNo() %>">
                                    <textarea name="content" placeholder="返信を入力してください" required></textarea>
                                    <div style="text-align:right;">
                                        <button type="button" class="btn btn-outline" onclick="toggleReplyForm(<%= comment.getCommentNo() %>)" style="margin-right:5px;">キャンセル</button>
                                        <button type="submit" class="btn btn-primary">登録</button>
                                    </div>
                                </form>
                            </div>

                            <div id="edit-form-<%= comment.getCommentNo() %>" class="edit-form">
                                <form action="commentUpdate.do" method="post" class="comment-form" style="margin:0; padding:10px; background:transparent;">
                                    <input type="hidden" name="commentNo" value="<%= comment.getCommentNo() %>">
                                    <input type="hidden" name="boardNo" value="<%= board.getBoardNo() %>">
                                    <textarea name="content" required><%= comment.getContent() %></textarea>
                                    <div style="text-align:right;">
                                        <button type="button" class="btn btn-outline" onclick="toggleEditForm(<%= comment.getCommentNo() %>)" style="margin-right:5px;">キャンセル</button>
                                        <button type="submit" class="btn btn-primary">修正完了</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    <% } 
                } else { %>
                        <p style="text-align:center; color:#999; padding:20px;">登録されたコメントがありません。</p>
                <% } %>
                </div>
            </div>
        </div>
        <% } %>
    </div>

    <script>
        function deleteConfirm(boardNo) {
            if(confirm('本当にこの投稿を削除しますか？')) {
                location.href = 'boardDelete.do?boardNo=' + boardNo + '&csrfToken=' + encodeURIComponent(window.groupwareCsrfToken);
            }
        }

        function toggleReplyForm(commentNo) {
            const form = document.getElementById('reply-form-' + commentNo);
            form.style.display = (form.style.display === 'block') ? 'none' : 'block';
        }

        function toggleEditForm(commentNo) {
            const form = document.getElementById('edit-form-' + commentNo);
            form.style.display = (form.style.display === 'block') ? 'none' : 'block';
            
            const text = document.getElementById('comment-text-' + commentNo);
            text.style.display = (form.style.display === 'block') ? 'none' : 'block';
        }

        function deleteComment(commentNo, boardNo) {
            if(confirm('本当にこのコメントを削除しますか？')) {
                location.href = 'commentDelete.do?commentNo=' + commentNo + '&boardNo=' + boardNo + '&csrfToken=' + encodeURIComponent(window.groupwareCsrfToken);
            }
        }
    </script>
</body>
</html>
