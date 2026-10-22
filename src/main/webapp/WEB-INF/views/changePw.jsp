<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setBundle basename="resources.message" scope="session" />

<!DOCTYPE html>
<html>
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
    <meta charset="UTF-8">
    <title><fmt:message key="password.change.title" /></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/findPw.css">
</head>
<body class="auth-body">
    <div class="auth-container">
        <h2 class="auth-title"><fmt:message key="password.change.heading" /></h2>
        
        <%-- 💡 컨트롤러에서 번역해서 보낸 에러 메시지(msg)가 있을 때만 화면에 표시 --%>
        <c:if test="${not empty requestScope.msg}">
            <div class="error-msg" style="color: red; text-align: center; margin-bottom: 15px; font-weight: bold;">
                ${requestScope.msg}
            </div>
        </c:if>

        <form action="changePw.do" method="post">
            <div class="form-group">
                <label><fmt:message key="password.change.current" /></label>
                <input type="password" name="currentPw" class="form-input" required>
            </div>
            <div class="form-group">
                <label><fmt:message key="password.change.new" /></label>
                <input type="password" name="newPw" class="form-input" required>
            </div>
            <div class="form-group">
                <label><fmt:message key="password.change.confirm" /></label>
                <input type="password" name="newPwConfirm" class="form-input" required>
            </div>

            <button type="submit" class="btn-confirm" style="width: 100%; margin-top: 10px;">
                <fmt:message key="password.change.btn.save" />
            </button>
            <a href="myPage.jsp" class="btn-cancel" style="width: 100%; margin-top: 10px;">
                <fmt:message key="password.change.btn.cancel" />
            </a>
        </form>
    </div>
</body>
</html>
