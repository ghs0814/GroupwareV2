<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:requestEncoding value="UTF-8" />
<fmt:setBundle basename="resources.message" scope="session" />

<!DOCTYPE html>
<html>
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
    <meta charset="UTF-8">
    <title>Groupware - <fmt:message key="pw.reset.title" /></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/findPw.css">
</head>
<body class="auth-body">
    <div class="auth-container">
        <h2 class="auth-title"><fmt:message key="pw.reset.header" /></h2>
        <p style="text-align: center; color: #666; margin-bottom: 20px;">
            <fmt:message key="pw.reset.description" />
        </p>
        
        <form action="resetPw.do" method="post"> 
            <%-- 안전한 EL 표기법으로 사번 바인딩 (단 한 번만 명시) --%>
            <input type="hidden" name="empNo" value="${sessionScope.resetEmpNo}">
            
            <%-- 새 비밀번호 입력 상자 복구 완료 --%>
            <div class="form-group">
                <label><fmt:message key="pw.reset.newLabel" /></label>
                <fmt:message key="pw.reset.newPlaceholder" var="newPwPlaceholder"/>
                <input type="password" name="newPw" class="form-input" placeholder="${newPwPlaceholder}" required>
            </div>
            
            <%-- 새 비밀번호 확인 입력 상자 --%>
            <div class="form-group">
                <label><fmt:message key="pw.reset.confirmLabel" /></label>
                <fmt:message key="pw.reset.confirmPlaceholder" var="confirmPwPlaceholder"/>
                <input type="password" name="newPwConfirm" class="form-input" placeholder="${confirmPwPlaceholder}" required>
            </div>
            
            <button type="submit" class="btn-confirm" style="width: 100%; margin-top: 10px;">
                <fmt:message key="pw.reset.btnSubmit" />
            </button>
            <a href="index.jsp" class="btn-cancel" style="width: 100%; margin-top: 10px;">
                <fmt:message key="pw.reset.btnCancel" />
            </a>
        </form>
    </div>
</body>
</html>
