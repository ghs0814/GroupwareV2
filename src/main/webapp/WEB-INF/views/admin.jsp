<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="com.groupware.dto.EmployeeDTO"%>

<%
	 EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
	if (loginEmp == null) {
		response.sendRedirect("index.jsp");
		return;
	} 
	
	List<EmployeeDTO> empList = (List<EmployeeDTO>) request.getAttribute("reserveList");
	
	// 데이터 가져오기
    
    List<EmployeeDTO> reserveList = (List<EmployeeDTO>) request.getAttribute("empList");
%>
<!DOCTYPE html>
<html>
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
<meta charset="UTF-8">
<title>사내 시스템 - 사원 관리</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/admin.css">
</head>
<body>
<script type="text/javascript">

	//전역 변수 설정: 스크롤 이동 상태 관리용
	let lastKeyword = "";
	let currentMatchIndex = -1;
	
	function searchAndScroll() {
	    const searchType = document.getElementById("searchType").value;
	    const keyword = document.getElementById("searchKeyword").value.trim();
	    const lowerKeyword = keyword.toLowerCase();
	
	    if (keyword === "") {
	        alert("検索キーワードを入力してください。");
	        return;
	    }
	
	    // 1. 새로운 키워드로 검색 시 기존 하이라이트 스타일 전부 제거 (기존 마크 태그 해제)
	    if (lastKeyword !== lowerKeyword) {
	        // 이전 검색 시 생성된 custom-highlight 스팬 태그를 순수 텍스트로 환원
	        document.querySelectorAll(".custom-highlight-span").forEach(span => {
	            const parent = span.parentNode;
	            if (parent) {
	                parent.replaceChild(document.createTextNode(span.textContent), span);
	                parent.normalize(); // 쪼개진 텍스트 노드 하나로 병합
	            }
	        });
	
	        // 부서 셀 하이라이트 클래스 및 배경색 리셋
	        document.querySelectorAll(".custom-target-highlight").forEach(td => {
	            td.classList.remove("custom-target-highlight");
	            td.style.backgroundColor = "";
	        });
	
	        // 2. 전체 행을 돌며 조건에 맞는 데이터 매칭 및 글자 자체에 스타일 부여
	        const rows = document.querySelectorAll(".table-wrapper tbody tr");
	        rows.forEach((row) => {
	            const tds = row.querySelectorAll("td");
	            if (tds.length >= 6) {
	                const empNoTd = tds[0];
	                const empNameTd = tds[1];
	                const deptTd = tds[5];
	
	                // [사원 번호 검색]
	                if (searchType === "all" || searchType === "emNo") {
	                    highlightSpecificText(empNoTd, keyword);
	                }
	                
	                // [성명 검색]
	                if (searchType === "all" || searchType === "emName") {
	                    highlightSpecificText(empNameTd, keyword);
	                }
	                
	                // [부서 검색] 내부 select 구조를 깨지 않기 위해 TD 통째로 스타일 적용
	                if (searchType === "all" || searchType === "emDept") {
	                    const selectEl = deptTd.querySelector("select");
	                    let deptText = selectEl ? selectEl.options[selectEl.selectedIndex].text : deptTd.textContent.trim();
	
	                    if (deptText.toLowerCase().includes(lowerKeyword)) {
	                        deptTd.classList.add("custom-target-highlight");
	                        deptTd.style.backgroundColor = "#fef08a"; 
	                    }
	                }
	            }
	        });
	
	        lastKeyword = lowerKeyword;
	        currentMatchIndex = -1; 
	    }
	
	    // 3. 스크롤 이동 대상 요소들을 리스트로 수집 (.custom-highlight-span 과 .custom-target-highlight)
	    const allTargets = document.querySelectorAll(".custom-highlight-span, .custom-target-highlight");
	    
	    if (allTargets.length > 0) {
	        currentMatchIndex++;
	        if (currentMatchIndex >= allTargets.length) {
	            currentMatchIndex = 0; 
	        }
	
	        // 4. 잡힌 타깃 위치로 화면을 부드럽게 스크롤
	        allTargets[currentMatchIndex].scrollIntoView({
	            behavior: "smooth",
	            block: "center"
	        });
	
	        document.getElementById("searchKeyword").select();
	    } else {
	        alert("一致する社員または部署が見つかりません。");
	        document.getElementById("searchKeyword").focus();
	        lastKeyword = "";
	        currentMatchIndex = -1;
	    }
	}
	
	// 엘리먼트 내부에서 딱 '그 글자만' 찾아내어 안전하게 스타일용 SPAN을 감싸는 함수
	function highlightSpecificText(element, keyword) {
	    if (!element || !keyword) return;
	
	    const walker = document.createTreeWalker(element, NodeFilter.SHOW_TEXT, null, false);
	    const textNodes = [];
	    while (walker.nextNode()) {
	        textNodes.push(walker.currentNode);
	    }
	
	    // 텍스트 노드 역순으로 순회하면서 키워드 매칭 영역 스팬 래핑 (JSP 컴파일 간섭 우회)
	    textNodes.forEach(node => {
	        const text = node.nodeValue;
	        const lowerText = text.toLowerCase();
	        const lowerKeyword = keyword.toLowerCase();
	        let index = lowerText.indexOf(lowerKeyword);
	
	        if (index >= 0) {
	            const range = document.createRange();
	            range.setStart(node, index);
	            range.setEnd(node, index + keyword.length);
	
	            const highlightSpan = document.createElement("span");
	            highlightSpan.className = "custom-highlight-span";
	            highlightSpan.style.backgroundColor = "#fef08a";
	            highlightSpan.style.color = "#000";
	            highlightSpan.style.fontWeight = "bold";
	            highlightSpan.style.padding = "2px 4px";
	            highlightSpan.style.borderRadius = "4px";
	
	            range.surroundContents(highlightSpan);
	        }
	    });
	}

	// 페이징 이동 함수
	function changePage(page) {
		location.href = "?page=" + page;
	}
