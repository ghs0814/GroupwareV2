<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.groupware.dto.EmployeeDTO"%>
<%@ page import="com.groupware.dto.RentalHistoryDTO"%>
<%
    // 1. セッションログイン状態チェック
    EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
    if (loginEmp == null) {
        response.sendRedirect("index.jsp");
        return;
    }

    // 2. コントローラーから渡された詳細申請データの受け取り
    RentalHistoryDTO detail = (RentalHistoryDTO) request.getAttribute("detail");
    if (detail == null) {
%>
    <script>
        alert('存在しないか、または削除された申請文書です。');
        location.href = 'documentList.do';
    </script>
<%
        return;
    }

    // 📌 [ビジネスロジック]: 起案者が退職者(Lv.0)かつ「承認待ち」状態の場合、画面上で強制的に「差し戻し」に切り替え
    boolean isRetiredCreator = (detail.getEmpLevel() == 0);
    String currentStatus = detail.getStatus();
    
    if (isRetiredCreator && "승인대기".equals(currentStatus)) {
        currentStatus = "반려됨"; // 내부 로직 연동을 위해 한글 조건 분기 유지 후 currentStatus 업데이트
    }

    // 3. リアルタイム決裁承認/差し戻しボタンの露出権限検証 (切り替えられた currentStatus を基準にガードが作動)
    int currentStep = detail.getApprovalStep();
    boolean isApprover = "Y".equals(loginEmp.getManager()) && (loginEmp.getManagerLevel() == currentStep);
    boolean isPending = "승인대기".equals(detail.getStatus()); // 원본 상태 체크용
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<script src="<%=request.getContextPath()%>/js/common.js" data-token="${sessionScope.csrfToken}"></script>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>電子決裁 - 備品貸出申請詳細</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css?v=1.6">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/rentalDetail.css">
</head>
<body>

    <div class="detail-container">
        
        <div class="detail-header-row">
            <h2 class="detail-title"><%= detail.getTitle() != null ? detail.getTitle() : "タイトルなし" %></h2>
            <div>
                <% 
                    String badgeClass = "status-wait";
                    String displayStatusText = currentStatus; // 出力用テキスト変数の分離
                    
                    if ("승인대기".equals(currentStatus)) {
                        badgeClass = "status-wait";
                        displayStatusText = "承認待ち";
                    } else if ("대여중".equals(currentStatus)) {
                        badgeClass = "status-active";
                        displayStatusText = "貸出中";
                    } else if ("반려됨".equals(currentStatus)) {
                        badgeClass = "status-reject";
                        displayStatusText = "差し戻し";
                    } else if ("미반납".equals(currentStatus)) {
                        badgeClass = "status-finish";
                        displayStatusText = "未返却";
                    } else if ("반납완료".equals(currentStatus)) {
                        badgeClass = "status-finish";
                        displayStatusText = "返却完了";
                    }
                %>
                <span class="status-badge <%= badgeClass %>"><%= displayStatusText %></span>
            </div>
        </div>

         <div class="approval-container">
            <% 
                int creatorLevel = detail.getEmpLevel(); 
                int currentLoginLevel = loginEmp.getEmpLevel();
                int docProgressStep = detail.getApprovalStep();
        
                for (int i = 1; i <= 5; i++) { 
                    String signName = null;
                    java.sql.Date signDate = null;
                    
                    if (i == 1) { signName = detail.getSign1(); signDate = detail.getSign1Date(); }
                    else if (i == 2) { signName = detail.getSign2(); signDate = detail.getSign2Date(); }
                    else if (i == 3) { signName = detail.getSign3(); signDate = detail.getSign3Date(); }
                    else if (i == 4) { signName = detail.getSign4(); signDate = detail.getSign4Date(); }
                    else if (i == 5) { signName = detail.getSign5(); signDate = detail.getSign5Date(); }
        
                    if (i < creatorLevel) {
                        continue;
                    }
        
                    String stepTitle = (i == creatorLevel) ? "起案者" : i + "次決裁欄";
                    
                    // 문서 상태가 완벽하게 '승인대기'이고, 내 결재 차례일 때만 하이라이트 클래스(active-turn)를 부여
                    boolean isMyTurn = (isPending && i == docProgressStep && i == currentLoginLevel);
                    String activeClass = isMyTurn ? "active-turn" : "";
                    boolean isRejectedStamp = (signName != null && (signName.contains("반려") || signName.contains("差し戻し")));
                    
                    String displaySignName = "";
                    if (signName != null) {
                        if (isRejectedStamp) {
                            String pureName = signName.replaceAll("\\(반려\\)", "").replaceAll("\\(差し戻し\\)", "").trim();
                            displaySignName = pureName + " (差戻)"; 
                        } else {
                            displaySignName = signName;
                        }
                    }
                    
                    // 退職者が起案者(creatorLevel == 0)이고 현재 기안자 칸(i == 1)이라면 서명 도장 스타일을 입히지 않음
                    String stampClass = "";
                    if (signName != null && !isRejectedStamp && !(creatorLevel == 0 && i == 1)) {
                        stampClass = "signed";
                    }
            %>
                <div class="stamp-box <%= activeClass %>">
                <div class="stamp-step"><%= stepTitle %></div>
                
                <%-- [決裁欄名前出力部]: 退職者の場合は印鑑なしでテキストのみ露出 --%>
                <div class="stamp-name <%= stampClass %>" style="<%= isRejectedStamp ? "color: #ef4444;" : "" %>">
                    <%
                        if (creatorLevel == 0 && i == 1) {
                    %>
                        <span style="font-weight: 600; color: #64748b;">退職者</span>
                    <%
                        } else {
                    %>
                        <%= displaySignName %>
                    <%
                        }
                    %>
                </div>
     
                <%-- [決裁欄日付出力部]: 退職者申請の最初の欄(i == 1)の時は日付の代わりにハイプン('-')表示 --%>
                <div class="stamp-date">
                    <%
                        if (creatorLevel == 0 && i == 1) {
                    %>
                        -
                    <%
                        } else {
                    %>
                        <%= signDate != null ? signDate.toString() : "-" %>
                    <%
                        }
                    %>
                </div>
            </div>
        <% 
                } 
            %>
        </div>

        <table class="info-table">
            <colgroup>
                <col style="width: 20%;">
                <col style="width: 30%;">
                <col style="width: 20%;">
                <col style="width: 30%;">
            </colgroup>
            
            <tr>
                <th>文書番号</th>
                <td><%= detail.getRentalNo() %></td>
                <th>起案社情報</th>
                <td>
                    <%
                        if (detail.getEmpLevel() == 0) { 
                    %>
                        <b style="color: #94a3b8;">退職者</b>
                    <% 
                        } else { 
                    %>
                        <b><%= detail.getEmpName() != null ? detail.getEmpName() : "不明" %></b> 
                        <span style="font-size: 13px; color: #64748b; font-weight: 500;">(職位レベル: <%= detail.getEmpLevel() %>)</span>
                    <% 
                        } 
                    %>
                </td>
            </tr>
            <tr>
                <th>申請備品名</th>
                <td class="emphasize-text"><%= detail.getEqName() != null ? detail.getEqName() : "未指定の備品" %></td>
                <th>申請数量</th>
                <td><b><%= detail.getReqCount() %></b> EA</td>
            </tr>
            <tr>
                <th>貸出開始日</th>
                <td><%= detail.getRentalDate() %></td>
                <th>返却予定日</th>
                <td><%= detail.getReturnDate() %></td>
            </tr>
            <tr>
                <th>現在の決裁段階</th>
                <td>
                    <% 
                        int appStep = detail.getApprovalStep();
                        if ("반려됨".equals(currentStatus)) { 
                            if (isRetiredCreator) {
                    %>
                                <b style="color: #ef4444;">退職により差し戻し</b>
                    <% 
                            } else { 
                    %>
                                <b style="color: #ef4444;"><%= appStep %>次決裁にて差し戻し</b>
                    <% 
                            }
                        } else if (appStep > 5 || "반납완료".equals(currentStatus) || "대여중".equals(currentStatus)) { 
                    %>
                            <b style="color: #16a34a;">最終承認完了</b>
                    <% 
                        } else { 
                    %>
                            <b class="emphasize-text"><%= appStep %>次</b> 決裁待ち
                    <% 
                        } 
                    %>
                </td>
                <th>備品マスタ在庫</th>
                <td>残り数量: <%= detail.getRemainCount() %> EA / 総保有数: <%= detail.getTotalCount() %> EA</td>
            </tr>
             <tr>
                <th>貸出理由</th>
                <td colspan="3" style="text-align: left; padding: 15px; line-height: 1.6; background: #fafafa; white-space: pre-wrap;"><%= detail.getContent() != null ? detail.getContent() : "記入された理由がありません。" %></td>
            </tr>
        </table>

        <div class="btn-group">
            <a href="documentList.do?tab=equipment" class="btn btn-back">一覧に戻る</a>
            
            <% if (isPending && currentStep == loginEmp.getEmpLevel()) { %>
                <form action="processApproval.do" method="post" style="display: inline;" onsubmit="return confirm('この申請書を最終承認しますか？');">
                    <input type="hidden" name="rentalNo" value="<%= detail.getRentalNo() %>">
                    <input type="hidden" name="eqNo" value="<%= detail.getEqNo() %>">
                    <input type="hidden" name="step" value="<%= currentStep %>">
                    <input type="hidden" name="isApprove" value="true">
                    <button type="submit" class="btn btn-approve">決裁承認</button>
                </form>
                
                <form action="processApproval.do" method="post" style="display: inline;" onsubmit="return confirm('この申請を差し戻しますか？\n先占された備品の在庫は即時還元されます。');">
                    <input type="hidden" name="rentalNo" value="<%= detail.getRentalNo() %>">
                    <input type="hidden" name="eqNo" value="<%= detail.getEqNo() %>">
                    <input type="hidden" name="step" value="<%= currentStep %>">
                    <input type="hidden" name="isApprove" value="false">
                    <button type="submit" class="btn btn-reject">決裁差戻</button>
                </form>
            <% } %>
        </div>
        
    </div>

</body>
</html>
