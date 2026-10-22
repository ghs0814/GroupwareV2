<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%
    // [1순위 고정] 자바 로직이 돌기 전, 요청과 응답 스트림의 구멍을 UTF-8로 선제 타격해서 열어둡니다.
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- [2순위 고정] JSTL이 브라우저 언어헤더를 읽어 동적으로 프로퍼티를 선택할 때, 깨지지 않도록 인코딩 필터 주입 --%>
<fmt:requestEncoding value="UTF-8" />
<fmt:setBundle basename="resources.message" scope="session" />

<!DOCTYPE html>
<html>
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
    <meta charset="UTF-8">
    <title><fmt:message key="findPw.page.title" /></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/findPw.css">
</head>
<body class="auth-body">
    <div class="auth-container">
        <div class="auth-logo">
            <span class="bold"><fmt:message key="login.brand.bold" /></span>
            <span class="thin"><fmt:message key="login.brand.thin" /></span>
        </div>
        <h2 class="auth-title"><fmt:message key="findPw.page.title" /></h2>
        <p class="auth-subtitle"><fmt:message key="findPw.page.subtitle" /></p>

        <form action="findPw.do" method="post">
            <div class="form-group">
                <label><fmt:message key="join.label.empNo" /></label>
                <%-- HTML 속성 내 따옴표 중충돌 방지를 위한 var 변수 처리 --%>
                <fmt:message key="login.placeholder.empNo" var="plhEmpNo"/>
                <input type="text" name="empNo" class="form-input" placeholder="${plhEmpNo}" required>
            </div>
            <div class="form-group">
                <label><fmt:message key="join.label.name" /></label>
                <fmt:message key="findPw.placeholder.name" var="plhName"/>
                <input type="text" name="empName" class="form-input" placeholder="${plhName}" required>
            </div>
            <div class="btn-group">
                <a href="index.jsp" class="btn-cancel"><fmt:message key="join.btn.cancel" /></a>
                <button type="submit" class="btn-confirm"><fmt:message key="join.btn.check" /></button>
            </div>
        </form>
    </div>
</body>
</html>
