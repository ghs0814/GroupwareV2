<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="com.groupware.dto.EmployeeDTO"%>
<%
    List<EmployeeDTO> empList = (List<EmployeeDTO>) request.getAttribute("empList");
    Integer currentPage = (Integer) request.getAttribute("currentPage");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    if (currentPage == null) currentPage = 1;
    if (totalPages == null) totalPages = 1;
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<title>社内連絡先 - GroupWare</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/addressBook.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

<script>
    function filterEmployees() {
        const keyword = document.getElementById('searchInput').value.toLowerCase();
        const cards = document.getElementsByClassName('employee-card');
        
        for (let card of cards) {
            const name = card.getAttribute('data-name').toLowerCase();
            const dept = card.getAttribute('data-dept').toLowerCase();
            const no = card.getAttribute('data-no').toLowerCase();
            
            if (name.includes(keyword) || dept.includes(keyword) || no.includes(keyword)) {
                card.style.display = 'flex';
            } else {
                card.style.display = 'none';
            }
        }
    }
</script>
</head>
<body>

<jsp:include page="header.jsp" />

<div class="address-book-container">
    
    <div class="search-header">
        <div class="search-title">
            <h2>社内連絡先一覧</h2>
            <p>社員の連絡先情報を確認できます。</p>
        </div>
   
        <div class="search-box">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" id="searchInput" onkeyup="filterEmployees()" placeholder="現在のページ内から検索">
        </div>
    </div>

    <div class="employee-grid">
        <% 
            if (empList != null && !empList.isEmpty()) {
          
                for (EmployeeDTO emp : empList) {
                    // 役職マッピング
                    String levelName = "社員";
                    switch(emp.getEmpLevel()) {
                        case 5: levelName = "役員"; break;
                        case 4: levelName = "部長"; break; // 또는 部署長
                        case 3: levelName = "課長"; break;
                        case 2: levelName = "係長"; break; // 한국의 '대리' 직급에 가장 매칭되는 일본 직책
                        default: levelName = "社員"; break;
                    }
                    
                    String deptName = emp.getDept() != null ? emp.getDept() : "未所属";
        %>
        <div class="employee-card" 
             data-name="<%=emp.getEmpName()%>" 
             data-dept="<%=deptName%>" 
             data-no="<%=emp.getEmpNo()%>">
            
            <div class="emp-main-info">
     
                <div class="emp-no-badge">NO. <%=emp.getEmpNo()%></div>
                <div class="emp-name-area">
                    <div class="emp-name">
                        <%=emp.getEmpName()%>
                        <% if ("Y".equals(emp.getManager())) { %>
                            <span class="admin-badge">ADMIN</span>
                        <% } %>
                    </div>
                    <div class="emp-position-dept">
                        <%=levelName%> <span class="dept">| <%=deptName%></span>
                    </div>
                </div>
            </div>
            
            <div class="emp-details">
            
                <div class="detail-item">
                    <i class="fa-solid fa-envelope"></i>
                    <div>
                        <span class="detail-label">Email</span>
                        <span class="detail-value"><%=emp.getEmpNo()%>@groupware.com</span>
                    </div>
                </div>
                <div class="detail-item">
                    <i class="fa-solid fa-phone"></i>
                    <div>
                        <span class="detail-label">Extension</span>
                        <span class="detail-value">700-<%= String.format("%03d", emp.getEmpNo() % 1000) %></span>
                    </div>
                </div>
            </div>
        </div>
        <% 
                }
            } else {
        %>
            <div class="no-data">登録されている連絡先情報がありません。</div>
        <%
            }
        %>
    </div>

    <div class="pagination">
        <% if (currentPage > 1) { %>
            <a href="addressBook.do?page=<%=currentPage - 1%>" class="page-link"><i class="fa-solid fa-chevron-left"></i></a>
        <% } %>
        
        <% 
            for (int i = 1; i <= totalPages; i++) { 
                if (i == currentPage) {
        %>
            <span class="page-link active"><%=i%></span>
        <%      } else { %>
            <a href="addressBook.do?page=<%=i%>" class="page-link"><%=i%></a>
        <% 
                }
            } 
        %>
        
        <% if (currentPage < totalPages) { %>
            <a href="addressBook.do?page=<%=currentPage + 1%>" class="page-link"><i class="fa-solid fa-chevron-right"></i></a>
        <% } %>
    </div>
</div>

</body>
</html>
