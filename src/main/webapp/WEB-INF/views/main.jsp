<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page
	import="java.util.*, java.time.*, com.groupware.dto.*, com.groupware.dao.*, com.groupware.util.StatusUtil"%>
<%
// [1순위 고정] 자바 로직이 돌기 전, 요청과 응답 스트림의 구멍을 UTF-8로 선제 타격해서 열어둡니다.
request.setCharacterEncoding("UTF-8");
response.setCharacterEncoding("UTF-8");
%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<%-- [2순위 고정] JSTL이 브라우저 언어헤더를 읽어 동적으로 프로퍼티를 선택할 때, 깨지지 않도록 인코딩 필터 주입 --%>
<fmt:requestEncoding value="UTF-8" />
<fmt:setBundle basename="resources.message" scope="session" />
<%
EmployeeDTO loginEmp = (EmployeeDTO) request.getAttribute("loginEmp");
EmployeeDTO updatedEmp = (EmployeeDTO) request.getAttribute("updatedEmp");
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
List<RentalHistoryDTO> rawRentalList = (List<RentalHistoryDTO>) request.getAttribute("rawRentalList");
List<RentalHistoryDTO> filteredRentalList = (List<RentalHistoryDTO>) request.getAttribute("filteredRentalList");
int rentalTotalCount = (Integer) request.getAttribute("rentalTotalCount");
int rentalTotalPages = (Integer) request.getAttribute("rentalTotalPages");
int rentalStartPage = (Integer) request.getAttribute("rentalStartPage");
int rentalEndPage = (Integer) request.getAttribute("rentalEndPage");
int rentalStartIdx = (Integer) request.getAttribute("rentalStartIdx");
int rentalEndIdx = (Integer) request.getAttribute("rentalEndIdx");
List<RentalHistoryDTO> rentalList = (List<RentalHistoryDTO>) request.getAttribute("rentalList");
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
<title><fmt:message key="dashboard.title" /></title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">
<script
	src='https://cdn.jsdelivr.net/npm/fullcalendar@6.1.10/index.global.min.js'></script>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        var calendarEl = document.getElementById('calendar');
        var calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            headerToolbar: { left: 'prev,next', center: 'title', right: '' },
            height: 'auto',
            dayMaxEvents: 2, 
            events: [
                <%if (fullReserveList != null) {
	boolean isFirst = true;
	for (Map<String, Object> map : fullReserveList) {
		if (!"status.res.canceled".equals(map.get("statusKey"))) {
			if (!isFirst) {
				out.print(",");
			}
			isFirst = false;%>
                {
                    title: '[<%=map.get("roomId")%>号室]\n<%=map.get("startTime")%> ~ <%=map.get("endTime")%>',
                    start: '<%=map.get("resDate")%>',
                    extendedProps: {
                        purpose: '<%=((String) map.get("purpose")).replace("'", "\\'")%>'
                    },
                    backgroundColor: '#4e73df', 
                    borderColor: '#4e73df'
                }
                <%}
}
}%>
            ],
            eventClick: function(info) {
                alert("<fmt:message key="alert.cal.title" />" + info.event.title.replace('\n', ' ') + "<fmt:message key="alert.cal.purpose" />" +
                      info.event.extendedProps.purpose);
            }
        });
        calendar.render();
    });
    </script>
<script>
        // 비품 반납 핸들러
        function returnProcess(rentalNo) { 
            if (confirm("<fmt:message key="alert.return.confirm" />")) {
                saveScroll(); 
                location.href = 'returnProcess.do?rentalNo=' + rentalNo + '&from=main' + '&csrfToken=' + encodeURIComponent(window.groupwareCsrfToken); 
            }
        }
        
        // 회의실 예약 취소 핸들러
        function cancelReserve(resNo) { 
            if (confirm("<fmt:message key="alert.cancel.confirm" />")) {
                saveScroll(); 
                location.href = "cancelReserve.do?resNo=" + resNo + '&from=main' + '&csrfToken=' + encodeURIComponent(window.groupwareCsrfToken); 
            }
        }

        // 삼분할 페이징 전용 스크롤 유지 라우터
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
                window.scrollTo({
                    top: parseInt(savedScrollY),
                    behavior: 'instant' 
                });
                localStorage.removeItem("main_scroll_y");
            }
        });
    </script>
