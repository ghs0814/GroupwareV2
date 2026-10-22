package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.groupware.dao.EmployeeDAO;

import com.groupware.dto.EmployeeDTO;

import com.groupware.dao.EquipmentDAO;
import com.groupware.dto.EquipmentDTO;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin.do")
public class AdminController {
    @Autowired
    private com.groupware.dao.EmployeeDAO employeeDAO;

    private static final long serialVersionUID = 1L;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
        if (loginEmp == null || !"Y".equals(loginEmp.getManager())) {
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().println("<script>alert('관리자만 접근할 수 있는 페이지입니다.'); location.href='index.jsp';</script>");
            return;
        }
        EmployeeDAO dao = employeeDAO;
        List<EmployeeDTO> empList = dao.getAllEmployees();
        request.setAttribute("empList", empList);
        request.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(request, response);
    }
}
