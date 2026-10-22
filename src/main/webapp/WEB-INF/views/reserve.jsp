<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="com.groupware.dto.EmployeeDTO"%>
<%@ page import="com.groupware.dto.RoomDTO"%>
<%
EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
if (loginEmp == null) {
	response.sendRedirect("index.jsp");
	return;
}

RoomDTO roomInfo = (RoomDTO) request.getAttribute("roomInfo");
if (roomInfo == null) {
	response.sendRedirect("main.jsp");
	return;
}
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>社内システム - 会議室予約申請</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/reserve.css">

<script>
    let timerInterval = null;
    let selectedTimes = [];

    function isContinuous(times) {
        if (times.length <= 1) return true;
        for (let i = 0; i < times.length - 1; i++) {
            let t1 = parseInt(times[i].split(':')[0]);
            let t2 = parseInt(times[i+1].split(':')[0]);
            if (t2 - t1 !== 1) return false;
        }
        return true;
    }

    function startTimer(startStr, endStr) {
        let timeLeft = 300;
        const timerBox = document.getElementById("timerBox");
        const timeDisplay = document.getElementById("timeDisplay");
        const submitBtn = document.getElementById("submitBtn");
        const holdBtn = document.getElementById("holdBtn");
        const holdTimeText = document.getElementById("holdTimeText");
        holdTimeText.innerText = "[" + startStr + " ~ " + endStr + "]";

        timerBox.style.display = "block"; 
        submitBtn.disabled = false;
        holdBtn.style.display = "none"; 

        document.querySelectorAll('.time-btn').forEach(btn => btn.disabled = true);

        if(timerInterval) clearInterval(timerInterval);
        timerInterval = setInterval(function() {
            timeLeft--;
            let minutes = Math.floor(timeLeft / 60);
            let seconds = timeLeft % 60;
            timeDisplay.textContent = (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;

            if (timeLeft <= 0) {
 
                clearInterval(timerInterval);
                alert("予約の有効期限が切れました。最初からやり直してください。");
                location.reload(); 
            }
        }, 1000);
    }

    document.addEventListener("DOMContentLoaded", function() {
        const timeButtons = document.querySelectorAll('.time-btn');
        const resDateInput = document.getElementById("resDate");
        const holdBtn = document.getElementById("holdBtn");
        
        const today = new Date();
        const todayStr = today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0') + '-' + String(today.getDate()).padStart(2, '0');
        resDateInput.setAttribute("min", todayStr);
        
 
        timeButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                if(this.disabled) return;
                const timeStr = this.getAttribute('data-time');

                if (this.classList.contains('active')) {
                    this.classList.remove('active');
   
                    selectedTimes = selectedTimes.filter(t => t !== timeStr);
                    if (!isContinuous(selectedTimes)) {
                        alert("連続した時間のみ選択可能です。");
                        this.classList.add('active');
                        selectedTimes.push(timeStr);
                        selectedTimes.sort();
                    }
                } else {
                    selectedTimes.push(timeStr);
                    selectedTimes.sort();
                    if (!isContinuous(selectedTimes)) {
                        alert("連続した時間のみ選択可能です。");
                        selectedTimes = selectedTimes.filter(t => t !== timeStr);
                    } else {
                        this.classList.add('active');
                    }
                }
            });
        });

        holdBtn.addEventListener('click', function() {
            <% boolean isRoomEnableJs = "Y".equals(roomInfo.getEnable()); %>
            <% if (!isRoomEnableJs) { %>
                alert("現在メンテナンス中のため、予約できない会議室です。");
                return;
            <% } %>

            const selectedDate = resDateInput.value;
            if(!selectedDate) { alert("先に予約日を選択してください。"); return; }
            if(selectedTimes.length === 0) { alert("予約する時間を選択してください。"); return; }

            const startTime = selectedTimes[0];
            let lastHour = parseInt(selectedTimes[selectedTimes.length - 1].split(':')[0]);
            let endTimeStr = (lastHour + 1 < 10 ? '0' + (lastHour + 1) : (lastHour + 1)) + ':00';
            
            const roomId = document.getElementById("roomId").value;
            const empNo = document.getElementById("empNo").value;

            const formData = new URLSearchParams();
            formData.append("roomId", roomId);
            formData.append("empNo", empNo);
            formData.append("resDate", selectedDate);
            formData.append("startTime", startTime);
            formData.append("endTime", endTimeStr); 

            fetch('holdReserve.do', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData.toString()
            })
            .then(response => response.json())
            .then(data => {
                if(data.result === 'success') {
                    document.getElementById('resNo').value = data.resNo;
                    startTimer(startTime, endTimeStr);
                } else {
                   
                    alert("ご希望の時間帯に、すでに他の予約が登録されています。ページを更新してもう一度お試しください。");
                    location.reload();
                }
            })
            .catch(error => console.error('Error:', error));
        });

        resDateInput.addEventListener('change', function() {
            const selectedDate = this.value;
            if (!selectedDate) return;
            
            if (selectedDate < todayStr) {
                alert("過去の日付は予約できません。");
                this.value = "";
                 return;
            }
            
            const roomId = document.getElementById("roomId").value;

            fetch('checkReservedTime.do?roomId=' + roomId + '&resDate=' + selectedDate)
                .then(response => response.json())
                 .then(reservedTimes => {
                    selectedTimes = []; 
                    timeButtons.forEach(btn => { btn.disabled = false; btn.classList.remove('active'); });
                    
                    clearInterval(timerInterval);
                     document.getElementById("timerBox").style.display = "none";
                    document.getElementById("submitBtn").disabled = true;
                    document.getElementById("holdBtn").style.display = "block";
                    document.getElementById("resNo").value = "";

                    const now = new Date();
                    const isToday = (selectedDate === todayStr); 
                    const currentHour = now.getHours();
                    timeButtons.forEach(btn => {
                        const btnTime = btn.getAttribute('data-time');
                        const btnHour = parseInt(btnTime.split(':')[0]);

                        if (reservedTimes.includes(btnTime)) {
                             btn.disabled = true;
                        }
                        else if (isToday && btnHour <= currentHour) {
                            btn.disabled = true;
                         }
                    });
                })
                .catch(error => console.error('Error:', error));
        });
    });

    function validateForm() {
        if (!document.getElementById("resNo").value) { alert("先に予約時間を指定してください。"); return false; }
        if (!document.getElementById("purpose").value.trim()) { alert("使用目的を入力してください。"); return false; }
        return confirm("入力された内容で会議室の予約を確定しますか？");
    }
