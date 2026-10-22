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
<html>
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<title><fmt:message key="nav.mypage" /></title>
<link rel="stylesheet"
	href="<%=request.getContextPath()%>/css/myPage.css">
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
                	<%if (filteredRentalList != null) {
	for (RentalHistoryDTO rDto : filteredRentalList) {
		String itemTitle = rDto.getTitle() != null ? rDto.getTitle() : "제목 없음";

		// FullCalendar 종료일 배타적(Exclusive) 적용에 대응하기 위한 반납일 +1일 연산 추가
		String rentalEndDateStr = "";
		if (rDto.getReturnDate() != null) {
			rentalEndDateStr = java.time.LocalDate.parse(rDto.getReturnDate().toString()).plusDays(1).toString();
		}%>
                	                    {
                	 
                	                        title: '[<fmt:message key="calendar.prefix.rental" />] <%=itemTitle%>',
                	                        start: '<%=rDto.getRentalDate()%>',
                	                        end: '<%=rentalEndDateStr%>',
                	                        backgroundColor: '#1cc88a', 
                	                      
                	                        borderColor: '#1cc88a',
                	                        extendedProps: { 
                	                                url: 'rentalDetail.do?rentalNo=<%=rDto.getRentalNo()%>' 
                	                            }
                	         
                	            },
                	                    <%}
}%>

<%if (leaveList != null) {
						for (LeaveHistoryDTO lDto : leaveList) {
							String rawReasonCal = lDto.getReason();
							String displayReasonCal = "사유 없음";

							if (rawReasonCal != null) {
								if (rawReasonCal.equals("연차"))
									displayReasonCal = "年次有給休暇";
								else if (rawReasonCal.equals("병가"))
									displayReasonCal = "傷病休暇";
								else if (rawReasonCal.equals("경조사"))
									displayReasonCal = "慶弔休暇";
								else
									displayReasonCal = rawReasonCal;
							}

							// FullCalendar 종료일 배타적(Exclusive) 적용에 대응하기 위한 +1일 연산 추가
							String calEndDateStr = "";
							if (lDto.getEndDate() != null) {
								calEndDateStr = java.time.LocalDate.parse(lDto.getEndDate().toString()).plusDays(1)
										.toString();
							}%>
{
    title: '[<fmt:message key="nav.leave.apply" />] <%=displayReasonCal%>',
    start: '<%=lDto.getStartDate()%>',
    end: '<%=calEndDateStr%>', 
    backgroundColor: '#e74a3b', 
    borderColor: '#e74a3b',
    extendedProps: { 
        url: 'leaveDetail.do?leaveNo=<%=lDto.getLeaveNo()%>' 
    }
},
<%}
}%>
                ],
                eventClick: function(info) {
                    if (info.event.extendedProps && info.event.extendedProps.url) {
                        location.href = info.event.extendedProps.url;
                    }
                }
            });
            calendar.render();
        });
    </script>
