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
import com.groupware.dto.BoardDTO;

@Controller
@RequestMapping("/boardList.do")
public class BoardListController {
    @Autowired
    private com.groupware.dao.BoardDAO boardDAO;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String typeStr = request.getParameter("type");
        int type = (typeStr == null) ? 1 : Integer.parseInt(typeStr);
        
        String pageStr = request.getParameter("page");
        int page = (pageStr == null) ? 1 : Integer.parseInt(pageStr);
        int pageSize = 10;
        
        String searchType = request.getParameter("searchType");
        String keyword = request.getParameter("keyword");
        
        BoardDAO dao = boardDAO;
        List<BoardDTO> list = dao.getBoardList(type, page, pageSize, searchType, keyword);
        int totalCount = dao.getTotalCount(type, searchType, keyword);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        
        request.setAttribute("boardList", list);
        request.setAttribute("type", type);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("searchType", searchType);
        request.setAttribute("keyword", keyword);
        
        request.getRequestDispatcher("/WEB-INF/views/boardList.jsp").forward(request, response);
    }
}
