package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import com.groupware.dto.ReservationDTO;

@Controller
@RequestMapping("/holdReserve.do")
public class HoldReserveController {
    @Autowired private com.groupware.service.ReservationService reservations;

    private static final long serialVersionUID = 1L;

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String roomId = request.getParameter("roomId");
        int empNo = ((com.groupware.dto.EmployeeDTO) request.getSession().getAttribute("loginEmp")).getEmpNo();
        Date resDate = Date.valueOf(request.getParameter("resDate"));
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate reserveDate = resDate.toLocalDate();
        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int currentHour = java.time.LocalTime.now().getHour();

        if (reserveDate.isEqual(today) && startHour <= currentHour) {
            response.setContentType("application/json; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{\"result\":\"past_time\"}");
            out.flush();
            out.close();
            return; // 검증에 실패했으므로 여기서 컨트롤러 실행을 즉시 종료합니다.
        }

        ReservationDTO dto = new ReservationDTO();
        dto.setRoomId(roomId);
        dto.setEmpNo(empNo);
        dto.setResDate(resDate);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);

        int newResNo = reservations.hold(dto);

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        if (newResNo > 0) {
            out.print("{\"result\":\"success\", \"resNo\":" + newResNo + "}");
        } else {
            out.print("{\"result\":\"fail\"}");
        }
        
        out.flush();
        out.close();
    }
}
