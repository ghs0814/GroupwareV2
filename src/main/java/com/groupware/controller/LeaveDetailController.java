package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import com.groupware.dao.LeaveDAO;
import com.groupware.dto.LeaveHistoryDTO;

@Controller
@RequestMapping("/leaveDetail.do")
public class LeaveDetailController {
    @Autowired
    private LeaveDAO dao;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("loginEmp") == null) {
            response.sendRedirect("index.jsp");
            return;
        }
        String leaveNoParam = request.getParameter("leaveNo");
        if (leaveNoParam == null) {
            response.sendRedirect("documentList.do?tab=leave");
            return;
        }

        int leaveNo = Integer.parseInt(leaveNoParam);
        LeaveHistoryDTO leave = dao.getLeaveDetail(leaveNo);
        if (leave != null) {
            request.setAttribute("leave", leave);
            request.setAttribute("loginUserLevel", ((com.groupware.dto.EmployeeDTO)session.getAttribute("loginEmp")).getManagerLevel());
            request.getRequestDispatcher("/WEB-INF/views/leaveDetail.jsp").forward(request, response);
        } else {
            response.sendRedirect("documentList.do?tab=leave");
        }
    }
}
