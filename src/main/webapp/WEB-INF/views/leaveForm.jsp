<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.groupware.dto.EmployeeDTO"%>

<%
    // 세션에서 로그인한 사원 정보를 가져옴
    EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
    // 로그인 정보가 없으면 로그인 페이지로 리다이렉트
    if (loginEmp == null) { response.sendRedirect("index.jsp"); return; }
    
    // 잔여 연차를 계산하여 색상 클래스 지정 (0일 이하면 빨간색, 아니면 파란색)
    int curLeave = loginEmp.getCurLeave();
    String leaveColor = (curLeave <= 0) ? "red-text" : "blue-text";
%>
<!DOCTYPE html>
<html>
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
    <meta charset="UTF-8">
    <title>休暇申請書</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/leaveForm.css">
</head>
<body>
<jsp:include page="header.jsp" />
    <div class="container">
        <div class="title-area">
            <h3>🏖️ 休暇申請書</h3>
        </div>

        <div class="info-card">
            <p>申請者:<b> <%=loginEmp.getEmpName()%></b> (<%=loginEmp.getDept()%> / <%=loginEmp.getEmpLevel()%>段階)</p>
            <p>有給残日数: <span class="<%=leaveColor%>"><b><%=curLeave%></b></span> / <%=loginEmp.getMaxLeave()%>日</p>
        </div>

       <form action="leaveForm.do" method="post" onsubmit="return validate();">
            <div class="form-group">
                <label>開始日</label>
                <input type="date" id="startDate" name="startDate" required onchange="updateEndDateMin()">
            </div>
            
            <div class="form-group spacing">
                <label>終了日</label>
                <input type="date" id="endDate" name="endDate" required>
            </div>
            
            <div class="form-group">
   				<label>事由選択</label>
    					<select id="reasonCategory" name="reasonCategory" onchange="toggleReason(this.value)">
        					<option value="연차">年次有給休暇</option>
        					<option value="병가">傷病休暇</option>
        					<option value="경조사">慶弔休暇</option>
        
    					</select>
    
    			<!-- <input type="text" id="customReason" name="reason" style="margin-top:10px; width:100%; box-sizing:border-box;" placeholder="事由をご記入ください"> -->
			</div>

            <div class="btn-group">
                <button type="submit" class="btn-submit">申請する</button>
                <button type="button" class="btn-cancel" onclick="location.href='main.do'">キャンセル</button>
            </div>
        </form>
    </div>

    <script>
        // 페이지 로드 시 시작일의 최소 선택 가능 날짜를 오늘로 설정
        document.getElementById('startDate').setAttribute('min', new Date().toISOString().split('T')[0]);

        // 시작일이 변경될 때마다 종료일의 최소 날짜를 시작일로 업데이트
        function updateEndDateMin() {
            const startDate = document.getElementById('startDate').value;
            document.getElementById('endDate').setAttribute('min', startDate);
        }

        // 사유 선택에 따른 입력창 표시 여부 제어
        function toggleReason(val) {
            const input = document.getElementById('customReason');
            input.style.display = (val === '직접입력') ? 'block' : 'none';
            input.required = (val === '직접입력');
        }

        // 최종 유효성 검사 함수
        function validate() {
            const start = new Date(document.getElementById('startDate').value);
            const end = new Date(document.getElementById('endDate').value);
            
            let weekdayCount = 0;
            let currentDate = new Date(start);

            // 시작일부터 종료일까지 순회하며 평일(월~금) 개수 카운트
            while (currentDate <= end) {
                const dayOfWeek = currentDate.getDay(); // 0:일요일 ~ 6:토요일
                if (dayOfWeek !== 0 && dayOfWeek !== 6) {
                    weekdayCount++;
                }
                currentDate.setDate(currentDate.getDate() + 1);
            }

            // 주말만 선택하여 평일이 0일인 경우 전송 차단
            if (weekdayCount === 0) {
                alert('土日祝日を除いた平日のみを選択してください。.');
                return false;
            }

            return confirm('申請内容を提出しますか？ (土日 除く 平日 ' + weekdayCount + '日消化)');
        }
    </script>
</body>
</html>
