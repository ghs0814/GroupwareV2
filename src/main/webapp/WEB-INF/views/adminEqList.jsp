<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.groupware.dto.EmployeeDTO"%>
<%@ page import="com.groupware.dto.EquipmentDTO"%>

<%
	EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
	if (loginEmp == null) {
		response.sendRedirect("index.jsp");
		return;
	}
	
	List<EmployeeDTO> empList = (List<EmployeeDTO>) request.getAttribute("reserveList");
	List<EquipmentDTO> eqList = (List<EquipmentDTO>) request.getAttribute("eqList");
	
	// 데이터 가져오기

%>
<!DOCTYPE html>
<html>
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
	<meta charset="UTF-8">
	<title>사내 시스템 - 재고 관리</title>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/css/adminEqList.css">
	<%-- <link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css?v=1.5"> --%>
</head>
				<%-- 
                1. 재고 수량 확인 ( SELECT ) -- ok
                2. 정보 수정 통해 ( UPDATE ) -- ok
                3. 영구 폐기를 통해 ( DELETE ) --ok 비품대여 신청 로직 완성 후 테스트 확인
                4. 신규 비품 등록 ( INSERT ) -- ok
                5. 검색 ( SEARCH ) --ok
                --%>
<body>
<script>
    function deleteEquipment(eqNo) {
        if (confirm("該当の備品データをシステムから永久に削除しますか？")) {
            document.getElementById("delEqNo").value = eqNo;
            document.getElementById("deleteForm").submit();
        }
    }

    function openUpdateModal(no, name, total, remain) {
        document.getElementById("upEqNo").value = no;
        document.getElementById("upEqName").value = name;
        document.getElementById("upTotalCount").value = total;
        document.getElementById("upRemainCount").value = remain;
        
        document.getElementById("updateModal").style.display = "block";
        document.getElementById("modalOverlay").style.display = "block";
    }

    function closeUpdateModal() {
        document.getElementById("updateModal").style.display = "none";
        document.getElementById("modalOverlay").style.display = "none";
    }

    // 🌟 [추가] 누락되었던 페이징 이동 함수 완벽 삽입
    function changePage(page) {
        location.href = "?page=" + page;
    }

	// 전역 변수 설정: 원본 텍스트, 이전 검색어, 현재 포커스 인덱스 기록
    const originalTexts = new Map();
    let lastKeyword = "";
    let currentMatchIndex = -1;

    window.addEventListener('DOMContentLoaded', () => {
        // 페이지 로드 시 각 셀의 순수 원본 텍스트만 메모리에 기록
        document.querySelectorAll('.eq-no, .eq-name').forEach((td, index) => {
            td.setAttribute('data-search-id', index);
            originalTexts.set(index.toString(), td.textContent.trim());
        });
    });

    function searchAndScroll() {
        const searchType = document.getElementById("searchType").value;
        const keyword = document.getElementById("searchKeyword").value.trim();
        const lowerKeyword = keyword.toLowerCase();

        if (keyword === "") {
            alert("検索キーワードを入力してください。");
            return;
        }

        // [핵심 로직] 새로운 단어를 검색한 경우: 마킹을 새로 하고 인덱스를 초기화함
        if (lastKeyword !== lowerKeyword) {
            // 1. 기존 노란 하이라이트 마킹 전체 초기화
            document.querySelectorAll('.eq-no, .eq-name').forEach(td => {
                const id = td.getAttribute('data-search-id');
                if (originalTexts.has(id)) {
                    td.innerHTML = originalTexts.get(id); 
                }
            });

            // 2. 루프를 돌며 일치하는 모든 항목에 하이라이트 마킹 표시
            const rows = document.querySelectorAll(".eq-row");
            rows.forEach(row => {
                const eqNoTd = row.querySelector(".eq-no");
                const eqNameTd = row.querySelector(".eq-name");

                if (searchType === "all" || searchType === "eqNo") {
                    markCellIfMatch(eqNoTd, lowerKeyword);
                }
                if (searchType === "all" || searchType === "eqName") {
                    markCellIfMatch(eqNameTd, lowerKeyword);
                }
            });

            // 상태 기록 갱신
            lastKeyword = lowerKeyword;
            currentMatchIndex = -1; 
        }

        // 3. 화면에 생성된 모든 마킹 요소 리스트를 수집
        const allMarks = document.querySelectorAll(".mark-highlight");
		
        if (allMarks.length > 0) {
            // 다음 인덱스로 이동 (마지막 항목에 도달했다면 다시 0번째 첫 항목으로 회귀)
            currentMatchIndex++;
            if (currentMatchIndex >= allMarks.length) {
                currentMatchIndex = 0; 
            }

            // 4. 선택된 순서의 다음(Next) 마킹 위치로 부드럽게 스크롤 이동
            allMarks[currentMatchIndex].scrollIntoView({
                behavior: "smooth",
                block: "center"
            });

            // 5. 사용자가 편하게 다음 검색을 이어나가도록 텍스트창 블록 지정 (기존 유지)
            document.getElementById("searchKeyword").select();
        } else {
            alert("一致する備品が見つかりません。");
            document.getElementById("searchKeyword").focus();
            lastKeyword = "";
            currentMatchIndex = -1;
        }
    }

    // 텍스트 매칭 및 실제 태그 치환 함수 (숫자 보존 정규식 방식 적용)
    function markCellIfMatch(td, lowerKeyword) {
        if (!td) return false;
        
        const originalText = td.textContent.trim();
        const lowerText = originalText.toLowerCase();
        const index = lowerText.indexOf(lowerKeyword);

        if (index >= 0) {
            const escapedKeyword = lowerKeyword.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
            const regex = new RegExp(escapedKeyword, "gi");
            td.innerHTML = originalText.replace(regex, `<span class="mark-highlight">$&</span>`);
            return true; 
        }
        return false; 
    }
