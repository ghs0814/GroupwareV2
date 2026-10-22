<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%
    // [1순위 고정] 자바 로직이 돌기 전, 요청과 응답 스트림의 구멍을 UTF-8로 선제 타격해서 열어둡니다.
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- [2순위 고정] JSTL이 브라우저 언어헤더를 읽어 동적으로 프로퍼티를 선택할 때, 깨지지 않도록 인코딩 필터 주입 --%>
<fmt:requestEncoding value="UTF-8" />
<fmt:setBundle basename="resources.message" scope="session" />

<!DOCTYPE html>
<html>
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<title><fmt:message key="join.page.title" /></title>
<link rel="stylesheet" href="css/join.css">
<link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@300;600;800&display=swap" rel="stylesheet">
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>

    <div class="join-card">
        <header class="brand-header">
            <div class="brand-logo">
                <span class="bold"><fmt:message key="login.brand.bold" /></span>
                <span class="thin"><fmt:message key="login.brand.thin" /></span>
            </div>
            <div class="subtitle"><fmt:message key="join.page.subtitle" /></div>
        </header>

        <form action="join.do" method="post" id="joinForm">
            
            <label class="field-label"><fmt:message key="join.label.empNo"/></label>
            <div class="input-group">
                <%-- placeholder 내부에 태그 깨짐을 방지하기 위해 var 변수 처리 --%>
                <fmt:message key="join.placeholder.empNo" var="plhEmpNo"/>
                <input type="text" name="empNo" id="empNo" placeholder="${plhEmpNo}" required autocomplete="off">
                <button type="button" class="btn-check" onclick="checkEmp()"><fmt:message key="join.btn.check"/></button>
            </div>

            <label class="field-label"><fmt:message key="join.label.password"/></label>
            <div style="margin-bottom: 18px;">
                <%-- 큰따옴표 충돌 오류 수정 --%>
                <fmt:message key="join.placeholder.password" var="plhPassword"/>
                <input type="password" name="password" id="password" placeholder="${plhPassword}" required>
            </div>

            <label class="field-label"><fmt:message key="join.label.name" /></label>
            <fmt:message key="join.placeholder.auto" var="plhAuto"/>
            <input type="text" id="empName" class="readonly-field" placeholder="${plhAuto}" readonly>

            <label class="field-label"><fmt:message key="join.label.dept" /></label>
            <input type="text" id="deptInfo" class="readonly-field" placeholder="${plhAuto}" readonly>

            <div class="button-area">
                <button type="button" class="btn-main btn-cancel" onclick="location.href='index.jsp'"><fmt:message key="join.btn.cancel" /></button>
                <button type="submit" class="btn-main btn-register"><fmt:message key="join.btn.submit" /></button>
            </div>
        </form>
    </div>

    <%-- JavaScript Alert에서 가져다 쓰기 편하도록 세팅된 히든 레이블 --%>
    <div style="display:none;">
        <span id="msg_empty_empNo"><fmt:message key="join.alert.emptyEmpNo" /></span>
        <span id="msg_success_check"><fmt:message key="join.alert.successCheck" /></span>
        <span id="msg_retired"><fmt:message key="join.alert.retired" /></span>
        <span id="msg_invalid_empNo"><fmt:message key="join.alert.invalidEmpNo" /></span>
        <span id="msg_need_check"><fmt:message key="join.alert.needCheck" /></span>
        <span id="msg_net_error"><fmt:message key="join.alert.netError" /></span>
    </div>

    <script>
    function checkEmp() {
        const empNo = $('#empNo').val().trim();
        if(!empNo) {
            // 스크립트 내부에 fmt태그를 직접 쓰면 인코딩 및 개행문자 문제가 생길 수 있어 히든 태그 text() 방식으로 우회 수정
            alert($('#msg_empty_empNo').text());
            return;
        }
        $.ajax({
            url: 'getEmpInfo.jsp',
            type: 'get',
            data: { empNo: empNo },
            dataType: 'json',
            success: function(data) {
                if(data.result === "success") {
                    if(data.retired === "N")
                    {
                        $('#empName').val(data.name);
                        $('#deptInfo').val(data.dept + " / " + data.position);
                        alert($('#msg_success_check').text());
                        $('#password').focus();                     
                    }else
                    {
                        alert($('#msg_retired').text());
                        return;
                    }
                    
                } else {
                    alert($('#msg_invalid_empNo').text());
                    $('#empNo').val('').focus();
                }
            },
            error: function() { alert($('#msg_net_error').text()); }
        });
    }

    $('#joinForm').submit(function() {
        if($('#empName').val() === "") {
            alert($('#msg_need_check').text());
            return false;
        }
        return true;
    });
    </script>
</body>
</html>
