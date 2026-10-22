package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.groupware.dao.ReservationDAO;

@Controller
@RequestMapping("/checkReservedTime.do")
public class CheckReservedTimeController {
    @Autowired
    private com.groupware.dao.ReservationDAO reservationDAO;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String roomId = request.getParameter("roomId");
    	String resDate = request.getParameter("resDate");
    	ReservationDAO dao = reservationDAO;
    	List<String> reservedTimes = dao.getReservedTimes(roomId, resDate);
    	StringBuilder json = new StringBuilder("[");
    	for (int i = 0; i < reservedTimes.size(); i++) {
    	    json.append("\"").append(reservedTimes.get(i)).append("\"");
    	    if (i < reservedTimes.size() - 1) json.append(",");
    	}
    	json.append("]");
    	response.setContentType("application/json; charset=UTF-8");
    	PrintWriter out = response.getWriter();
    	out.print(json.toString());
    	out.flush();
    	out.close();
    }
}
