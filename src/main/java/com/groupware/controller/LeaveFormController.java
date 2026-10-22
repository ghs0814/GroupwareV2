package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.groupware.dto.EmployeeDTO;
import com.groupware.dto.LeaveHistoryDTO;


@Controller
@RequestMapping("/leaveForm.do")
public class LeaveFormController {
    @Autowired private com.groupware.service.WorkflowService workflow;

    private static final long serialVersionUID = 1L;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8"); 
        response.setContentType("text/html; charset=UTF-8");
        request.getRequestDispatcher("/WEB-INF/views/leaveForm.jsp").forward(request, response);
    }

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        HttpSession session = request.getSession();
        
        EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
        if (loginEmp == null) { response.sendRedirect("index.jsp"); return; }
        
        String start = request.getParameter("startDate");
        String end = request.getParameter("endDate");
        String reason = "직접입력".equals(request.getParameter("reasonCategory")) ? 
                        request.getParameter("reason") : request.getParameter("reasonCategory");

        int days = calculateBusinessDays(start, end);
        
        if (days <= 0) {
            sendAlert(response, "신청하신 기간 내에 평일이 없습니다.", "leaveForm.do");
        } else if (days > loginEmp.getCurLeave()) {
            sendAlert(response, "연차가 부족합니다.", "leaveForm.do");
        } else {

            LeaveHistoryDTO dto = new LeaveHistoryDTO();
            
            dto.setEmpNo(loginEmp.getEmpNo());
            dto.setStartDate(java.sql.Date.valueOf(start));
            dto.setEndDate(java.sql.Date.valueOf(end));
            dto.setUseDays(days);
            dto.setReason(reason);
            dto.setEmpLevel(loginEmp.getEmpLevel());
            int userLevel = loginEmp.getEmpLevel();
            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

            if (userLevel == 5) {
                dto.setStatus("승인완료");
                dto.setApprovalStep(6);
                dto.setSign5(loginEmp.getEmpName());
                dto.setSign5Date(today);
            } else {
                dto.setStatus("승인대기");
                dto.setApprovalStep(userLevel + 1);
                
                if (userLevel == 1) { dto.setSign1(loginEmp.getEmpName()); dto.setSign1Date(today); }
                else if (userLevel == 2) { dto.setSign2(loginEmp.getEmpName()); dto.setSign2Date(today); }
                else if (userLevel == 3) { dto.setSign3(loginEmp.getEmpName()); dto.setSign3Date(today); }
                else if (userLevel == 4) { dto.setSign4(loginEmp.getEmpName()); dto.setSign4Date(today); }
            }
            boolean result = workflow.createLeave(dto);
            
            if (result) {
                response.sendRedirect("documentList.do?tab=leave");
            } else {
                sendAlert(response, "휴가 신청 중 오류가 발생했습니다.", "leaveForm.do");
            }
        }
    }

    private int calculateBusinessDays(String start, String end) {
        LocalDate s = LocalDate.parse(start);
        LocalDate e = LocalDate.parse(end);
        return (int) Stream.iterate(s, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(s, e) + 1)
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
    }

    private void sendAlert(HttpServletResponse response, String msg, String url) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().println("<script>alert('" + msg + "'); location.href='" + url + "';</script>");
    }
}
