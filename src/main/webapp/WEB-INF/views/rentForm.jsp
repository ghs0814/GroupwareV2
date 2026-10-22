<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.groupware.dto.EquipmentDTO" %>
<%@ page import="java.text.SimpleDateFormat, java.util.Date" %>
<%
    // サーバー基準の今日の日付を yyyy-MM-dd 形式で抽出
    String todayStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
%>
<jsp:useBean id="equipment" type="com.groupware.dto.EquipmentDTO" scope="request"/>
<!DOCTYPE html>
<html lang="ja">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>社内システム - 備品貸出申請書</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/rentForm.css">
</head>
<body>
<jsp:include page="header.jsp" />

<div class="form-container">
    <h2>📋 備品貸出申請書</h2>

    <% if (request.getAttribute("errorMsg") != null) { %>
        <div class="error-box">
            <%= request.getAttribute("errorMsg") %>
        </div>
    <% } %>

    <form action="rentInsert.do" method="post" onsubmit="return validateForm()">
        
        <input type="hidden" id="eqNo" name="eqNo" value="${equipment.eqNo}" />
        <input type="hidden" id="maxRemain" value="${equipment.remainCount}" />

        <table class="info-table">
            <tr>
                <th>備品番号</th>
                <td>${equipment.eqNo}</td>
            </tr>
            <tr>
                <th>備品名</th>
                <td><strong>${equipment.eqName}</strong></td>
            </tr>
            <tr>
                <th>貸出可能数量</th>
                <td>
                    <span class="${equipment.remainCount == 0 ? 'text-danger' : 'remain-count'}">
                        ${equipment.remainCount}
                    </span>
                    <span class="total-count">
                        / ${equipment.totalCount} EA
                    </span>
    
                    <% if (equipment.getRemainCount() == 0) { %>
                        <span class="text-danger">(現在、貸出可能な在庫がありません)</span>
                    <% } %>
                </td>
            </tr>
        </table>

        <div class="form-group">
            <label for="rentCount">申請数量 (EA)</label>
            <input type="number" id="rentCount" name="rentCount" class="form-control" 
                   value="1" min="1" max="${equipment.remainCount}" required
                   ${equipment.remainCount == 0 ? 'disabled' : ''}>
        </div>

		<div class="form-group">
		    <label for="rentalDate">貸出開始日</label>
		    <input type="date" id="rentalDate" name="rentalDate" class="form-control" 
		           min="<%= todayStr %>" 
		           required ${equipment.remainCount == 0 ? 'disabled' : ''}>
		</div>
		
		<div class="form-group">
		    <label for="returnDate">返却予定日</label>
		    <input type="date" id="returnDate" name="returnDate" class="form-control" 
		           min="<%= todayStr %>" 
		           required ${equipment.remainCount == 0 ? 'disabled' : ''}>
		</div>

        <div class="form-group">
            <label for="rentPurpose">貸出理由</label>
            <textarea id="rentPurpose" name="content" class="form-control" rows="4" 
                      placeholder="貸出の目的および理由を詳細に入力してください。" required
                      ${equipment.remainCount == 0 ? 'disabled' : ''}></textarea>
        </div>

        <div class="btn-area">
            <button type="submit" class="btn btn-submit" ${equipment.remainCount == 0 ? 'disabled' : ''}>
                ${equipment.remainCount == 0 ? '貸出不可 (在庫切れ)' : '申請書を提出'}
            </button>
            <button type="button" class="btn btn-cancel" onclick="location.href='equipmentList.do'">キャンセル</button>
        </div>
        
    </form>
</div>

<script>
function validateForm() {
    const rentalDateInput = document.getElementById("rentalDate");
    const returnDateInput = document.getElementById("returnDate");
    
    const sDate = rentalDateInput.value;
    const eDate = returnDateInput.value;
    const todayStr = "<%= todayStr %>";

    // 1. 日付入力漏れ防止ガード
    if (!sDate || !eDate) {
        alert("貸出開始日と返却予定日を両方選択してください。");
        if (!sDate) rentalDateInput.focus();
        else returnDateInput.focus();
        return false;
    }

    // 2. 過去日付の選択遮断
    if (sDate < todayStr) {
        alert("貸出開始日に今日より前の日付を選択することはできません。");
        rentalDateInput.focus();
        return false;
    }

    // 3. 貸出終了日が開始일より早い逆転エラーの検証
    if (sDate > eDate) {
        alert("返却予定日は貸出開始日より前の日付に設定できません。");
        returnDateInput.focus();
        return false;
    }

    return confirm("入力された内容で備品の貸出を申請しますか？");
}

/* リアルタイムで貸出開始日に応じて返却日の最小選択範囲を連動させるロック機能 */
document.addEventListener("DOMContentLoaded", function() {
    const rentalInput = document.getElementById("rentalDate");
    const returnInput = document.getElementById("returnDate");
    
    if (rentalInput && returnInput) {
        rentalInput.addEventListener("change", function() {
            if (this.value) {
                returnInput.min = this.value; 
                if (returnInput.value && returnInput.value < this.value) {
                    returnInput.value = this.value;
                }
            }
        });
    }
});
</script>

</body>
</html>
