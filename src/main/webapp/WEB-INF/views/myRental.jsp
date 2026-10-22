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
List<RentalHistoryDTO> rawRentalList = (List<RentalHistoryDTO>) request.getAttribute("rawRentalList");
List<RentalHistoryDTO> filteredRentalList = (List<RentalHistoryDTO>) request.getAttribute("filteredRentalList");
int rentalTotalCount = (Integer) request.getAttribute("rentalTotalCount");
int rentalTotalPages = (Integer) request.getAttribute("rentalTotalPages");
int rentalStartPage = (Integer) request.getAttribute("rentalStartPage");
int rentalEndPage = (Integer) request.getAttribute("rentalEndPage");
int rentalStartIdx = (Integer) request.getAttribute("rentalStartIdx");
int rentalEndIdx = (Integer) request.getAttribute("rentalEndIdx");
List<RentalHistoryDTO> rentalList = (List<RentalHistoryDTO>) request.getAttribute("rentalList");
%>
<!DOCTYPE html>
<html>
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<title>Insert title here</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">
<script>
//비품 반납 핸들러
function returnProcess(rentalNo) { 
    if (confirm("<fmt:message key="alert.return.confirm" />")) {
        saveScroll(); 
        location.href = 'returnProcess.do?rentalNo=' + rentalNo + '&from=main' + '&csrfToken=' + encodeURIComponent(window.groupwareCsrfToken); 
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
                <a href="myRental.jsp" class="data-link">
                    <fmt:message key="<%= isManagerMode ? \"table.rental.title.admin\" : \"table.rental.title.user\" %>" />
                </a>
            </div>
            <div class="table-wrapper">
                <table class="table-rental <%= isManagerMode ? "admin-view" : "" %>">
                    <thead>
                        <tr>
                            <th style="width: 10%;"><fmt:message key="table.rental.no" /></th>
                            <% if (isManagerMode) { %> <th style="width: 15%;"><fmt:message key="table.rental.writer" /></th> <% } %>
                            <th><fmt:message key="table.rental.title" /></th>
                            <th style="width: 25%;"><fmt:message key="table.rental.date" /></th>
                            <th style="width: 12%;"><fmt:message key="table.rental.status" /></th>
                            <th style="width: 12%;"><fmt:message key="table.rental.note" /></th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (rentalList == null || rentalList.isEmpty()) { %>
                        <tr><td colspan="<%= isManagerMode ? 6 : 5 %>" style="padding: 105px 0; color: #6c757d; border-bottom: none; text-align: center;"><fmt:message key="table.rental.empty" /></td></tr>
                    <% } else {
                               for (RentalHistoryDTO item : rentalList) {
                                   String displayStatus = item.getStatus();
                                   boolean isRetiredCreator = (item.getEmpLevel() == 0);
                                   
                                   String badgeClass = "bg-secondary";
                                   if ("승인대기".equals(displayStatus)) badgeClass = "bg-warning";
                                   else if ("대여중".equals(displayStatus)) badgeClass = "bg-success";
                                   else if ("반려됨".equals(displayStatus) || "미반납".equals(displayStatus)) badgeClass = "bg-danger";
                    %>
                        <tr>
                            <td><%=item.getRentalNo()%></td>
                            <% if (isManagerMode) { %>
                                <td style="font-weight: 600; color: #64748b;">
                                    <%=item.getEmpName()%> 
                                    <fmt:message key="<%= isRetiredCreator ? \"table.rental.retired\" : \"table.rental.myself\" %>" var="empType"/>
                                    <br>(${empType})
                                </td>
                            <% } %>
                            <td>
                                <a href="rentalDetail.do?rentalNo=<%=item.getRentalNo()%>" style="text-decoration: none; display: block;">
                                    <span class="title-link" style="color: #6366f1; font-weight: 600;" title="<%=item.getTitle()%>">
                                        <%=item.getTitle() != null ? item.getTitle() : "제목 없음"%>
                                    </span>
                                </a>
                            </td>
                            <td><%=item.getRentalDate()%> ~ <%=item.getReturnDate()%></td>
                            <td><span class="status-badge <%=badgeClass%>"><fmt:message key="<%= StatusUtil.getStatusKey(displayStatus) %>" /></span></td>
                            <td>
                                <% if ("대여중".equals(displayStatus) || "미반납".equals(displayStatus)) { %>
                                    <button class="btn-action" onclick="returnProcess('<%=item.getRentalNo()%>')"><fmt:message key="table.rental.btn.return" /></button>
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
                <% if (rentalStartPage > 1) { %>
                    <button type="button" class="pagination-btn" onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=rentalStartPage-1%>&leavePage=<%=leavePage%>')"><fmt:message key="dashboard.btn.prev" /></button>
                <% } %>
                <% for (int i = rentalStartPage; i <= rentalEndPage; i++) { %>
                    <button type="button" class="pagination-btn <%= (i == rentalPage) ? "active" : "" %>" onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=i%>&leavePage=<%=leavePage%>')"><%=i%></button>
                <% } %>
                <% if (rentalEndPage < rentalTotalPages) { %>
                    <button type="button" class="pagination-btn" onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=rentalEndPage+1%>&leavePage=<%=leavePage%>')"><fmt:message key="dashboard.btn.next" /></button>
                <% } %>
            </div>
	</div>
</body>
</html>