<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.groupware.dto.EquipmentDTO" %>
<%@ page import="com.groupware.dto.EmployeeDTO" %> 

<%
    EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
    if (loginEmp == null) {
        response.sendRedirect("index.jsp");
        return;
    }

    List<EquipmentDTO> eqList = (List<EquipmentDTO>) request.getAttribute("eqList");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>社内システム - 備品貸出申請</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/equipmentList.css?v=1.1">
</head>
<body>
    
    <jsp:include page="header.jsp" />
    
    <div class="table-container" style="margin-top: 30px; margin-bottom: 0;">
        <div class="headertitle">
            <h2 style="margin: 0; font-size: 26px; font-weight: 700; color: #1e293b;">備品貸出申請</h2>
        </div>
    </div>
    
    <div class="search-bar-container">
	    <div class="search-form">
	        <select id="searchType" class="search-select">
	            <option value="all">全体検索</option>
	            <option value="eqName">備品名</option>
	            <option value="eqNo">備品番号</option>
	        </select>
	        <input type="text" id="searchKeyword" class="search-input" placeholder="移動する備品名または番号を入力..." autocomplete="off" onkeyup="if(event.key === 'Enter') searchAndScroll()">
	        <button type="button" class="btn-search" onclick="searchAndScroll()">検索・移動</button>
	    </div>
	</div>

    <div class="table-container" style="margin-top: 10px;">
        <table class="eq-table">
            <thead>
                <tr>
                    <th style="width: 15%; text-align: center;">備品番号</th>
                    <th style="width: 45%; text-align: center;">備品名</th>
                    <th style="width: 25%; text-align: center;">貸出可能数量 (残り/全体)</th>
                    <th style="width: 15%; text-align: center;">申請</th>
                </tr>
            </thead>
            <tbody>
                <% if (eqList != null && !eqList.isEmpty()) {
                    for (EquipmentDTO eq : eqList) {
                %>
                    <tr class="eq-row">
                        <td class="eq-no" style="text-align: center;"><%= eq.getEqNo() %></td>
                        
                        <td class="eq-name" style="text-align: center;"><%= eq.getEqName() %></td>
                        
                        <td style="text-align: center;" class="eq-count <%= eq.getRemainCount() == 0 ? "text-danger" : "" %>">
                            <b><%= eq.getRemainCount() %></b> / <%= eq.getTotalCount() %> EA
                        </td>
                        
                        <td style="text-align: center;">
                            <% if (eq.getRemainCount() > 0) { %>
                                <button class="btn-rent" onclick="location.href='rentForm.do?eqNo=<%= eq.getEqNo() %>'">貸出申請</button>
                            <% } else { %>
                                <button class="btn-disabled" disabled>在庫切れ</button>
                            <% } %>
                        </td>
                    </tr>
                <% } } else { %>
                    <tr>
                        <td colspan="4" style="text-align: center; color: #64748b; padding: 50px; font-size: 15px;">登録されている備品がありません。</td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>

    <%
        Integer currentPage = (Integer) request.getAttribute("currentPage");
        Integer totalPages = (Integer) request.getAttribute("totalPages");
        Integer startPage = (Integer) request.getAttribute("startPage");
        Integer endPage = (Integer) request.getAttribute("endPage");
        
        if (totalPages != null && totalPages > 1) {
    %>
    <div class="pagination-container" style="text-align: center; margin-top: 20px; margin-bottom: 40px;">
        <div class="pagination">
            <% if (startPage > 1) { %>
                <a href="equipmentList.do?page=<%= startPage - 1 %>" class="page-link">&laquo; 前へ</a>
            <% } %>

            <% for (int i = startPage; i <= endPage; i++) { %>
                <a href="equipmentList.do?page=<%= i %>" class="page-link <%= (i == currentPage) ? "active" : "" %>"><%= i %></a>
            <% } %>

            <% if (endPage < totalPages) { %>
                <a href="equipmentList.do?page=<%= endPage + 1 %>" class="page-link">次へ &raquo;</a>
            <% } %>
        </div>
    </div>
    <% } %>

</body>
</html>

<script>
// 全変数の設定: オリジナルテキスト、前回の検索キーワード、現在のフォーカスインデックスの記録
const originalTexts = new Map();
let lastKeyword = "";
let currentMatchIndex = -1;

window.addEventListener('DOMContentLoaded', () => {
    // ページロード時に各セルの純粋なオリジナルテキストのみメモリに記録
    document.querySelectorAll('.eq-no, .eq-name').forEach((td, index) => {
        td.setAttribute('data-search-id', index);
        originalTexts.set(index.toString(), td.textContent.trim());
    });
});

function searchAndScroll() {
    const searchType = document.getElementById("searchType").value;
    const keyword = document.getElementById("searchKeyword").value.trim();
    const lowerKeyword = keyword.toLowerCase();

    if (keyword === "") {
        alert("検索キーワードを入力してください。");
        return;
    }

    // [コアロジック] 新しい単語を検索した場合: マーキングを新しくし、インデックスを初期化する
    if (lastKeyword !== lowerKeyword) {
        // 1. 既存のハイライトマーキングをすべて初期化
        document.querySelectorAll('.eq-no, .eq-name').forEach(td => {
            const id = td.getAttribute('data-search-id');
            if (originalTexts.has(id)) {
                td.innerHTML = originalTexts.get(id); 
            }
        });
        // 2. ループを回して一致するすべての項目にハイライトマーキングを表示
        const rows = document.querySelectorAll(".eq-row");
        rows.forEach(row => {
            const eqNoTd = row.querySelector(".eq-no");
            const eqNameTd = row.querySelector(".eq-name");

            if (searchType === "all" || searchType === "eqNo") {
                markCellIfMatch(eqNoTd, lowerKeyword);
            }
            if (searchType === "all" || searchType === "eqName") {
                markCellIfMatch(eqNameTd, lowerKeyword);
            }
        });
        // 状態記録の更新
        lastKeyword = lowerKeyword;
        currentMatchIndex = -1;
    }

    // 3. 画面に生成されたすべてのマーキング要素のリストを収集
    const allMarks = document.querySelectorAll(".mark-highlight");
    if (allMarks.length > 0) {
        // 次のインデックスに移動 (最後の項目に達したら最初の項目に戻る)
        currentMatchIndex++;
        if (currentMatchIndex >= allMarks.length) {
            currentMatchIndex = 0;
        }

        // 4. 選択された順序の次のマーキング位置へスムーズにスクロール移動
        allMarks[currentMatchIndex].scrollIntoView({
            behavior: "smooth",
            block: "center"
        });
        // 5. 次の検索がスムーズに行えるようテキストボックスを選択状態にする
        document.getElementById("searchKeyword").select();
    } else {
        alert("一致する備品が見つかりません。");
        document.getElementById("searchKeyword").focus();
        lastKeyword = "";
        currentMatchIndex = -1;
    }
}

// テキストマッチングおよび実際のタグ置換関数
function markCellIfMatch(td, lowerKeyword) {
    if (!td) return false;
    const originalText = td.textContent.trim();
    const lowerText = originalText.toLowerCase();
    const index = lowerText.indexOf(lowerKeyword);
    if (index >= 0) {
        const escapedKeyword = lowerKeyword.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
        const regex = new RegExp(escapedKeyword, "gi");
        td.innerHTML = originalText.replace(regex, `<span class="mark-highlight">$&</span>`);
        return true; 
    }
    return false; 
}
</script>