</script>
<jsp:include page="header.jsp" />

	<div class="dashboard-container" style="padding-top: 20px;">
	    <div style="margin-bottom: 15px;">
	        <h2 style="margin: 0; font-size: 22px; font-weight: 700; color: #1e293b;">共用備品マスターデータ管理</h2>
	        <!-- <h2 style="margin: 0; font-size: 22px; font-weight: 700; color: #1e293b;">공용 비품 마스터 데이터 관리</h2> -->
	    </div> 
	    
	    <div class="insert-box">
	        <h3>備品の新規登録</h3>
	        <!-- <h3>신규 비품 등록</h3> -->
	        <form action="insertEq.do" method="post" class="insert-form-flex">
	            <input type="text" name="eqName" class="search-input" placeholder="備品名入力" required>
	            <input type="number" name="totalCount" class="search-input" style="max-width: 200px;" placeholder="初期数量を入力" required>
	            <button type="submit" class="btn-register">登録</button>
	        </form>
	    </div>
	    
	    <div class="search-bar-container">
		    <div class="search-form">
		        <select id="searchType" class="search-select">
		            <option value="all">全体検索</option>
		            <option value="eqName">備品名</option>
		            <option value="eqNo">備品コード</option>
<!-- 		            <option value="all">전체 검색</option>
		            <option value="eqName">비품 명칭</option>
		            <option value="eqNo">비품 번호</option> -->
		        </select>
		        <input type="text" id="searchKeyword" class="search-input" placeholder="使用する備品名またはコードを入力..." autocomplete="off" onkeyup="if(event.key === 'Enter') searchAndScroll()">
		        <button type="button" class="btn-search" onclick="searchAndScroll()">検索および移動</button>
		    </div>
		</div>
	
	    <div class="table-wrapper">
	        <table>
	            <thead>
	                <tr>
	                    <th style="width: 15%;">備品番号</th>
	                    <th style="width: 35%;">備品名</th>
	                    <th style="width: 15%;">保有総数量</th>
	                    <th style="width: 15%;">貸出可能数</th>
	                    <th style="width: 20%;">データ管理</th>
	                </tr>
	            </thead>
	            <tbody>
	                <% 
						// 🌟 [추가] 페이징 처리를 위한 변수 선언 및 연산 로직
						int pageSize = 10; 
						int currentPage = 1;
						
						String pageParam = request.getParameter("page");
						if (pageParam != null && !pageParam.isEmpty()) {
							try {
								currentPage = Integer.parseInt(pageParam);
							} catch (NumberFormatException e) {
								currentPage = 1;
							}
						}

						int totalCount = 0;
						if (eqList != null) {
							totalCount = eqList.size();
						}

						int totalPages = (int) Math.ceil((double) totalCount / pageSize);
						if (totalPages == 0) { totalPages = 1; }
						if (currentPage > totalPages) { currentPage = totalPages; }

						int printCount = 0;
						int skipCount = 0;
						int targetStartIndex = (currentPage - 1) * pageSize;

						if (eqList != null && !eqList.isEmpty()) {
							for (EquipmentDTO eq : eqList) { 
								if (eq == null) continue;

								// 현재 페이지 이전 데이터는 패스(Skip)
								if (skipCount < targetStartIndex) {
									skipCount++;
									continue;
								}
								
								// 화면에 10개 출력했으면 루프 종료
								if (printCount >= pageSize) {
									break;
								}
								printCount++;
					%>
	                <tr class="eq-row">
	                    <td class="eq-no" style="color: #64748b; font-weight: 500;"><%= eq.getEqNo() %></td>
	                    <td class="eq-name" style="text-align: left; padding-left: 24px; font-weight: 600; color: #1e293b;"><%= eq.getEqName() %></td>
	                    <td><%= eq.getTotalCount() %> EA</td>
	                    <td>
	                    	<span class="<%= eq.getRemainCount() > 0 ? "status-badge status-blue" : "status-badge status-red" %>">
	                    		<%= eq.getRemainCount() %> EA
	                    	</span>
	                    </td>
	                    <td>
	                        <button class="btn-action-edit" onclick="openUpdateModal('<%= eq.getEqNo() %>', '<%= eq.getEqName() %>', '<%= eq.getTotalCount() %>', '<%= eq.getRemainCount() %>')">情報修正</button>
	                        <button class="btn-action-del" onclick="deleteEquipment('<%= eq.getEqNo() %>')">永久廃棄</button>
	                    </td>
	                </tr>
	                <%  } } else { %>
	                <tr><td colspan="5" class="empty-data">システムに登録された備品マスタデータがありません。</td></tr>
	                <% } %>
	            </tbody>
	        </table>
	    </div>

		<% if (totalCount > 0) { %>
			<div class="pagination-container" style="display: flex; justify-content: center; align-items: center; gap: 6px; margin: 25px 0 15px 0; font-family: -apple-system, BlinkMacSystemFont, sans-serif;">
				<button type="button" onclick="changePage(1)" <%= currentPage == 1 ? "disabled" : "" %> style="padding: 6px 10px; border: 1px solid #e2e8f0; background-color: #ffffff; color: #64748b; border-radius: 6px; cursor: pointer; font-weight: bold; font-size: 13px;">&lt;&lt;</button>
				<button type="button" onclick="changePage(<%= currentPage - 1 %>)" <%= currentPage == 1 ? "disabled" : "" %> style="padding: 6px 12px; border: 1px solid #e2e8f0; background-color: #ffffff; color: #64748b; border-radius: 6px; cursor: pointer; font-weight: bold; font-size: 13px;">&lt;</button>
				
				<% for (int p = 1; p <= totalPages; p++) { %>
					<button type="button" onclick="changePage(<%= p %>)" 
							style="<%= p == currentPage ? "padding: 6px 12px; border: 2px solid #6366f1; background-color: #6366f1; color: #ffffff; font-weight: bold; border-radius: 6px; cursor: pointer; font-size: 13px;" : "padding: 6px 12px; border: 1px solid #cbd5e1; background-color: #ffffff; color: #334155; font-weight: normal; border-radius: 6px; cursor: pointer; font-size: 13px;" %>">
						<%= p %>
					</button>
				<% } %>
				
				<button type="button" onclick="changePage(<%= currentPage + 1 %>)" <%= currentPage == totalPages ? "disabled" : "" %> style="padding: 6px 12px; border: 1px solid #e2e8f0; background-color: #ffffff; color: #64748b; border-radius: 6px; cursor: pointer; font-weight: bold; font-size: 13px;">&gt;</button>
				<button type="button" onclick="changePage(<%= totalPages %>)" <%= currentPage == totalPages ? "disabled" : "" %> style="padding: 6px 10px; border: 1px solid #e2e8f0; background-color: #ffffff; color: #64748b; border-radius: 6px; cursor: pointer; font-weight: bold; font-size: 13px;">&gt;&gt;</button>
			</div>
		<% } %>

	</div>

<div class="modal-overlay" id="modalOverlay" onclick="closeUpdateModal()"></div>
<div id="updateModal">
    <h3>備品情報修正</h3>
    <form action="updateEq.do" method="post">
        <input type="hidden" name="eqNo" id="upEqNo">
        <div class="update-group">
            <label>備品名</label>
            <input type="text" name="eqName" id="upEqName" required>
        </div>
        <div class="update-group">
            <label>保有総数量</label>
            <input type="number" name="totalCount" id="upTotalCount" required>
        </div>
        <div class="update-group">
            <label>レンタル可能残数量</label>
            <input type="number" name="remainCount" id="upRemainCount" required>
        </div>
        <div class="modal-btn-group">
            <button type="button" class="btn-modal-cancel" onclick="closeUpdateModal()">キャンセル</button>
            <button type="submit" class="btn-modal-submit">修正反映</button>
        </div>
    </form>
</div>

<form id="deleteForm" action="deleteEq.do" method="post">
    <input type="hidden" name="eqNo" id="delEqNo">
</form>
</body>
</html>
