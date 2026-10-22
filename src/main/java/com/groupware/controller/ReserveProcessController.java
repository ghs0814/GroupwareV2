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
@RequestMapping("/reserveProcess.do")
public class ReserveProcessController {
    @Autowired private com.groupware.service.ReservationService reservations;

    private static final long serialVersionUID = 1L;

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        String resNoStr = request.getParameter("resNo");
        String purpose = request.getParameter("purpose");
        String roomId = request.getParameter("roomId"); // 실패 시 돌아갈 방 번호

        PrintWriter out = response.getWriter();
        out.println("<script>");

        if (resNoStr == null || resNoStr.isEmpty()) {
            out.println("alert('まず時間を選択してください。');");
            out.println("history.back();");
        } else {
            int resNo = Integer.parseInt(resNoStr);

            boolean isSuccess = reservations.confirm(resNo, purpose, ((com.groupware.dto.EmployeeDTO) request.getSession().getAttribute("loginEmp")).getEmpNo());

            if (isSuccess) {
                out.println("alert('会議室の予約が正常に確定しました。');");
                out.println("location.href='myPage.do';"); 
            } else {
                out.println("alert('予約確定時間が超過したか、すでにキャンセルされた予約です。 もう一度選択してください。');");
                out.println("location.href='reserve.do?roomId=" + roomId + "';");
            }
        }
        
        out.println("</script>");
        out.flush();
        out.close();
    }
}
