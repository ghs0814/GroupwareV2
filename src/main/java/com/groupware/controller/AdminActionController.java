package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.groupware.dao.EmployeeDAO;
import com.groupware.dao.EquipmentDAO;
import com.groupware.dto.EquipmentDTO;
import com.groupware.dto.EmployeeDTO;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.*;

@Controller
@RequestMapping("/adminAction.do")
public class AdminActionController {
    @Autowired
    private com.groupware.dao.EmployeeDAO employeeDAO;

    private static final long serialVersionUID = 1L;

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
        if (loginEmp == null || !"Y".equals(loginEmp.getManager())) {
            out.println("<script>alert('管理者ではありません。'); location.href='index.jsp';</script>");
            return;
        }
        
        String action = request.getParameter("action");
        int targetEmpNo = Integer.parseInt(request.getParameter("empNo"));
        EmployeeDAO dao = employeeDAO;
        List<EmployeeDTO> dto = dao.getAllEmployees();
        boolean isSuccess = false;
        int currentManagerCount = dto.get(0).getCount_manager();// DB에서 실시간 관리자 수 조회
        
        out.println("<script>");

        try {
        	if ("updateLevel".equals(action)) {
                int newLevel = Integer.parseInt(request.getParameter("newLevel"));
                isSuccess = dao.updateEmployeeLevel(targetEmpNo, newLevel);
                
                if (isSuccess) {
                    out.println("alert('職位が正常に変更されました。');");
                    out.println("location.href='admin.do';");
                }
                
            } else if ("transferManager".equals(action)) {
              isSuccess = dao.transferManagerRole(targetEmpNo);
              
              if (isSuccess) {
            	 System.out.println(loginEmp.getEmpLevel());
            	if(loginEmp.getEmpLevel() == 5)
              	{
              		
              		out.println("alert('職位が正常に変更されました。');");
                    out.println("location.href='admin.do';");
              	}else
              	{
                      session.invalidate();
                      out.println("alert('管理者権限の譲渡が完了しました。一般社員権限に変更されるため、ログアウトします。');");
                      out.println("location.href='index.jsp';");
              	}
              }
                
            } else if ("deleteEmp".equals(action)) {
                isSuccess = dao.deleteEmployee(targetEmpNo);
                
                if (isSuccess) {
                    out.println("alert('該当社員の退職（削除）処理が完了しました。');");
                    out.println("location.href='admin.do';");
                }
            } else if("updateDept".equals(action))
            {
            	String newDept = request.getParameter("newDept");
            	isSuccess = dao.updateEmployeeDept(targetEmpNo, newDept);
                
                if (isSuccess) {
                    out.println("alert('職位が正常に変更されました。');");
                    out.println("location.href='admin.do';");
                }

            }

            if (!isSuccess) {
                out.println("alert('要求された処理に失敗しました。');");
                out.println("history.back();");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("alert('サーバーエラーが発生しました。');");
            out.println("history.back();");
        }

        out.println("</script>");
        out.flush();
        out.close();
    }
}
