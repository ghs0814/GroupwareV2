<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="com.groupware.dto.EmployeeDTO"%>
<%
EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
if (loginEmp == null) {
	response.sendRedirect("index.jsp");
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<title>社内システム - オフィス図面予約</title>
<link rel="stylesheet"
	href="<%=request.getContextPath()%>/css/officeMap.css?v=3.0">
<script>
	function switchFloor(floor) {
		for (let i = 4; i <= 5; i++) {
			document.getElementById('map' + i).style.display = 'none';
			document.getElementById('tab' + i).classList.remove('active');
		}
		document.getElementById('map' + floor).style.display = 'block';
		document.getElementById('tab' + floor).classList.add('active');
	}
</script>
</head>
<body>

	<jsp:include page="header.jsp" />

	<div class="dashboard-container">
		<div class="map-wrapper">
			<div class="reservation-header">
				<h3>オフィス予約</h3>
				<p>ご予約される階数と会議室を図面からお選びください。</p>
			</div>

			<div class="floor-tabs">
				<%-- 기존 1~3층 탭 주석 처리 상태 유지 --%>
				<%-- <button class="floor-tab active" id="tab1" onclick="switchFloor(1)">1층</button>
                <button class="floor-tab" id="tab2" onclick="switchFloor(2)">2층</button>
                <button class="floor-tab" id="tab3" onclick="switchFloor(3)">3층</button> --%>
				<button class="floor-tab active" id="tab4" onclick="switchFloor(4)">4層</button>
				<button class="floor-tab" id="tab5" onclick="switchFloor(5)">5層</button>
			</div>

			<div id="map4" class="map-container">
				<div class="room-btn" id="room404" data-name="404号室"
					onclick="location.href='reserve.do?roomId=404'"></div>
				<div class="room-btn" id="room403" data-name="403号室"
					onclick="location.href='reserve.do?roomId=403'"></div>
				<div class="room-btn" id="room402" data-name="402号室"
					onclick="location.href='reserve.do?roomId=402'"></div>
				<div class="room-btn" id="room401" data-name="401号室"
					onclick="location.href='reserve.do?roomId=401'"></div>
				<div class="room-btn" id="roomInterview" data-name="面接室"
					onclick="location.href='reserve.do?roomId=Interview'"></div>
				<div class="room-btn" id="roomConsult" data-name="相談室"
					onclick="location.href='reserve.do?roomId=Consult'"></div>
				<div class="room-btn" id="roomMeeting" data-name="会議室"
					onclick="location.href='reserve.do?roomId=Meeting'"></div>
			</div>

			<div id="map5" class="map-container">
				<div class="room-btn" id="room503" data-name="503号室"
					onclick="location.href='reserve.do?roomId=503'"></div>
				<div class="room-btn" id="room502" data-name="502号室"
					onclick="location.href='reserve.do?roomId=502'"></div>
				<div class="room-btn" id="room501" data-name="501号室"
					onclick="location.href='reserve.do?roomId=501'"></div>
				<div class="room-btn" id="roomMeeting5" data-name="会議室"
					onclick="location.href='reserve.do?roomId=Meeting5'"></div>
			</div>
		</div>
	</div>

</body>
</html>
