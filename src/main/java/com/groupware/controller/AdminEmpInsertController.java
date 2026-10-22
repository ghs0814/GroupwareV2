package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.groupware.dao.EmployeeDAO;
import com.groupware.dao.EquipmentDAO;
import com.groupware.dto.EmployeeDTO;
import com.groupware.dto.EquipmentDTO;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/insertEmp.do")
public class AdminEmpInsertController {
    @Autowired
    private com.groupware.dao.EmployeeDAO employeeDAO;

    private static final long serialVersionUID = 1L;

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        int empNo = Integer.parseInt(request.getParameter("empNo"));
        String empName = request.getParameter("empName").trim();
        int empLevel = Integer.parseInt(request.getParameter("empLevel"));
        String manager = request.getParameter("manager").trim();
        String dept = request.getParameter("dept").trim();
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmpNo(empNo);
        dto.setEmpName(empName);
        dto.setEmpLevel(empLevel);
        dto.setManager(manager);
        dto.setDept(dept);
        EmployeeDAO dao = employeeDAO;
        boolean isSuccess = dao.insertEmployee(dto);

        PrintWriter out = response.getWriter();
        out.println("<script>");
        if (isSuccess) {
            out.println("alert('新規社員情報がシステムに事前登録されました。');");
            out.println("location.href='admin.do';"); // 사원 관리 목록 페이지로 리다이렉트
        } else {
            out.println("alert('登録に失敗しました。既に存在する社員番号か確認してください。');");
            out.println("history.back();");
        }
        out.println("</script>");
        out.flush();
        out.close();
    }
}
