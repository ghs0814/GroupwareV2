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
@RequestMapping("/commentUpdate.do")
public class CommentUpdateController {
    @Autowired
    private com.groupware.dao.CommentDAO commentDAO;

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
        
        if (loginEmp == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        int commentNo = Integer.parseInt(request.getParameter("commentNo"));
        int boardNo = Integer.parseInt(request.getParameter("boardNo"));
        String content = request.getParameter("content");

        CommentDAO dao = commentDAO;
        CommentDTO comment = dao.getComment(commentNo);

        if (comment != null) {
            if (comment.getEmpNo() == loginEmp.getEmpNo() || "Y".equals(loginEmp.getManager())) {
                dao.updateComment(commentNo, content);
            }
        }
        
        response.sendRedirect("boardDetail.do?boardNo=" + boardNo);
    }
}
