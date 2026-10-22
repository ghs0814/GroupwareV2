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
<html lang="ko">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>GroupWare - Login</title>

<link rel="stylesheet" href="css/index.css?v=1"> <%-- ?v=1을 붙이면 새로고침을 강제합니다 --%>

<link
	href="https://fonts.googleapis.com/css2?family=Inter:wght@300;800&family=Noto+Sans+KR:wght@400;700&display=swap"
	rel="stylesheet">
</head>
<body>

	<div class="main-container">

		<div class="login-card">

			<header class="brand-header">
				<div class="brand-logo">
					<span class="bold"><fmt:message key="login.brand.bold" /><!-- Group --></span>
					<span class="thin"><fmt:message key="login.brand.thin" /><!-- Ware --></span>
				</div>
			</header>

			<%-- 
                로그인 폼 설정 
                1. action="login.do": 사용자가 입력한 데이터를 LoginController 서블릿으로 전송
                2. method="post": 아이디와 비밀번호가 주소창에 노출되지 않도록 보안 처리
            --%>
			<form action="login.do" method="post">

				<div class="input-wrap">
					<%-- 
                        name="empNo": 서버(Java) 측에서 request.getParameter("empNo")로 
                        데이터를 받기 위한 식별자입니다. 
                    --%>
					<input type="text" name="empNo" placeholder="<fmt:message key="login.placeholder.empNo" />" required
						autocomplete="off">
				</div>

				<div class="input-wrap">
					<%-- 
                        name="password": 서버 측에서 request.getParameter("password")로 
                        데이터를 받기 위한 식별자입니다. 
                    --%>
					<input type="password" name="password" placeholder="<fmt:message key="login.placeholder.password" />"
						required>
				</div>

				<%-- 
                    [동적 메시지 출력] 
                    로그인 실패 시 서블릿(Controller)에서 request.setAttribute("msg", "...")에 담아 보낸 
                    에러 메시지를 화면에 출력하는 로직입니다.
                --%>
				<% if(request.getAttribute("msg") != null) { %>
				<div class="error-msg"
					style="color: #ff6b6b; font-size: 13px; margin-bottom: 15px; text-align: center; font-weight: 600;">
					<%= request.getAttribute("msg") %>
				</div>
				<% } %>

				<button type="submit" class="login-submit"><fmt:message key = "login.btn.submit"/></button>
			</form>

				<div class="link-footer">
					<%-- 계정 등록(join.jsp)과 비밀번호 찾기(findPw.jsp)로 이동하는 링크 --%>
					<a href="join.jsp"><fmt:message key = "login.link.join"/></a> <span class="sep">|</span> <a
						href="findPw.jsp"><fmt:message key = "login.link.findPw"/></a>
				</div>
			</div>
		</div>
	</body>
</html>
