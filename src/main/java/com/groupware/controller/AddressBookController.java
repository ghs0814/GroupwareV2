package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.groupware.dao.AddressBookDAO;
import com.groupware.dto.EmployeeDTO;

@Controller
@RequestMapping("/addressBook.do")
public class AddressBookController {
    @Autowired
    private com.groupware.dao.AddressBookDAO addressBookDAO;

    private static final long serialVersionUID = 1L;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("loginEmp") == null) {
            response.sendRedirect("index.jsp");
            return;
        }
        String pageStr = request.getParameter("page");
        int page = (pageStr == null || pageStr.isEmpty()) ? 1 : Integer.parseInt(pageStr);
        int pageSize = 10;

        AddressBookDAO dao = addressBookDAO;
        List<EmployeeDTO> empList = dao.getAddressListPaged(page, pageSize);
        int totalCount = dao.getTotalAddressCount();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        request.setAttribute("empList", empList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        
        request.getRequestDispatcher("/WEB-INF/views/addressBook.jsp").forward(request, response);
    }

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
