package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.groupware.dao.RoomDAO;
import com.groupware.dto.RoomDTO;

@Controller
@RequestMapping("/roomUpdate.do")
public class RoomUpdateController {
    @Autowired
    private com.groupware.dao.RoomDAO roomDAO;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String roomId = request.getParameter("roomId");
        
        RoomDAO dao = roomDAO;
        RoomDTO room = dao.getRoomDetail(roomId); // 기존 상세조회 메서드 활용
        
        request.setAttribute("room", room);
        request.getRequestDispatcher("/WEB-INF/views/roomUpdate.jsp").forward(request, response);
    }
}
