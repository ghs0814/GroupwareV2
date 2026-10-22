package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.groupware.dao.RentalDAO;
import com.groupware.dao.LeaveDAO; 
import com.groupware.dto.RentalHistoryDTO;
import com.groupware.dto.LeaveHistoryDTO;
import com.groupware.dto.EmployeeDTO;

@Controller
@RequestMapping("/documentList.do")
public class DocumentListController {
    @Autowired
    private com.groupware.dao.LeaveDAO leaveDAO;
    @Autowired
    private com.groupware.dao.RentalDAO rentalDAO;

    private static final long serialVersionUID = 1L;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        
        HttpSession session = request.getSession();
        EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
        
        if (loginEmp == null) {
            PrintWriter out = response.getWriter();
            out.println("<script>alert('ログインが必要なサービスです。');location.href='index.jsp';</script>");
            out.close();
            return;
        }
        
        try {
            int empNo = loginEmp.getEmpNo();
            RentalDAO rentalDao = rentalDAO;
            LeaveDAO leaveDao = leaveDAO;
            int pageSize = 10;
            int pageBlock = 5;
            int eqCurrentPage = 1;
            String eqPageParam = request.getParameter("eqPage");
            if (eqPageParam != null && !eqPageParam.isEmpty()) {
                try { eqCurrentPage = Integer.parseInt(eqPageParam); } catch (NumberFormatException e) { eqCurrentPage = 1; }
            }
            int eqStartRow = (eqCurrentPage - 1) * pageSize + 1;
            int eqEndRow = eqCurrentPage * pageSize;
            
            List<RentalHistoryDTO> docList = rentalDao.getAllDocumentListPaging(eqStartRow, eqEndRow);
            int eqTotalCount = rentalDao.getTotalRentalCount();
            int eqTotalPages = (int) Math.ceil((double) eqTotalCount / pageSize);
            int eqStartPage = ((eqCurrentPage - 1) / pageBlock) * pageBlock + 1;
            int eqEndPage = Math.min(eqStartPage + pageBlock - 1, eqTotalPages);
            
            request.setAttribute("docList", docList);
            request.setAttribute("eqCurrentPage", eqCurrentPage);
            request.setAttribute("eqTotalPages", eqTotalPages);
            request.setAttribute("eqStartPage", eqStartPage);
            request.setAttribute("eqEndPage", eqEndPage);
            int leaveCurrentPage = 1;
            String leavePageParam = request.getParameter("leavePage");
            if (leavePageParam != null && !leavePageParam.isEmpty()) {
                try { leaveCurrentPage = Integer.parseInt(leavePageParam); } catch (NumberFormatException e) { leaveCurrentPage = 1; }
            }
            int leaveStartRow = (leaveCurrentPage - 1) * pageSize + 1;
            int leaveEndRow = leaveCurrentPage * pageSize;
            
            List<LeaveHistoryDTO> leaveList = leaveDao.getAllLeaveDocumentsPaging(leaveStartRow, leaveEndRow);
            int leaveTotalCount = leaveDao.getTotalLeaveCount();
            int leaveTotalPages = (int) Math.ceil((double) leaveTotalCount / pageSize);
            int leaveStartPage = ((leaveCurrentPage - 1) / pageBlock) * pageBlock + 1;
            int leaveEndPage = Math.min(leaveStartPage + pageBlock - 1, leaveTotalPages);
            
            request.setAttribute("leaveList", leaveList);
            request.setAttribute("leaveCurrentPage", leaveCurrentPage);
            request.setAttribute("leaveTotalPages", leaveTotalPages);
            request.setAttribute("leaveStartPage", leaveStartPage);
            request.setAttribute("leaveEndPage", leaveEndPage);
            
            String activeTab = request.getParameter("tab");
            request.setAttribute("activeTab", activeTab); 
            
            request.getRequestDispatcher("/WEB-INF/views/documentList.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            PrintWriter out = response.getWriter();
            out.println("<script>alert('フォルダを読み込んでいる途中でエラーが発生しました。');location.href='main.jsp';</script>");
            out.close();
        }
    }

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
