package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.groupware.dao.CommentDAO;
import com.groupware.dto.CommentDTO;
import com.groupware.dto.EmployeeDTO;

@Controller
@RequestMapping("/commentDelete.do")
public class CommentDeleteController {
    @Autowired
    private com.groupware.dao.CommentDAO commentDAO;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
        
        if (loginEmp == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        int commentNo = Integer.parseInt(request.getParameter("commentNo"));
        int boardNo = Integer.parseInt(request.getParameter("boardNo"));

        CommentDAO dao = commentDAO;
        CommentDTO comment = dao.getComment(commentNo);

        if (comment != null) {
            if (comment.getEmpNo() == loginEmp.getEmpNo() || "Y".equals(loginEmp.getManager())) {
                dao.deleteComment(commentNo);
            }
        }
        
        response.sendRedirect("boardDetail.do?boardNo=" + boardNo);
    }
}