</script>
</head>
<body>

<jsp:include page="header.jsp" />

<div class="form-container">
    <h2>
        📋 会議室予約申請
        <% if ("Y".equals(loginEmp.getManager())) { %>
            <button type="button" class="btn-modify"
                onclick="location.href='roomUpdate.do?roomId=<%=roomInfo.getRoomId()%>'">
                情報修正
            </button>
        <% } %>
     </h2>

    <% boolean isRoomEnable = "Y".equals(roomInfo.getEnable()); %>

    <div class="info-box">
        <h3>
            <%=roomInfo.getRoomName()%>
            <% if (isRoomEnable) { %>
                <span class="status-badge bg-success" style="margin-left: 10px;">予約可能</span>
            <% } else { %>
                <span class="status-badge bg-danger" style="margin-left: 10px;">メンテナンス中 (予約不可)</span>
            <% } %>
        </h3>
        <p>
            <strong>収容人数:</strong> <%=roomInfo.getCapacity()%>名 |
            <strong>プロジェクター:</strong> <%= "Y".equals(roomInfo.getHasBeam()) ? "あり" : "なし" %>
        </p>
        <p><%=roomInfo.getDescription()%></p>
    </div>

    <div class="timer-box" id="timerBox">
        <span id="holdTimeText" style="font-weight: 800; font-size: 15px;"></span><br>
        対象の時間帯を仮押さえしました。
        制限時間内に確定してください。 
        <span class="timer-text" id="timeDisplay">05:00</span>
    </div>

    <form action="reserveProcess.do" method="post" onsubmit="return validateForm();">
        <input type="hidden" id="resNo" name="resNo" value=""> 
        <input type="hidden" id="roomId" name="roomId" value="<%=roomInfo.getRoomId()%>"> 
        <input type="hidden" id="empNo" name="empNo" value="<%=loginEmp.getEmpNo()%>">

        <div class="form-group">
            <label for="resDate">予約日</label> 
            <input type="date" id="resDate" name="resDate" class="form-control" required>
         </div>

        <div class="form-group">
            <label>予約時間の選択</label>
            <div class="time-grid" id="timeGrid">
                <% String disabledAttr = isRoomEnable ? "" : "disabled"; %>
                <button type="button" class="time-btn" data-time="09:00" <%=disabledAttr%>>09:00</button>
                <button type="button" class="time-btn" data-time="10:00" <%=disabledAttr%>>10:00</button>
                <button type="button" class="time-btn" data-time="11:00" <%=disabledAttr%>>11:00</button>
                <button type="button" class="time-btn" data-time="12:00" <%=disabledAttr%>>12:00</button>
                <button type="button" class="time-btn" data-time="13:00" <%=disabledAttr%>>13:00</button>
                <button type="button" class="time-btn" data-time="14:00" <%=disabledAttr%>>14:00</button>
                <button type="button" class="time-btn" data-time="15:00" <%=disabledAttr%>>15:00</button>
                <button type="button" class="time-btn" data-time="16:00" <%=disabledAttr%>>16:00</button>
                <button type="button" class="time-btn" data-time="17:00" <%=disabledAttr%>>17:00</button>
                <button type="button" class="time-btn" data-time="18:00" <%=disabledAttr%>>18:00</button>
            </div>

            <button type="button" class="btn btn-hold" id="holdBtn" <%=disabledAttr%> 
                <% if (!isRoomEnable) { %> style="background-color: #cbd5e1; cursor: not-allowed;" <% } %>>
                選択した時間を確保
            </button>
        </div>

        <div class="form-group">
            <label for="purpose">使用目的</label> 
            <input type="text" id="purpose" name="purpose" class="form-control" 
                    placeholder="例：週次部門会議" required>
        </div>

        <div class="btn-area">
            <button type="submit" class="btn btn-submit" id="submitBtn" disabled>予約確定</button>
            <a href="main.jsp" class="btn btn-cancel">キャンセル</a>
        </div>
    </form>
</div>

</body>
</html>