</script>
	<jsp:include page="header.jsp" />
    
    <div class="container">
	
	    <div class="table-title-area">
        	<h2>ぜんじゅうぎょういんかんり</h2>
        </div>
        <div class="search-bar-container" 
 		 style="max-width: 1160px; width: 100%; padding: 10px 0 15px 0; margin: 0 auto; box-sizing: border-box; position: sticky; top: 65px; z-index: 99; background-color: #f4f7fa;">
			    <div class="search-form" 
			         style="display: flex; gap: 10px; background: #ffffff; padding: 12px; border-radius: 12px; border: 1px solid #e2e8f0; box-shadow: 0 4px 12px -2px rgba(0, 0, 0, 0.06); align-items: center; width: 100%; box-sizing: border-box;">
			        
			        <select id="searchType" class="search-select" 
			                style="width: 130px; height: 42px; padding: 0 10px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; color: #475569; outline: none; background-color: #ffffff; cursor: pointer; font-family: -apple-system, BlinkMacSystemFont, sans-serif;">
			            <option value="all">全体検索</option>
			            <option value="emNo">社員番号</option>
			            <option value="emName">名前</option>
			            <option value="emDept">部署</option>
			        </select>
			        
			        <input type="text" id="searchKeyword" class="search-input" placeholder="社員番号を入力.." autocomplete="off" 
			               onkeyup="if(event.key === 'Enter') searchAndScroll()"
			               onfocus="this.style.borderColor='#6366f1'" 
			               onblur="this.style.borderColor='#cbd5e1'"
			               style="flex-grow: 1; height: 42px; padding: 0 15px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; box-sizing: border-box; outline: none; background-color: #ffffff; color: #334155; font-family: -apple-system, BlinkMacSystemFont, sans-serif; transition: border-color 0.15s ease;">
			        
			        <button type="button" class="btn-search" onclick="searchAndScroll()"
			                onmouseover="this.style.backgroundColor='#4f46e5'" 
			                onmouseout="this.style.backgroundColor='#6366f1'"
			                style="height: 42px; padding: 0 24px; background-color: #6366f1; color: #ffffff; border: none; border-radius: 8px; font-weight: bold; font-size: 14px; cursor: pointer; white-space: nowrap; font-family: -apple-system, BlinkMacSystemFont, sans-serif; transition: background-color 0.15s ease;">
			            検索して移動
			        </button>
			        
			    </div>
		</div>	
		<div class="table-wrapper">
			<table>
				<thead>
					<tr>
						<th>社員番号</th>
						<th>名前</th>
						<th>権限レベルの変更</th>
						<th>システム権限</th>
						<th>人事管理（権限委任／退職／委任変更／委任解除）</th>
						<th>部署</th>
					</tr>
				</thead>
				<tbody>
					
					<%
						// 페이징 연산 데이터 미리 세팅
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
						if (reserveList != null) {
							for (EmployeeDTO emp : reserveList) {
								if (emp != null && !"Y".equals(emp.getRetired())) {
									totalCount++;
								}
							}
						}

						int totalPages = (int) Math.ceil((double) totalCount / pageSize);
						if (totalPages == 0) { totalPages = 1; }
						if (currentPage > totalPages) { currentPage = totalPages; }

						// 현재 페이지에 노출할 인덱스 범위 계산용 변수 추가
						int printCount = 0;
						int skipCount = 0;
						int targetStartIndex = (currentPage - 1) * pageSize;

						if (reserveList != null && !reserveList.isEmpty() && totalCount > 0) {
							for (EmployeeDTO emp : reserveList) {
								if (emp == null) continue;
								
								boolean isRetired = "Y".equals(emp.getRetired()); 
								if (isRetired) continue; 
								
								// [추가] 현재 페이지 이전의 데이터들은 건너뛰기(Skip) 처리
								if (skipCount < targetStartIndex) {
									skipCount++;
									continue;
								}
								
								// [추가] 페이지당 보여줄 개수(10개)를 다 채웠으면 반복문 종료
								if (printCount >= pageSize) {
									break;
								}
								printCount++;
					%>
					        	<tr <%= isRetired ? "style='opacity: 0.6; background-color: #f8f9fa;'" : "" %>> 					        
					            <td style="color: #6c757d;"><%= emp.getEmpNo() %></td>
					            
					            <td style="font-weight: 600; color: <%= isRetired ? "#adb5bd" : "#343a40" %>;">
					                <%= emp.getEmpName() %>
					            </td>
					
					            <td>
					                <% if (isRetired) { %>
					                    <span style="color: #dc3545; font-weight: bold; font-size: 13px;">退職処理済み</span>
					                <% } else { %>
					                    <form action="adminAction.do" method="post" style="margin: 0; display: flex; justify-content: center; gap: 5px; align-items: center;" onsubmit="return confirm('<%= emp.getEmpName() %> 社員の職位を修正しますか？');">
					                        <input type="hidden" name="action" value="updateLevel">
					                        <input type="hidden" name="empNo" value="<%= emp.getEmpNo() %>">
					                        <select name="newLevel">
											    <% 
											        int currentLevel = emp.getEmpLevel(); 
											        for(int i = 1; i <= 5; i++) {
											            if (i >= currentLevel) {
											    %>
											                <option value="<%= i %>" <%= currentLevel == i ? "selected" : "" %>>
											                    <%= i %>段階<%= (i==1?"（一般）": i==4?"（部署長）": i==5?"（役員）":"") %>
											                </option>
											    <% 
											            }
											        } 
											    %>
											</select>
					                        <button type="submit" class="btn-action btn-update">修正</button>
					                    </form>
					                <% } %>
					            </td>
					
					            <td>
					                <% if (isRetired) { %>
					                    <span class="badge-retired" style="color: #999;">アクセス不可</span>
					                <% } else if (emp.getEmpLevel() == 5) { %>
					                    <span class="badge-manager" style="color: #007bff; font-weight: bold;">最高管理者</span>
					                <% } else if ("Y".equals(emp.getManager())) { %>
				                    <span class="badge-manager" style="color: #007bff; font-weight: bold;">中間管理者</span>
				                	<% } else { %>
					                    <span class="badge-normal">一般社員</span>
					                <% } %>
					            </td>
								<td>
								    <% 
								        if (isRetired) { 
								    %>
								        <span style="color: #adb5bd; font-size: 13px;">-</span>
								    <% 
								        } else if (loginEmp != null && emp.getEmpNo() == loginEmp.getEmpNo()) { 
								    %>
								        <span style="color: #007bff; font-size: 12px; font-weight: bold;">本人（マスター）</span>
								    <% 
								        } else { 
								            boolean isCEO = (emp.getEmpLevel() == 5);
								            boolean isManager = "Y".equals(emp.getManager());
								    %>
								        <div style="display: flex; gap: 5px; justify-content: center; align-items: center;">
								            
								            <% if (!isCEO) { %>
								                
								                <% if (!isManager) { %>
								                    <form action="adminAction.do" method="post" style="display: inline;" >
								                        <input type="hidden" name="action" value="transferManager">
								                        <input type="hidden" name="empNo" value="<%= emp.getEmpNo() %>">
								                        <button type="submit" class="btn-action btn-transfer" 
								                                onclick="return confirm('この社員に管理者権限を付与しますか？');">委任</button>
								                    </form>
								                    
									                <form action="adminAction.do" method="post" style="display: inline;">
									                    <input type="hidden" name="action" value="deleteEmp">
									                    <input type="hidden" name="empNo" value="<%= emp.getEmpNo() %>">
									                    <button type="submit" class="btn-action btn-delete" 
									                            onclick="return confirm('該当の社員を退職処理しますか？');">退職</button>
									                </form>
								                
								                <% } else { %>
								                    <%-- 위임취소 주석 유지 --%>
								                <% } %>
								                
								            <% } else { %>
								                <span style="color: #dc3545; font-size: 12px; font-weight: bold;">修正不可（代表）</span>
								            <% } %>
								            
								        </div>
								    <% } %>
								</td>
								<td>
								    <% if (isRetired) { %>
								        <span style="color: #dc3545; font-weight: bold; font-size: 13px;">退職処理済み</span>
								    <% } else { %>
								        <form action="adminAction.do" method="post" style="margin: 0; display: flex; justify-content: center; gap: 5px; align-items: center;" onsubmit="return confirm('<%= emp.getEmpName() %> 社員の部署を修正しますか？');">
								            <input type="hidden" name="action" value="updateDept">
								            <input type="hidden" name="empNo" value="<%= emp.getEmpNo() %>">
								            
								            <select name="newDept" style="padding: 4px; border: 1px solid #ced4da; border-radius: 4px;">
								                <% 
								                    String currentDept = emp.getDept(); 
								                %>
								                <option value="経営支援チーム" <%= "経営支援チーム".equals(currentDept) ? "selected" : "" %>>経営支援チーム</option>
												<option value="企画チーム"     <%= "企画チーム".equals(currentDept) ? "selected" : "" %>>企画チーム</option>
												<option value="財務チーム"     <%= "財務チーム".equals(currentDept) ? "selected" : "" %>>財務チーム</option>
												<option value="営業チーム"     <%= "営業チーム".equals(currentDept) ? "selected" : "" %>>営業チーム</option>
												<option value="開発チーム"     <%= "開発チーム".equals(currentDept) ? "selected" : "" %>>開発チーム</option>
								            </select>
								            <button type="submit" class="btn-action btn-update">修正</button>
								        </form>
								    <% } %>
								</td>
					        </tr>
					<% 
					            } // for end
					        } else { 
					%>
					        <tr>
					            <td colspan="6" style="text-align: center; padding: 20px;">登録された社員がいません。</td>
					        </tr>
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

		<br>
		<hr>
		<br>
		<div class="insert-box"
			style="background-color: #ffffff; padding: 25px; border-radius: 6px; margin-bottom: 30px; border: 1px solid #e9ecef; border-left: 4px solid #343a40;">
			<h3 style="margin-top: 0;">新入社員の事前登録（初期設定）</h3>
			<form action="insertEmp.do" method="post"
				style="display: flex; gap: 10px; align-items: center;">

				<input type="number" name="empNo" placeholder="社員番号（数字" required
					style="padding: 10px; border: 1px solid #ced4da; border-radius: 4px; width: 120px;">

				<input type="text" name="empName" placeholder="社員の氏名" required
					style="padding: 10px; border: 1px solid #ced4da; border-radius: 4px; flex: 1;">

				<select name="empLevel" required
					style="padding: 10px; border: 1px solid #ced4da; border-radius: 4px;">
					<option value="1">1段階（一般社員）</option>
					<option value="2">2段階（代理）</option>
					<option value="3">3段階（課長）</option>
					<option value="4">4段階（次長）</option>
					<option value="5">5段階（代表・役員</option>
				</select>

				<select name="manager" required
					style="padding: 10px; border: 1px solid #ced4da; border-radius: 4px;">
					<option value="N">一般権限 (N)</option>
					<option value="Y">管理者権限 (Y)</option>
				</select>

				<select name="dept" style="padding: 10px; border: 1px solid #ced4da; border-radius: 4px;">
		                <option value="経営支援チーム">経営支援チーム</option>
						<option value="企画チーム">企画チーム</option>
						<option value="財務チーム">財務チーム</option>
						<option value="営業チーム">営業チーム</option>
						<option value="開発チーム">開発チーム</option>
		        </select> 

				<button type="submit"
					style="padding: 10px 20px; background-color: #343a40; color: white; border: none; border-radius: 4px; font-weight: bold; cursor: pointer;">社員登録</button>
			</form>
			<p style="margin: 10px 0 0 0; font-size: 12px; color: #6c757d;">登録後、該当の社員が直接会員登録メニューから社員番号を認証し、パスワードを設定する必要があります。</p>
		</div>

	</div>
</body>
</html>
