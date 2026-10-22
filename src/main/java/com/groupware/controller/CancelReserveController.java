package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



@Controller
@RequestMapping("/cancelReserve.do")
public class CancelReserveController {
    @Autowired private com.groupware.service.ReservationService reservations;

    private static final long serialVersionUID = 1L;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String resNoStr = request.getParameter("resNo");
        String from = request.getParameter("from"); 
        
        if (resNoStr == null || resNoStr.trim().isEmpty()) {
            response.sendRedirect("myPage.do");
            return;
        }
        
        int resNo = Integer.parseInt(resNoStr);

        boolean isSuccess = reservations.cancel(resNo, ((com.groupware.dto.EmployeeDTO) request.getSession().getAttribute("loginEmp")).getEmpNo());

        PrintWriter out = response.getWriter();
        out.println("<script>");
        if (isSuccess) {
            out.println("alert('予約が正常にキャンセルされました。');");
        } else {
            out.println("alert('予約のキャンセル処理に失敗しました。');");
        }

        if ("mypage".equals(from)) {
            out.println("location.href='myPage.do';"); 
        } else {
            out.println("location.href='main.do';"); 
        }
        
        out.println("</script>");
        out.flush();
        out.close();
    }
}
