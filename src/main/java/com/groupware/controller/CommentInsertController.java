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
@RequestMapping("/commentInsert.do")
public class CommentInsertController {
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

        int boardNo = Integer.parseInt(request.getParameter("boardNo"));
        String content = request.getParameter("content");
        String parentNoStr = request.getParameter("parentNo");
        if (content == null || content.isBlank() || content.length() > 1000) {
            throw new IllegalArgumentException("댓글을 1~1000자로 입력해주세요.");
        }
        
        CommentDTO comment = new CommentDTO();
        comment.setBoardNo(boardNo);
        comment.setEmpNo(loginEmp.getEmpNo());
        comment.setContent(content);
        
        if (parentNoStr != null && !parentNoStr.isEmpty()) {
            comment.setParentNo(Integer.parseInt(parentNoStr));
            CommentDTO parent = commentDAO.getComment(comment.getParentNo());
            if (parent == null || parent.getBoardNo() != boardNo) {
                throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다.");
            }
        }

        CommentDAO dao = commentDAO;
        if (!dao.insertComment(comment)) throw new IllegalArgumentException("댓글 저장에 실패했습니다.");
        
        response.sendRedirect("boardDetail.do?boardNo=" + boardNo);
    }
}
