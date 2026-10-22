<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.time.*, com.groupware.dto.*, com.groupware.dao.*, com.groupware.util.StatusUtil" %>
<%
    // [1순위 고정] 자바 로직이 돌기 전, 요청과 응답 스트림의 구멍을 UTF-8로 선제 타격해서 열어둡니다.
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- [2순위 고정] JSTL이 브라우저 언어헤더를 읽어 동적으로 프로퍼티를 선택할 때, 깨지지 않도록 인코딩 필터 주입 --%>
<fmt:requestEncoding value="UTF-8" />
<fmt:setBundle basename="resources.message" scope="session" />
<%
EmployeeDTO loginEmp = (EmployeeDTO) request.getAttribute("loginEmp");
int currentEmpNo = (Integer) request.getAttribute("currentEmpNo");
boolean isManagerMode = (Boolean) request.getAttribute("isManagerMode");
LocalDateTime currentDateTime = (LocalDateTime) request.getAttribute("currentDateTime");
String currentMapping = (String) request.getAttribute("currentMapping");
int RECORDS_PER_PAGE = (Integer) request.getAttribute("RECORDS_PER_PAGE");
int PAGES_PER_BLOCK = (Integer) request.getAttribute("PAGES_PER_BLOCK");
int resPage = (Integer) request.getAttribute("resPage");
String resPageParam = (String) request.getAttribute("resPageParam");
int rentalPage = (Integer) request.getAttribute("rentalPage");
String rentalPageParam = (String) request.getAttribute("rentalPageParam");
int leavePage = (Integer) request.getAttribute("leavePage");
String leavePageParam = (String) request.getAttribute("leavePageParam");
List<ReservationDTO> rawReserveList = (List<ReservationDTO>) request.getAttribute("rawReserveList");
List<Map<String, Object>> fullReserveList = (List<Map<String, Object>>) request.getAttribute("fullReserveList");
int resTotalCount = (Integer) request.getAttribute("resTotalCount");
int resTotalPages = (Integer) request.getAttribute("resTotalPages");
int resStartPage = (Integer) request.getAttribute("resStartPage");
int resEndPage = (Integer) request.getAttribute("resEndPage");
int resStartIdx = (Integer) request.getAttribute("resStartIdx");
int resEndIdx = (Integer) request.getAttribute("resEndIdx");
List<Map<String, Object>> resSubList = (List<Map<String, Object>>) request.getAttribute("resSubList");
%>
<!DOCTYPE html>
<html lang="ko">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<title>Insert title here</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">
    <script>
    // 회의실 예약 취소 핸들러
    function cancelReserve(resNo) { 
        if (confirm("<fmt:message key="alert.cancel.confirm" />")) {
            saveScroll(); 
            location.href = "cancelReserve.do?resNo=" + resNo + '&from=main' + '&csrfToken=' + encodeURIComponent(window.groupwareCsrfToken); 
        }
    }

        function navigateWithScroll(url) {
            saveScroll();
            location.href = url;
        }
        
        function saveScroll() {
            localStorage.setItem("main_scroll_y", window.scrollY);
        }

        window.addEventListener("DOMContentLoaded", function() {
            var savedScrollY = localStorage.getItem("main_scroll_y");
            if (savedScrollY !== null) {
                window.scrollTo(0, parseInt(savedScrollY)); 
                localStorage.removeItem("main_scroll_y"); 
            }
        });
    </script>
</head>
<body>
<jsp:include page="header.jsp" />
<div class="container">
	<div class="section-title"> 
                <a href="myReservation.jsp" class="data-link"><fmt:message key="table.res.title" /></a>
            </div>
            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                        <th><fmt:message key="table.res.no" /></th>
                        <th><fmt:message key="table.res.room" /></th>
                        <th><fmt:message key="table.res.date" /></th>
                        <th><fmt:message key="table.res.time" /></th>
                        <th><fmt:message key="table.res.purpose" /></th>
                        <th><fmt:message key="table.res.status" /></th>
                        <th><fmt:message key="table.res.note" /></th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if (resSubList == null || resSubList.isEmpty()) { %>
                            <tr><td colspan="7" style="padding: 105px 0; color: #6c757d; border-bottom: none; text-align: center;"><fmt:message key="table.res.empty" /></td></tr>
                        <% } else {
                               for (Map<String, Object> resMap : resSubList) {
                                   String statusKey = (String) resMap.get("statusKey");
                                   String statusClass = "bg-primary"; 
                                   
                                   if ("status.res.finished".equals(statusKey)) {
                                       statusClass = "bg-secondary"; 
                                   } else if ("status.res.canceled".equals(statusKey)) {
                                       statusClass = "bg-danger"; 
                                   }
                        %>
                        <tr>
                            <td style="color: #6c757d;"><%=resMap.get("resNo")%></td>
                            <td style="font-weight: 600; color: #343a40;"><%=resMap.get("roomId")%><fmt:message	key="status.room" /></td>
                            <td><%=resMap.get("resDate")%></td>
                            <td><%=resMap.get("startTime")%> ~ <%=resMap.get("endTime")%></td>
                            <td>
                                <span class="title-link" title="<%=resMap.get("purpose")%>"><%=resMap.get("purpose")%></span>
                            </td>
                            <td><span class="status-badge <%=statusClass%>"><fmt:message key="<%=statusKey%>" /></span></td>
                            <td>
                                <% if ("status.res.complete".equals(statusKey)) { %>
                                    <button class="btn-action" onclick="cancelReserve(<%=resMap.get("resNo")%>)"><fmt:message key="table.res.btn.cancel" /></button> 
                                <% } else { %> 
                                    <span style="color: #ced4da;">-</span> 
                                <% } %>
                            </td>
                        </tr>
                    <%     }
                       } %>
                    </tbody>
                </table>
            </div>
            
            <div class="pagination-container">
                <% if (resStartPage > 1) { %>
                    <button type="button" class="pagination-btn" onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resStartPage-1%>&rentalPage=<%=rentalPage%>&leavePage=<%=leavePage%>')">
                    	<fmt:message key="dashboard.btn.prev" />
                    </button>
                <% } %>
                <% for (int i = resStartPage; i <= resEndPage; i++) { %>
                    <button type="button" class="pagination-btn <%= (i == resPage) ? "active" : "" %>" onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=i%>&rentalPage=<%=rentalPage%>&leavePage=<%=leavePage%>')"><%=i%></button>
                <% } %>
                <% if (resEndPage < resTotalPages) { %>
                    <button type="button" class="pagination-btn" onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resEndPage+1%>&rentalPage=<%=rentalPage%>&leavePage=<%=leavePage%>')"><fmt:message key="dashboard.btn.next" /></button>
                <% } %>
            </div>
        </div> 
</body>
</html>