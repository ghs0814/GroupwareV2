package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.groupware.dao.BoardDAO;
import com.groupware.dao.CommentDAO;
import com.groupware.dto.BoardDTO;
import com.groupware.dto.BoardFileDTO;
import com.groupware.dto.CommentDTO;

@Controller
@RequestMapping("/boardDetail.do")
public class BoardDetailController {
    @Autowired
    private com.groupware.dao.BoardDAO boardDAO;
    @Autowired
    private com.groupware.dao.CommentDAO commentDAO;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int boardNo = Integer.parseInt(request.getParameter("boardNo"));
        
        BoardDAO dao = boardDAO;
        dao.increaseHit(boardNo);
        BoardDTO board = dao.getBoardDetail(boardNo);
        List<BoardFileDTO> files = dao.getFilesByBoardNo(boardNo);
        CommentDAO commentDao = commentDAO;
        List<CommentDTO> commentList = commentDao.getCommentsByBoardNo(boardNo);
        
        request.setAttribute("board", board);
        request.setAttribute("fileList", files);
        request.setAttribute("commentList", commentList);
        
        request.getRequestDispatcher("/WEB-INF/views/boardDetail.jsp").forward(request, response);
    }
}
