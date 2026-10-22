<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
    <meta charset="UTF-8">
    <title>休暇申請書詳細</title>
    <link rel="stylesheet" href="css/leaveDetail.css"> 
</head>
<body>
    <div class="detail-container">
        <div class="header-area">
            <h2>🏖️ 休暇申請書詳細</h2>
     <span class="status-badge ${leave.status == '반려됨' ? 'status-red' : 'status-blue'}">
    <c:choose>
        <c:when test="${leave.status == '승인대기'}">承認待ち</c:when>
        <c:when test="${leave.status == '승인완료'}">承認完了</c:when>
        <c:when test="${leave.status == '반려됨'}">差し戻し</c:when>
        <c:otherwise>${leave.status}</c:otherwise>
    </c:choose>
</span>
        </div>
        
       <div class="approval-table">
    <%-- 기안자 레벨(leave.empLevel)부터 5단계까지 반복 --%>	
    <c:forEach begin="${leave.empLevel}" end="5" var="i">
        <c:set var="signName" value="sign${i}" />
        <c:set var="signDate" value="sign${i}Date" />
        
        <div class="stamp-box">
            <div class="stamp-step">
                <c:choose>
                    <c:when test="${i == 5}">最終承認</c:when>
                    <c:otherwise>${i}段階承認</c:otherwise>
                </c:choose>
            </div>
            <%-- 서명 값이 있으면 이름 출력, 없으면 공백 --%>
            <div class="stamp-name">${not empty leave[signName] ? leave[signName] : ''}</div>
            <div class="stamp-date">${not empty leave[signDate] ? leave[signDate] : ''}</div>
        </div>
    </c:forEach>
</div>

        <table class="info-table">
            <tr>
                <th>文書番号</th><td>${leave.leaveNo}</td>
                <th>起案者情報</th><td>${leave.empName} (Lv.${leave.empLevel})</td>
            </tr>
            <tr>
                <th>休暇期間</th><td>${leave.startDate} ~ ${leave.endDate}</td>
                <th>使用日数</th><td>${leave.useDays} 日	</td>
            </tr>
            
        	<tr>
    			<th>休暇理由</th>
    			<%-- colspan은 td 태그 안에 위치해야 합니다 --%>
    				<td colspan="3" class="reason-cell">
       				 	<c:choose>
           					 <c:when test="${leave.reason == '연차'}">年次有給休暇</c:when>
            					<c:when test="${leave.reason == '병가'}">傷病休暇</c:when>
           						<c:when test="${leave.reason == '경조사'}">慶弔休暇</c:when>
            					<c:otherwise>${leave.reason}</c:otherwise>
        				</c:choose>
    				</td>
			</tr>
        </table>

      <%--   <div style="background: #fff3cd; border: 1px solid #ffeeba; padding: 15px; margin-bottom: 20px; text-align: center;">
            <strong>[디버깅 정보]</strong><br>
            현재 문서 상태: ${leave.status}<br>
            현재 결재 단계(approvalStep): <strong>${leave.approvalStep}</strong><br>
            로그인 사용자 레벨(empLevel): <strong>${loginEmp.empLevel}</strong><br>
            결과: <span style="color: ${loginEmp.empLevel == leave.approvalStep ? 'green' : 'red'}">
                  ${loginEmp.empLevel == leave.approvalStep ? '레벨 일치 (버튼 출력 가능)' : '레벨 불일치 (버튼 출력 불가)'}
                  </span>
        </div> --%>

        <div class="btn-group">
            <button type="button" class="btn-list" onclick="location.href='documentList.do?tab=leave'">一覧に戻る</button>
    
            <%-- 승인 대기 중이고, 로그인한 사용자의 직급이 현재 결재 단계와 같으면 버튼 노출 --%>
            <c:if test="${leave.status == '승인대기'}">
                <c:if test="${loginEmp.empLevel == leave.approvalStep}">
                    <form action="leaveApproveProcess.do" method="post" style="display:inline;">
                        <input type="hidden" name="leaveNo" value="${leave.leaveNo}">
                        <input type="hidden" name="step" value="${leave.approvalStep}">
                        <input type="hidden" name="action" value="approve">
                        <button type="submit" class="btn-approve">決裁承認</button>
                        
                    </form>
                    
                    <form action="leaveApproveProcess.do" method="post" style="display:inline;">
                        <input type="hidden" name="leaveNo" value="${leave.leaveNo}">
                        <input type="hidden" name="step" value="${leave.approvalStep}">
                        <input type="hidden" name="action" value="reject">
                        <button type="submit" class="btn-reject">決裁差戻</button>
                        
                    </form>
                </c:if>
            </c:if>
        </div>
    </div>
</body>
</html>