<script>
    function returnProcess(rentalNo) { 
        if (confirm("<fmt:message key='alert.return.confirm' />")) 
            location.href = 'returnProcess.do?rentalNo=' + rentalNo + '&from=main' + '&csrfToken=' + encodeURIComponent(window.groupwareCsrfToken); }
    
    function cancelReserve(resNo) { 
        if (confirm("<fmt:message key='alert.cancel.confirm' />")) 
            location.href = "cancelReserve.do?resNo=" + resNo + '&from=main' + '&csrfToken=' + encodeURIComponent(window.groupwareCsrfToken); }
    
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

		<div class="page-header">
			<h2>
				<fmt:message key="nav.mypage" />
			</h2>
			<div class="btn-group">
				<a href="changePw.jsp" class="btn-pw"><fmt:message
						key="mypage.btn.changePw" /></a>
			</div>
		</div>

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
					<%
					if (resSubList == null || resSubList.isEmpty()) {
					%>
					<tr>
						<td colspan="7"
							style="padding: 105px 0; color: #6c757d; border-bottom: none; text-align: center;"><fmt:message
								key="table.res.empty" /></td>
					</tr>
					<%
					} else {
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
						<td style="font-weight: 600; color: #343a40;"><%=resMap.get("roomId")%><fmt:message
								key="status.room" /></td>
						<td><%=resMap.get("resDate")%></td>
						<td><%=resMap.get("startTime")%> ~ <%=resMap.get("endTime")%></td>
						<td><span class="title-link"
							title="<%=resMap.get("purpose")%>"><%=resMap.get("purpose")%></span></td>
						<td><span class="status-badge <%=statusClass%>"><fmt:message
									key="<%=statusKey%>" /></span></td>
						<td>
							<%
							if ("status.res.complete".equals(statusKey)) {
							%>
							<button class="btn-action"
								onclick="cancelReserve(<%=resMap.get("resNo")%>)">
								<fmt:message key="table.res.btn.cancel" />
							</button> <%
 } else {
 %> <span style="color: #ced4da;">-</span> <%
 }
 %>
						</td>
					</tr>
					<%
					}
					}
					%>
				</tbody>
			</table>
		</div>

		<div class="pagination-container">
			<%
			if (resStartPage > 1) {
			%>
			<button type="button" class="pagination-btn"
				onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resStartPage - 1%>&rentalPage=<%=rentalPage%>&leavePage=<%=leavePage%>')">
				<fmt:message key="dashboard.btn.prev" />
			</button>
			<%
			}
			%>
			<%
			for (int i = resStartPage; i <= resEndPage; i++) {
			%>
			<button type="button"
				class="pagination-btn <%=(i == resPage) ? "active" : ""%>"
				onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=i%>&rentalPage=<%=rentalPage%>&leavePage=<%=leavePage%>')"><%=i%></button>
			<%
			}
			%>
			<%
			if (resEndPage < resTotalPages) {
			%>
			<button type="button" class="pagination-btn"
				onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resEndPage + 1%>&rentalPage=<%=rentalPage%>&leavePage=<%=leavePage%>')">
				<fmt:message key="dashboard.btn.next" />
			</button>
			<%
			}
			%>
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
						<%
						if (isManagerMode) {
						%>
						<th style="width: 15%;"><fmt:message
								key="table.rental.writer" /></th>
						<%
						}
						%>
						<th><fmt:message key="table.rental.title" /></th>
						<th style="width: 25%;"><fmt:message key="table.rental.date" /></th>
						<th style="width: 12%;"><fmt:message
								key="table.rental.status" /></th>
						<th style="width: 12%;"><fmt:message key="table.rental.note" /></th>
					</tr>
				</thead>
				<tbody>
					<%
					if (rentalList == null || rentalList.isEmpty()) {
					%>
					<tr>
						<td colspan="<%=isManagerMode ? 6 : 5%>"
							style="padding: 105px 0; color: #6c757d; border-bottom: none; text-align: center;"><fmt:message
								key="table.rental.empty" /></td>
					</tr>
					<%
					} else {
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
						<%
						if (isManagerMode) {
						%>
						<td style="font-weight: 600; color: #64748b;"><%=item.getEmpName()%>
							<fmt:message
								key="<%=isRetiredCreator ?\"table.rental.retired\" :\"table.rental.myself\"%>"
								var="empType" /> <br>(${empType})</td>
						<%
						}
						%>
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
							<%
							if ("대여중".equals(displayStatus) || "미반납".equals(displayStatus)) {
							%>
							<button class="btn-action"
								onclick="returnProcess('<%=item.getRentalNo()%>')">
								<fmt:message key="table.rental.btn.return" />
							</button> <%
 } else {
 %> <span style="color: #ced4da;">-</span> <%
 }
 %>
						</td>
					</tr>
					<%
					}
					}
					%>
				</tbody>
			</table>
		</div>

		<div class="pagination-container">
			<%
			if (rentalStartPage > 1) {
			%>
			<button type="button" class="pagination-btn"
				onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=rentalStartPage - 1%>&leavePage=<%=leavePage%>')">
				<fmt:message key="dashboard.btn.prev" />
			</button>
			<%
			}
			%>
			<%
			for (int i = rentalStartPage; i <= rentalEndPage; i++) {
			%>
			<button type="button"
				class="pagination-btn <%=(i == rentalPage) ? "active" : ""%>"
				onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=i%>&leavePage=<%=leavePage%>')"><%=i%></button>
			<%
			}
			%>
			<%
			if (rentalEndPage < rentalTotalPages) {
			%>
			<button type="button" class="pagination-btn"
				onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=rentalEndPage + 1%>&leavePage=<%=leavePage%>')">
				<fmt:message key="dashboard.btn.next" />
			</button>
			<%
			}
			%>
		</div>

		<div class="section-title">
			<a href="myLeave.jsp" class="data-link"><fmt:message
					key="table.leave.title" /></a>
		</div>
		<div class="table-wrapper">
			<table class="table-leave">
				<thead>
					<tr>
						<th><fmt:message key="table.leave.no" /></th>
						<th><fmt:message key="table.leave.date" /></th>
						<th><fmt:message key="table.leave.days" /></th>
						<th><fmt:message key="table.leave.reason" /></th>
						<th><fmt:message key="table.leave.status" /></th>
					</tr>
				</thead>
				<tbody>
					<%
					if (leaveList == null || leaveList.isEmpty()) {
					%>
					<tr>
						<td colspan="5"
							style="padding: 105px 0; color: #6c757d; border-bottom: none; text-align: center;"><fmt:message
								key="table.leave.empty" /></td>
					</tr>
					<%
					} else {
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
					<%
					}
					}
					%>
				</tbody>
			</table>
		</div>

		<div class="pagination-container">
			<%
			if (leaveStartPage > 1) {
			%>
			<%-- ★ [수정] leavePage 파라미터 유도 시 자바 표현식 기호(<%= %>)가 빠졌던 치명적인 오타 복구 --%>
			<button type="button" class="pagination-btn"
				onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=rentalPage%>&leavePage=<%=leaveStartPage - 1%>')">
				<fmt:message key="dashboard.btn.prev" />
			</button>
			<%
			}
			%>
			<%
			for (int i = leaveStartPage; i <= leaveEndPage; i++) {
			%>
			<button type="button"
				class="pagination-btn <%=(i == leavePage) ? "active" : ""%>"
				onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=rentalPage%>&leavePage=<%=i%>')"><%=i%></button>
			<%
			}
			%>
			<%
			if (leaveEndPage < leaveTotalPages) {
			%>
			<button type="button" class="pagination-btn"
				onclick="navigateWithScroll('<%=currentMapping%>?resPage=<%=resPage%>&rentalPage=<%=rentalPage%>&leavePage=<%=leaveEndPage + 1%>')">
				<fmt:message key="dashboard.btn.next" />
			</button>
			<%
			}
			%>
		</div>

	</div>
</body>
</html>