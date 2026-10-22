package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/reserve.do")
public class ReserveController {
    @Autowired
    private com.groupware.dao.RoomDAO roomDAO;

	private static final long serialVersionUID = 1L;

	@GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		HttpSession session = request.getSession();
		if (session.getAttribute("loginEmp") == null) {
			response.sendRedirect("index.jsp");
			return;
		}
		String roomId = request.getParameter("roomId");
		com.groupware.dao.RoomDAO roomDao = roomDAO;
		com.groupware.dto.RoomDTO roomInfo = roomDao.getRoomDetail(roomId);
		if (roomInfo == null) {
			System.out.println("해당 방 정보가 DB에 존재하지 않습니다: " + roomId);
			response.sendRedirect("main.jsp");
			return;
		}
		request.setAttribute("roomInfo", roomInfo);

		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/reserve.jsp");
		dispatcher.forward(request, response);
	}

	@PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}
