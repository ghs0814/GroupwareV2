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
List<LeaveHistoryDTO> fullLeaveList = (List<LeaveHistoryDTO>) request.getAttribute("fullLeaveList");
int leaveTotalCount = (Integer) request.getAttribute("leaveTotalCount");
int leaveTotalPages = (Integer) request.getAttribute("leaveTotalPages");
int leaveStartPage = (Integer) request.getAttribute("leaveStartPage");
int leaveEndPage = (Integer) request.getAttribute("leaveEndPage");
int leaveStartIdx = (Integer) request.getAttribute("leaveStartIdx");
int leaveEndIdx = (Integer) request.getAttribute("leaveEndIdx");
List<LeaveHistoryDTO> leaveList = (List<LeaveHistoryDTO>) request.getAttribute("leaveList");
%>
<!DOCTYPE html>
<html lang="ko">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<title>Insert title here</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">
    <script>
        function returnProcess(rentalNo) { 
            if (confirm("해당 비품을 반납 처리하시겠습니까?")) {
                saveScroll(); 
                location.href = 'returnProcess.do?rentalNo=' + rentalNo + '&from=main' + '&csrfToken=' + encodeURIComponent(window.groupwareCsrfToken); 
            }
        }
        
        function cancelReserve(resNo) { 
            if (confirm("정말 이 예약을 취소하시겠습니까?")) {
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
				<a href="myLeave.jsp" class="data-link">
					<fmt:message key="table.leave.title" />
				</a>
			</div>
			<div class="table-wrapper">
				<table class="table-leave">
					<thead>
						<tr>
							<th style="width: 10%;"><fmt:message key="table.leave.no" /></th>
							<th style="width: 30%;"><fmt:message key="table.leave.date" /></th>
							<th style="width: 15%;"><fmt:message key="table.leave.days" /></th>
							<th><fmt:message key="table.leave.reason" /></th>
							<th style="width: 15%;">
								<fmt:message key="table.leave.status" />
							</th>
						</tr>
					</thead>
					<tbody>
						<% if (leaveList == null || leaveList.isEmpty()) { %>
						<tr>
							<td colspan="5"
								style="padding: 105px 0; color: #6c757d; border-bottom: none; text-align: center;"><fmt:message
									key="table.leave.empty" /></td>
						</tr>
						<% } else {
						for (LeaveHistoryDTO leave : leaveList) {
							String badgeClass = "bg-warning";
							if ("승인완료".equals(leave.getStatus()))
								badgeClass = "bg-success";
							else if ("반려됨".equals(leave.getStatus()))
								badgeClass = "bg-danger";

							// 사유 변환 로직 추가
							String rawReason = leave.getReason();
							String displayReason = "사유 없음";

							if (rawReason != null) {
								if (rawReason.equals("연차")) {
							displayReason = "年次有給休暇";
								} else if (rawReason.equals("병가")) {
							displayReason = "傷病休暇";
								} else if (rawReason.equals("경조사")) {
							displayReason = "慶弔休暇";
								} else {
							// 더미 데이터(기존 일어) 및 직접 입력 데이터 보존
							displayReason = rawReason;
								}
							}
						%>
						<tr>
							<td style="color: #6c757d;"><%=leave.getLeaveNo()%></td>
							<td><%=leave.getStartDate()%> ~ <%=leave.getEndDate()%></td>
							<td><b><%=leave.getUseDays()%><fmt:message
										key="table.leave.days.unit" /></b></td>
							<td><a href="leaveDetail.do?leaveNo=<%=leave.getLeaveNo()%>"
								style="text-decoration: none; display: block;"> <span
									class="title-link" style="color: #6366f1; font-weight: 600;"
									title="<%=displayReason%>"> <%=displayReason%>
								</span>
							</a></td>
							<td><span class="status-badge <%=badgeClass%>"><fmt:message
										key="<%=StatusUtil.getStatusKey(leave.getStatus())%>" /></span></td>
						</tr>
						<% } } %>
					</tbody>
				</table>
			</div>

			<div class="pagination-container">
				<% if (leaveStartPage > 1) { %>
				<button type="button" class="pagination-btn"
					onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=rentalPage%>&leavePage=<%=leaveStartPage - 1%>')">
					<fmt:message key="dashboard.btn.prev" />
				</button>
				<% } %>
				<% for (int i = leaveStartPage; i <= leaveEndPage; i++) { %>
				<button type="button"
					class="pagination-btn <%=(i == leavePage) ? "active" : ""%>"
					onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=rentalPage%>&leavePage=<%=i%>')"><%=i%></button>
				<% } %>
				<% if (leaveEndPage < leaveTotalPages) { %>
				<button type="button" class="pagination-btn"
					onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=rentalPage%>&leavePage=<%=leaveEndPage + 1%>')">
					<fmt:message key="dashboard.btn.next" />
				</button>
				<% } %>
			</div>
	</div>
</body>
</html>