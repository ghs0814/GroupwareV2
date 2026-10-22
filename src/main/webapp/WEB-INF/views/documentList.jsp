<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="com.groupware.dto.EmployeeDTO"%>
<%@ page import="com.groupware.dto.LeaveHistoryDTO"%>
<%@ page import="com.groupware.dto.RentalHistoryDTO"%>
<%
    // 1. セッションログイン状態チェック
    EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
    if (loginEmp == null) {
        response.sendRedirect("index.jsp");
        return;
    }

    // 2. コントローラー等から渡された起票一覧の受け取り
    List<LeaveHistoryDTO> leaveList = (List<LeaveHistoryDTO>) request.getAttribute("leaveList");
    List<RentalHistoryDTO> eqList = (List<RentalHistoryDTO>) request.getAttribute("docList");
    
    // コントローラーから渡されたアクティブタブ情報の確認
    String activeTab = (String) request.getAttribute("activeTab");
    boolean isEqTab = "equipment".equals(activeTab); // 詳細画面から戻ってきたかどうかの判定
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>社内システム - 稟議・申請文書一覧</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css?v=1.6">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/documentList.css?v=1.2">
</head>
<body>

<jsp:include page="header.jsp" />

    <div class="dashboard-container" style="padding-top: 20px; max-width: 100%; width: 100%; box-sizing: border-box;"> 
        
        <div class="headertitle" style="margin-bottom: 15px;">
            <h2 style="margin: 0; font-size: 22px; font-weight: 700; color: #1e293b;">統合申請文書一覧</h2>
        </div>
        
        <%-- 検索バー領域 --%>
        <div class="search-bar-container" style="width: 100%; max-width: 100%; box-sizing: border-box;">
            <div class="search-form">
                <select id="searchType" class="search-select" onchange="toggleSearchInput()">
                    <option value="all">全体検索</option>
                    <option value="title">申請タイトル</option>
                    <option value="empName">申請者 (備品)</option>
                    <option value="status">決裁ステータス</option>
                </select>
    
                <input type="text" id="keyword" class="search-input" placeholder="検索キーワードを入力した後、右側の検索ボタンを押すかエンターキーを押してください..." 
                       onkeydown="if(event.keyCode==13) { filterDocuments(); return false; }">
                
                <select id="statusSelect" class="search-select search-input" style="display: none; flex-grow: 1;" onchange="filterDocuments()">
                    <option value="">-- 決裁ステータスを選択してください --</option>
                    <option value="承認待ち">承認待ち</option>
                    <option value="貸出中">貸出中</option>
                    <option value="未返却">未返却</option>
                    <option value="返却完了">返却完了</option>
                    <option value="差し戻し">差し戻し</option>
                </select>
                
                <button type="button" class="btn-search" onclick="filterDocuments()">検索</button>
            </div>
        </div>
        
        <%-- タブメニュー領域 --%>
        <div class="tab-menu-container" style="width: 100%; max-width: 100%;">
            <button class="tab-btn <%= !isEqTab ? "active" : "" %>" onclick="switchTab(event, 'tab-leave')">休暇申請一覧</button>
            <button class="tab-btn <%= isEqTab ? "active" : "" %>" onclick="switchTab(event, 'tab-equipment')">備品貸出申請一覧</button>
        </div>

        <%-- タブ 1: 休暇申請コンテンツ --%>
        <div id="tab-leave" class="tab-content <%= !isEqTab ? "active" : "" %>" style="display: <%= !isEqTab ? "block" : "none" %>; width: 100%;">
            <div class="table-wrapper" style="width: 100% !important; max-width: 100% !important; box-sizing: border-box; background: #ffffff; border-radius: 8px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);">
                <table style="width: 100%; table-layout: fixed; border-collapse: collapse;">
				    <thead>
				        <tr>
				            <th style="width: 12%;">文書番号</th> 
				            <th style="width: 25%;">休暇期間</th>
				            <th style="width: 12%;">取得日数</th> 
				            <th style="width: 39%;">休暇理由</th> 
				            <th style="width: 12%;">決裁ステータス</th> 
				        </tr>
				    </thead>
				    <tbody>
				    <% 
				        if (leaveList == null || leaveList.isEmpty()) { 
				    %>
				        <tr><td colspan="5" class="empty-data" style="text-align:center; padding:20px;">申請された休暇の履歴がありません。</td></tr>
				    <% 
				        } else { 
				            for (LeaveHistoryDTO leave : leaveList) { 
				                String statusClass = "status-blue";
				                if ("差し戻し".equals(leave.getStatus()) || "반려됨".equals(leave.getStatus())) statusClass = "status-red";
				                else if ("承認完了".equals(leave.getStatus()) || "승인완료".equals(leave.getStatus())) statusClass = "status-gray";
				    %>
				        <tr data-doc-id="leave_<%= leave.getLeaveNo() %>">
				            <td><%= leave.getLeaveNo() %></td>
				            <td><%= leave.getStartDate() %> ~ <%= leave.getEndDate() %></td>
				            <td><b><%= leave.getUseDays() %>日</b></td>
				            
				            <td style="text-align: left; padding: 14px 20px; white-space: normal; word-break: break-all;">
    							<a href="javascript:void(0);" 
      							   onclick="goToDetail('leave', '<%= leave.getLeaveNo() %>')"
       							   class="title-link" 
                                   style="color: #6366f1; font-weight: 600; text-decoration: none; cursor: pointer; display: block; width: 100%;"
                                   onmouseover="this.style.textDecoration='underline'; this.style.color='#0284c7';"
                                    onmouseout="this.style.textDecoration='none'; this.style.color='#6366f1';">
        <%
            String reason = leave.getReason();
            if ("연차".equals(reason)) out.print("年次有給休暇");
            else if ("병가".equals(reason)) out.print("傷病休暇");
            else if ("경조사".equals(reason)) out.print("慶弔休暇");
            else out.print(reason != null ? reason : "理由なし");
        %>
    </a>
</td>
				            
				            <%-- 한국어 데이터 매핑을 고려하여 화면 노출만 일본어로 변경 --%>
				          <td>
    <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 5px; min-height: 42px;">
        <span class="status-badge <%= statusClass %>" style="display: inline-block; margin: 0;">
            <% 
                String lStatus = leave.getStatus();
                if("승인대기".equals(lStatus)) out.print("承認待ち");
                else if("승인완료".equals(lStatus)) out.print("承認完了");
                else if("반려됨".equals(lStatus)) out.print("差し戻し");
                else out.print(lStatus);
            %>
        </span>

        <%-- [추가] 휴가 신청 결재 차례일 때 문구 노출 --%>
        <% if ("승인대기".equals(leave.getStatus()) && (loginEmp.getEmpLevel() == leave.getApprovalStep())) { %>
            <div class="approval-blink" style="margin: 0 !important; display: inline-block !important;">
                決裁をお願いします
            </div>
        <% } %>
    </div>
</td>
				        </tr>
				    <% 
				            } 
				        } 
				    %>
				    </tbody>
				</table>
            </div>
            
            <%-- 休暇申請ページング --%>
            <%
                Integer leaveCurrentPage = (Integer) request.getAttribute("leaveCurrentPage");
                Integer leaveTotalPages = (Integer) request.getAttribute("leaveTotalPages");
                Integer leaveStartPage = (Integer) request.getAttribute("leaveStartPage");
                Integer leaveEndPage = (Integer) request.getAttribute("leaveEndPage");
                
                if (leaveTotalPages != null && leaveTotalPages > 1) {
            %>
            <div class="pagination-container" style="text-align: center; margin-top: 20px;">
                <div class="pagination">
                    <% if (leaveStartPage > 1) { %>
                        <a href="documentList.do?tab=leave&leavePage=<%= leaveStartPage - 1 %>&eqPage=${eqCurrentPage}" class="page-link">&laquo; 前へ</a>
                    <% } %>
                    <% for (int i = leaveStartPage; i <= leaveEndPage; i++) { %>
                        <a href="documentList.do?tab=leave&leavePage=<%= i %>&eqPage=${eqCurrentPage}" class="page-link <%= (i == leaveCurrentPage) ? "active" : "" %>"><%= i %></a>
                    <% } %>
                    <% if (leaveEndPage < leaveTotalPages) { %>
                        <a href="documentList.do?tab=leave&leavePage=<%= leaveEndPage + 1 %>&eqPage=${eqCurrentPage}" class="page-link">次へ &raquo;</a>
                    <% } %>
                </div>
            </div>
            <% } %>
        </div>

        <%-- タブ 2: 備品貸出申請コンテンツ --%>
        <div id="tab-equipment" class="tab-content" style="display: none; width: 100%;">
            <div class="table-wrapper" style="width: 100% !important; max-width: 100% !important; box-sizing: border-box; background: #ffffff; border-radius: 8px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);">
                <table class="eq-table" style="width: 100%; table-layout: fixed; border-collapse: collapse;">
                    <thead>
                        <tr>
                            <th style="width: 12%;">申請番号</th>
                            <th style="width: 34%;">申請タイトル</th>
                            <th style="width: 15%;">申請者</th> 
                            <th style="width: 12%;">貸出数量</th>
                            <th style="width: 15%;">貸出期間</th>
                            <th style="width: 12%;">決裁ステータス</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% 
                        if (eqList == null || eqList.isEmpty()) { 
                    %>
                        <tr><td colspan="6" class="empty-data">申請された備品貸出の履歴がありません。</td></tr>
                    <% 
                        } else { 
                            boolean hasVisibleData = false; 
                            int myLevel = loginEmp.getEmpLevel(); 
                            boolean isAdmin = "Y".equals(loginEmp.getManager());
                            for (RentalHistoryDTO eq : eqList) { 
                                int targetLevel = eq.getEmpLevel();
                                boolean isRetiredCreator = (targetLevel == 0);
                                boolean isMyDoc = (loginEmp.getEmpNo() == eq.getEmpNo());
                                String currentStatus = eq.getStatus();
                                if (isRetiredCreator && "승인대기".equals(currentStatus)) {
                                    currentStatus = "반려됨";
                                }
                                
                                boolean isPermitted = false;
                                if (isRetiredCreator) {
                                    if (isAdmin) isPermitted = true;
                                } else if ("반려됨".equals(currentStatus)) {
                                    if (isMyDoc || isAdmin) isPermitted = true;
                                } else {
                                    if (isAdmin || isMyDoc || myLevel >= targetLevel) isPermitted = true;
                                }
                                
                                if (!isPermitted) continue;
                                hasVisibleData = true; 
                                String statusClass = "status-blue";
                                
                                if ("반려됨".equals(currentStatus)) { 
                                    statusClass = "status-red";
                                } else if ("반납완료".equals(currentStatus) || "이용 종료".equals(currentStatus)) { 
                                    statusClass = "status-gray";
                                }
                                
                                boolean isMyApprovalTurn = "승인대기".equals(currentStatus) && (eq.getApprovalStep() == loginEmp.getEmpLevel());
                                String displayName = eq.getEmpName() != null ? eq.getEmpName() : "不明";
                    %>
                        <tr data-doc-id="rent_<%= eq.getRentalNo() %>">
                            <td><%= eq.getRentalNo() %></td>
                            
                            <td class="td-title" style="text-align: left; padding: 14px 20px; white-space: normal; word-break: break-all;">
                                <a href="rentalDetail.do?rentalNo=<%= eq.getRentalNo() %>" 
                                   class="title-link" 
                                   style="color: #6366f1; font-weight: 600; text-decoration: none; cursor: pointer; display: block; width: 100%;"
                                   onmouseover="this.style.textDecoration='underline'; this.style.color='#0284c7';"
                                   onmouseout="this.style.textDecoration='none'; this.style.color='#6366f1';">
                                     <%= eq.getTitle() != null ? eq.getTitle() : "タイトルなし" %>
                                </a>
                            </td>
                            
                            <td>
                                <% if (isRetiredCreator) { %>
                                    <b style="color: #64748b;"><%= displayName %></b>
                                    <span style="font-size: 11px; color: #94a3b8; font-weight: 500; margin-left: 2px;">(退職者)</span>
                                <% } else { %>
                                    <b><%= displayName %></b>
                                    <span style="font-size: 11px; color: #64748b; font-weight: 500; margin-left: 2px;">
                                        (Lv.<%= targetLevel %>)
                                    </span>
                                <% } %>
                            </td>
  
                            <td class="td-qty">
                                <span class="qty-wrap"><b><%= eq.getReqCount() %></b>&nbsp;EA</span>
                            </td>
                            
                            <td><%= eq.getRentalDate() %> ~ <%= eq.getReturnDate() %></td>
                   
                            <%-- 決裁バッジ列 --%>
                            <td>
                                <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 5px; min-height: 42px;">
                                    <span class="status-badge <%= statusClass %>" style="display: inline-block; margin: 0;">
                                        <% 
                                            if("승인대기".equals(currentStatus)) out.print("承認待ち");
                                            else if("대여중".equals(currentStatus)) out.print("貸出中");
                                            else if("미반납".equals(currentStatus)) out.print("未返却");
                                            else if("반납완료".equals(currentStatus)) out.print("返却完了");
                                            else if("반려됨".equals(currentStatus)) out.print("差し戻し");
                                            else out.print(currentStatus);
                                        %>
                                    </span>
                                    
                                    <% if (isMyApprovalTurn) { %>
                                        <div class="approval-blink" style="margin: 0 !important; display: inline-block !important;">
                                            決裁をお願いします
                                        </div>
                                    <% } %>
                                </div>
                            </td>
                        </tr>
                    <% 
                            } // for(eqList)
                            
                            if (!hasVisibleData) {
                    %>
                        <tr><td colspan="6" class="empty-data">閲覧可能な申請履歴がありません。</td></tr>
                    <% 
                            }
                        } // else
                    %>
                    </tbody>
                </table>
            </div>

            <%-- 備品貸出申請ページング --%>
            <%
                Integer eqCurrentPage = (Integer) request.getAttribute("eqCurrentPage");
                Integer eqTotalPages = (Integer) request.getAttribute("eqTotalPages");
                Integer eqStartPage = (Integer) request.getAttribute("eqStartPage");
                Integer eqEndPage = (Integer) request.getAttribute("eqEndPage");
                
                if (eqTotalPages != null && eqTotalPages > 1) {
            %>
            <div class="pagination-container" style="text-align: center; margin-top: 20px;">
                <div class="pagination">
                    <% if (eqStartPage > 1) { %>
                        <a href="documentList.do?tab=equipment&eqPage=<%= eqStartPage - 1 %>&leavePage=${leaveCurrentPage}" class="page-link">&laquo; 前へ</a>
                    <% } %>
                    <% for (int i = eqStartPage; i <= eqEndPage; i++) { %>
                        <a href="documentList.do?tab=equipment&eqPage=<%= i %>&leavePage=${leaveCurrentPage}" class="page-link <%= (i == eqCurrentPage) ? "active" : "" %>"><%= i %></a>
                    <% } %>
                    <% if (eqEndPage < eqTotalPages) { %>
                        <a href="documentList.do?tab=equipment&eqPage=<%= eqEndPage + 1 %>&leavePage=${leaveCurrentPage}" class="page-link">次へ &raquo;</a>
                    <% } %>
                </div>
            </div>
            <% } %>
        </div>
    </div>
        
    <style>
    /* ブリンクアニメーション */
    .approval-blink {
        font-size: 11px !important;
        color: #ef4444 !important;
        font-weight: 800 !important;
        margin-top: 5px !important;
        animation: alert-flash 0.8s infinite alternate ease-in-out;
    }
    @keyframes alert-flash {
        from { opacity: 0.3; transform: scale(0.98); }
        to { opacity: 1; transform: scale(1); }
    }
    .table-wrapper table tbody tr td a.title-link {
        display: block !important;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    </style>

    <script>
    let currentMatchIndex = -1;
    let matchRows = []; 
    let lastKeyword = ""; 
    
    function switchTab(event, tabId) {
        const contents = document.querySelectorAll('.tab-content');
        contents.forEach(content => { content.style.display = 'none'; content.classList.remove('active'); });
        
        const buttons = document.querySelectorAll('.tab-btn');
        buttons.forEach(btn => { btn.classList.remove('active'); });
        const targetContent = document.getElementById(tabId);
        if (targetContent) { 
            targetContent.style.display = 'block';
            targetContent.classList.add('active'); 
            
            const rows = targetContent.querySelectorAll('tbody tr');
            rows.forEach(row => { row.style.display = ""; });
        }
        if (event && event.currentTarget) event.currentTarget.classList.add('active');
        
        document.getElementById("searchType").value = "all";
        document.getElementById("keyword").value = "";
        document.getElementById("statusSelect").value = "";
        toggleSearchInput();
        
        currentMatchIndex = -1;
        matchRows = [];
        lastKeyword = "";
    }
    
    function toggleSearchInput() {
        const searchType = document.getElementById("searchType").value;
        const keywordInput = document.getElementById("keyword");
        const statusSelect = document.getElementById("statusSelect");
        
        if (searchType === "status") {
            keywordInput.style.display = "none";
            statusSelect.style.display = "block"; keywordInput.value = "";
        } else {
            keywordInput.style.display = "block";
            statusSelect.style.display = "none"; statusSelect.value = "";
        }
    }
    
    function filterDocuments() {
        const searchType = document.getElementById("searchType").value;
        let keyword = (searchType === "status") ? document.getElementById("statusSelect").value : document.getElementById("keyword").value;
        
        if (!keyword) keyword = "";
        keyword = keyword.trim().toLowerCase();
        if (keyword !== lastKeyword) {
            currentMatchIndex = -1;
            matchRows = [];
            lastKeyword = keyword;
        }
    
        const activeTab = document.querySelector('.tab-content.active');
        if (!activeTab) return;
        
        const rows = activeTab.querySelectorAll('tbody tr');
        
        if (matchRows.length === 0 && keyword !== "") {
            rows.forEach(row => {
                if (row.querySelector('.empty-data')) return; 
                
                const titleSpan = row.querySelector('.title-link');
                const bTag = row.querySelector('td b');
                const badge = row.querySelector('.status-badge');
                const cells = row.getElementsByTagName('td');
                
                const leaveReasonText = cells && cells[3] ? cells[3].innerText.replace(/\u00a0/g, " ").replace(/\s+/g, " ").toLowerCase().trim() : "";
                const titleText = titleSpan ? titleSpan.innerText.replace(/\u00a0/g, " ").replace(/\s+/g, " ").toLowerCase().trim() : "";
                const empNameText = bTag ? bTag.innerText.replace(/\u00a0/g, " ").replace(/\s+/g, " ").toLowerCase().trim() : "";
                const statusText = badge ? badge.innerText.replace(/\u00a0/g, " ").replace(/\s+/g, " ").toLowerCase().trim() : "";
                const fullText = row.innerText.replace(/\u00a0/g, " ").replace(/\s+/g, " ").toLowerCase();
                let isMatch = false;
                
                if (searchType === "all") {
                    isMatch = fullText.includes(keyword) || 
                              (fullText.includes("承認待ち") && "승인대기".includes(keyword)) ||
                              (fullText.includes("承認完了") && "승인완료".includes(keyword)) ||
                              (fullText.includes("差し戻し") && "반려됨".includes(keyword)) ||
                              (fullText.includes("貸出中") && "대여중".includes(keyword)) ||
                              (fullText.includes("未返却") && "미반납".includes(keyword)) ||
                              (fullText.includes("返却完了") && "반납완료".includes(keyword));
                } else if (searchType === "title") {
                    if (titleText.includes(keyword) || (titleText === "" && leaveReasonText.includes(keyword))) isMatch = true;
                } else if (searchType === "empName") {
                    if (empNameText.includes(keyword)) isMatch = true;
                } else if (searchType === "status") {
                    // 스크립트 필터링 동기화를 위해 한글/일어 상태값 매핑 처리
                    let mappedKeyword = keyword;
                    if(keyword === "승인대기") mappedKeyword = "承認待ち";
                    else if(keyword === "대여중") mappedKeyword = "貸出中";
                    else if(keyword === "미반납") mappedKeyword = "未返却";
                    else if(keyword === "반납완료") mappedKeyword = "返却完了";
                    else if(keyword === "반려됨") mappedKeyword = "差し戻し";

                    if (statusText.includes(mappedKeyword) || (fullText.includes("決裁をお願いします") && "決裁をお願いします".includes(keyword))) {
                        isMatch = true;
                    }
                }
                
                if (isMatch) {
                    matchRows.push(row);
                }
            });
        }
    
        rows.forEach(row => {
            if (row.querySelector('.empty-data')) return;
            
            if (keyword === "") {
                row.style.display = ""; 
            } else {
                if (matchRows.includes(row)) {
                    row.style.display = "";
                } else {
                    row.style.display = "none";
                }
            }
        });
    
        if (keyword !== "") {
            if (matchRows.length > 0) {
                currentMatchIndex = (currentMatchIndex + 1) % matchRows.length;
                setTimeout(() => {
                    matchRows[currentMatchIndex].scrollIntoView({
                        behavior: "smooth",
                        block: "center"
                    });
                }, 50);
                if (searchType !== "status") {
                    document.getElementById("keyword").select();
                }
            } else {
                alert("一致する申請履歴が見つかりません。");
                currentMatchIndex = -1;
                matchRows = [];
            }
        }
    }
    
    function goToDetail(type, no) {
        if (!no) {
            alert("無効な文書番号です。");
            return;
        }
        if (type === 'leave') {
            location.href = "leaveDetail.do?leaveNo=" + no;
        } else if (type === 'rental') {
            location.href = "rentalDetail.do?rentalNo=" + no;
        }
    }
    
    window.addEventListener('DOMContentLoaded', function() {
        const urlParams = new URLSearchParams(window.location.search);
        const tabParam = urlParams.get('tab');
        
        if (tabParam === 'equipment' || window.location.hash === '#equipment') {
            switchTab(null, 'tab-equipment');
            const eqTabBtn = document.querySelector('.tab-btn[onclick*="tab-equipment"]');
            if (eqTabBtn) eqTabBtn.classList.add('active');
        }
    });
    </script>
</body>
</html>
