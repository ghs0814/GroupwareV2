<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.groupware.dto.RoomDTO" %>
<%@ page import="com.groupware.dto.EmployeeDTO" %>
<%
    EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
    if (loginEmp == null) {
        response.sendRedirect("index.jsp");
        return;
    }

    RoomDTO room = (RoomDTO) request.getAttribute("room");
    if (room == null) {
        response.sendRedirect("adminEqList.do");
        return;
    }
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>社内システム - 会議室情報修正</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/roomUpdate.css">
</head>
<body>

<jsp:include page="header.jsp" />

<div class="form-container">
    <h2>📋 会議室情報修正</h2>
    
    <div class="info-box">
        <h3>会議室ID: <%=room.getRoomId()%></h3>
    </div>

    <form action="roomUpdateProcess.do" method="post" onsubmit="return confirm('修正された内容を保存しますか？');">
        <input type="hidden" name="roomId" value="<%=room.getRoomId()%>">
        
        <div class="form-group">
            <label for="roomName">会議室名</label>
             <input type="text" id="roomName" name="roomName" class="form-control" 
                   value="<%=room.getRoomName()%>" required>
        </div>
        
        <div class="form-group">
            <label for="capacity">収容人数 (名)</label>
            <input type="number" id="capacity" name="capacity" class="form-control" 
                    value="<%=room.getCapacity()%>" min="1" required>
        </div>
        
        <div class="form-group">
            <label for="hasBeam">プロジェクターの有無</label>
            <select id="hasBeam" name="hasBeam" class="form-control">
                <option value="Y" <%="Y".equals(room.getHasBeam()) ? "selected" : ""%>>あり (Y)</option>
                <option value="N" <%="N".equals(room.getHasBeam()) ? "selected" : ""%>>なし (N)</option>
            </select>
        </div>
        
        <div class="form-group">
            <label for="enable">予約可能か否か</label>
            <select id="enable" name="enable" class="form-control">
                <option value="Y" <%="Y".equals(room.getEnable()) ? "selected" : ""%>>可能 (通常運営)</option>
                <option value="N" <%="N".equals(room.getEnable()) ? "selected" : ""%>>メンテナンス中 (予約不可)</option>
            </select>
        </div>
        
        <div class="form-group">
            <label for="description">会議室の詳細説明</label>
            <textarea id="description" name="description" class="form-control" 
                      rows="4" placeholder="会議室に関する補足説明を入力してください。"><%=room.getDescription()%></textarea>
        </div>
        
        <div class="btn-area">
            <button type="submit" class="btn btn-submit">修正内容を保存</button>
            <a href="adminEqList.do" class="btn btn-cancel">キャンセル</a>
        </div>
    </form>
</div>

</body>
</html>
