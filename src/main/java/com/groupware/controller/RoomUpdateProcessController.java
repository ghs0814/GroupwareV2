package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.groupware.dao.RoomDAO;
import com.groupware.dto.RoomDTO;

@Controller
@RequestMapping("/roomUpdateProcess.do")
public class RoomUpdateProcessController {
    @Autowired
    private com.groupware.dao.RoomDAO roomDAO;

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        RoomDTO dto = new RoomDTO();
        dto.setRoomId(request.getParameter("roomId"));
        dto.setRoomName(request.getParameter("roomName"));
        dto.setCapacity(Integer.parseInt(request.getParameter("capacity")));
        dto.setHasBeam(request.getParameter("hasBeam"));
        dto.setEnable(request.getParameter("enable"));
        dto.setDescription(request.getParameter("description"));

        RoomDAO dao = roomDAO;
        boolean isSuccess = dao.updateRoom(dto);

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<script>");
        if(isSuccess) {
            out.println("alert('会議室情報が修正されました。');");
            out.println("location.href='officeMap.do';"); // 요청하신 대로 officeMap으로 이동
        } else {
            out.println("alert('修正に失敗しました。');");
            out.println("history.back();");
        }
        out.println("</script>");
    }
}
