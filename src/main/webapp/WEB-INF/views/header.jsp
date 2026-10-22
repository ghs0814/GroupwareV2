<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ page import="com.groupware.dto.EmployeeDTO"%>

<% 
    // 로그인 체크 세션
    EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
    if (loginEmp == null) {
        response.sendRedirect("index.jsp");
        return;
    }
%>

<%-- 다국어 설정: 브라우저 언어 자동 감지 --%>
<fmt:requestEncoding value="UTF-8" />
<fmt:setBundle basename="resources.message" />

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/header.css?v=1.5">
<title>GroupWare</title>
</head>
<body>
    <div class="header">
        <div class="header-inner">
            <a href="main.jsp" class="logo-area">
                <span class="logo-group">Group</span><span class="logo-ware">Ware</span>
            </a>
            
            <div class="nav-buttons">
                <span class="user-profile-info">
                    <% if ("Y".equals(loginEmp.getManager())) { %>
                        <span class="admin-tag">ADMIN</span>
                    <% } %>
                    <b>${loginEmp.empName}</b><fmt:message key="user.suffix" />
                </span>

                <% if ("Y".equals(loginEmp.getManager())) { %>
                    <a href="adminEqList.do" class="nav-btn admin-special"><fmt:message key="nav.admin.stock" /></a>
                    <a href="admin.do" class="nav-btn admin-special"><fmt:message key="nav.admin.emp" /></a>
                <% } %>

                <a href="officeMap.jsp" class="nav-btn"><fmt:message key="nav.office.reservation" /></a>
                <a href="leaveForm.do" class="nav-btn"><fmt:message key="nav.leave.apply" /></a>
                <a href="equipmentList.do" class="nav-btn"><fmt:message key="nav.rental.apply" /></a>
                <a href="documentList.do" class="nav-btn"><fmt:message key="nav.document.box" /></a>
                <a href="boardList.do" class="nav-btn"><fmt:message key="nav.board" /></a>
                <a href="addressBook.do" class="nav-btn"><fmt:message key="nav.address.book" /></a>
                <a href="myPage.do" class="nav-btn"><fmt:message key="nav.mypage" /></a>
                <a href="logout.do" class="nav-btn logout"><fmt:message key="nav.logout" /></a>
            </div>
        </div>
    </div>
</body>
</html>