</head>
<body>

	<jsp:include page="header.jsp" />

	<div class="dashboard-container">

		<div class="info-card"
			style="width: 1100px; margin: 20px auto; background: #fff; padding: 25px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03); display: flex; justify-content: space-between; align-items: center; box-sizing: border-box;">
			<div class="welcome-box">
				<h3 style="margin: 0 0 5px 0; font-size: 20px; color: #34495e;">
					<fmt:message key="dashboard.welcome">
						<fmt:param value="<%=loginEmp.getEmpName()%>" />
					</fmt:message>
				</h3>
				<p style="margin: 0; color: #7f8c8d; font-size: 14px;">
					<fmt:message key="dashboard.dept">
						<fmt:param value="<%=loginEmp.getDept()%>" />
					</fmt:message>
				</p>
			</div>
			<div class="leave-box" style="text-align: right;">
				<p class="leave-label"
					style="margin: 0 0 5px 0; font-size: 13px; color: #7f8c8d;">
					<fmt:message key="dashboard.leave.label" />
				</p>
				<div class="leave-count"
					style="font-size: 22px; font-weight: bold; color: #2c3e50;">
					<span class="current" style="color: #4f46e5;"><%=loginEmp.getCurLeave()%></span>
					<span class="divider" style="color: #d1d5db; margin: 0 4px;">/</span>
					<span class="total"><%=loginEmp.getMaxLeave()%></span>
				</div>
			</div>
		</div>

		<div class="container">

			<div class="dashboard-grid">
				<div class="calendar-section">
					<div id="calendar"></div>
				</div>
			</div>
			<div class="section-title">
				<a href="myReservation.jsp" class="data-link"><fmt:message
						key="table.res.title" /></a>
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
						<tr>
							<td colspan="7"
								style="padding: 105px 0; color: #6c757d; border-bottom: none; text-align: center;"><fmt:message
									key="table.res.empty" /></td>
						</tr>
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
							<td><span class="title-link"
								title="<%=resMap.get("purpose")%>"><%=resMap.get("purpose")%></span>
							</td>
							<td><span class="status-badge <%=statusClass%>"><fmt:message
										key="<%=statusKey%>" /></span></td>
							<td>
								<% if ("status.res.complete".equals(statusKey)) { %>
								<button class="btn-action"
									onclick="cancelReserve(<%=resMap.get("resNo")%>)">
									<fmt:message key="table.res.btn.cancel" />
								</button>
								<% } else { %> 
							<span style="color: #ced4da;">-</span> 
						<% } %>
							</td>
						</tr>
						<% } } %>
					</tbody>
				</table>
			</div>

			<div class="pagination-container">
				<%if (resStartPage > 1) {%>
				<button type="button" class="pagination-btn" onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resStartPage - 1%>&rentalPage=<%=rentalPage%>&leavePage=<%=leavePage%>')">
					<fmt:message key="dashboard.btn.prev" />
				</button>
				<% }	%>
				<%for (int i = resStartPage; i <= resEndPage; i++) {%>
				<button type="button"
					class="pagination-btn <%=(i == resPage) ? "active" : ""%>"
					onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=i%>&rentalPage=<%=rentalPage%>&leavePage=<%=leavePage%>')"><%=i%></button>
				<% }	%>
				<%if (resEndPage < resTotalPages) {	%>
				<button type="button" class="pagination-btn"
					onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resEndPage + 1%>&rentalPage=<%=rentalPage%>&leavePage=<%=leavePage%>')">
					<fmt:message key="dashboard.btn.next" />
				</button>
				<% } %>
			</div>


			<div class="section-title">
				<a href="myRental.jsp" class="data-link"> <fmt:message
						key="<%=isManagerMode ?\"table.rental.title.admin\" :\"table.rental.title.user\"%>" />
				</a>
			</div>
			<div class="table-wrapper">
				<table class="table-rental <%=isManagerMode ? "admin-view" : ""%>">
					<thead>
						<tr>
							<th style="width: 10%;"><fmt:message key="table.rental.no" /></th>
							<% if (isManagerMode) { %>
							<th style="width: 15%;"><fmt:message
									key="table.rental.writer" /></th>
							<% } %>
							<th><fmt:message key="table.rental.title" /></th>
							<th style="width: 25%;"><fmt:message key="table.rental.date" /></th>
							<th style="width: 12%;"><fmt:message
									key="table.rental.status" /></th>
							<th style="width: 12%;"><fmt:message key="table.rental.note" /></th>
						</tr>
					</thead>
					<tbody>
						<% if (rentalList == null || rentalList.isEmpty()) { %>
						<tr>
							<td colspan="<%=isManagerMode ? 6 : 5%>"
								style="padding: 105px 0; color: #6c757d; border-bottom: none; text-align: center;"><fmt:message
									key="table.rental.empty" /></td>
						</tr>
						<% } else {
						for (RentalHistoryDTO item : rentalList) {
							String displayStatus = item.getStatus();
							boolean isRetiredCreator = (item.getEmpLevel() == 0);

							String badgeClass = "bg-secondary";
							if ("승인대기".equals(displayStatus))
								badgeClass = "bg-warning";
							else if ("대여중".equals(displayStatus))
								badgeClass = "bg-success";
							else if ("반려됨".equals(displayStatus) || "미반납".equals(displayStatus))
								badgeClass = "bg-danger";
						%>
						<tr>
							<td><%=item.getRentalNo()%></td>
							<% if (isManagerMode) { %>
							<td style="font-weight: 600; color: #64748b;"><%=item.getEmpName()%>
								<fmt:message
									key="<%=isRetiredCreator ?\"table.rental.retired\" :\"table.rental.myself\"%>"
									var="empType" /> <br>(${empType})</td>
							<% } %>
							<td><a
								href="rentalDetail.do?rentalNo=<%=item.getRentalNo()%>"
								style="text-decoration: none; display: block;"> <span
									class="title-link" style="color: #6366f1; font-weight: 600;"
									title="<%=item.getTitle()%>"> <%=item.getTitle() != null ? item.getTitle() : "제목 없음"%>
								</span>
							</a></td>
							<td><%=item.getRentalDate()%> ~ <%=item.getReturnDate()%></td>
							<td><span class="status-badge <%=badgeClass%>"><fmt:message
										key="<%=StatusUtil.getStatusKey(displayStatus)%>" /></span></td>
							<td>
								<% if ("대여중".equals(displayStatus) || "미반납".equals(displayStatus)) { %>
								<button class="btn-action"
									onclick="returnProcess('<%=item.getRentalNo()%>')">
									<fmt:message key="table.rental.btn.return" />
								</button> 
								<% } else {%>
								<span style="color: #ced4da;">-</span> 
							<% } %>
							</td>
						</tr>
						<% } } %>
					</tbody>
				</table>
			</div>

			<div class="pagination-container">
				<% if (rentalStartPage > 1) { %>
				<button type="button" class="pagination-btn"
					onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=rentalStartPage - 1%>&leavePage=<%=leavePage%>')">
					<fmt:message key="dashboard.btn.prev" />
				</button>
				<% } %>
				<% for (int i = rentalStartPage; i <= rentalEndPage; i++) { %>
				<button type="button"
					class="pagination-btn <%=(i == rentalPage) ? "active" : ""%>"
					onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=i%>&leavePage=<%=leavePage%>')"><%=i%></button>
				<% } %>
				<% if (rentalEndPage < rentalTotalPages) { %>
				<button type="button" class="pagination-btn"
					onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=rentalEndPage + 1%>&leavePage=<%=leavePage%>')">
					<fmt:message key="dashboard.btn.next" />
				</button>
				<% } %>
			</div>



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
	</div>
</body>
</html>