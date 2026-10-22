package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.groupware.dao.EquipmentDAO;
import com.groupware.dto.EquipmentDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/equipmentList.do")
public class EquipmentListController {
    @Autowired
    private com.groupware.dao.EquipmentDAO equipmentDAO;

    private static final long serialVersionUID = 1L;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	int currentPage = 1;
    	String pageParam = request.getParameter("page");
    	if (pageParam != null && !pageParam.isEmpty()) {
    		try {
    			currentPage = Integer.parseInt(pageParam);
    		} catch (NumberFormatException e) {
    			currentPage = 1;
    		}
    	}

    	int pageSize = 10; // 한 페이지당 출력할 항목 수
    	int startRow = (currentPage - 1) * pageSize + 1;
    	int endRow = currentPage * pageSize;
    	EquipmentDAO dao = equipmentDAO;
    	List<EquipmentDTO> eqList = dao.getEquipmentsPaging(startRow, endRow);
    	int totalCount = dao.getTotalCount();
    	int totalPages = (int) Math.ceil((double) totalCount / pageSize);
    	int pageBlock = 5; // 하단에 표시할 페이지 번호 개수
    	int startPage = ((currentPage - 1) / pageBlock) * pageBlock + 1;
    	int endPage = startPage + pageBlock - 1;
    	if (endPage > totalPages) {
    		endPage = totalPages;
    	}
    	request.setAttribute("eqList", eqList);
    	request.setAttribute("currentPage", currentPage);
    	request.setAttribute("totalPages", totalPages);
    	request.setAttribute("startPage", startPage);
    	request.setAttribute("endPage", endPage);
    	request.getRequestDispatcher("/WEB-INF/views/equipmentList.jsp").forward(request, response);
    }
}
