package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.groupware.dao.RentalDAO;
import com.groupware.dto.RentalHistoryDTO;
import com.groupware.dto.EmployeeDTO;

@Controller
@RequestMapping("/rentalDetail.do")
public class RentalDetailController {
    @Autowired
    private com.groupware.dao.RentalDAO rentalDAO;

    private static final long serialVersionUID = 1L;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        HttpSession session = request.getSession();
        EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
        if (loginEmp == null) {
            PrintWriter out = response.getWriter();
            out.println("<script>alert('ログインが必要なサービスです。');location.href='index.jsp';</script>");
            out.close();
            return;
        }
        String rentalNoStr = request.getParameter("rentalNo");
        if (rentalNoStr == null || rentalNoStr.trim().isEmpty()) {
            response.sendRedirect("documentList.do");
            return;
        }
        
        try {
            int rentalNo = Integer.parseInt(rentalNoStr);
            RentalDAO rentalDao = rentalDAO;
            RentalHistoryDTO rentalDetail = rentalDao.getDocumentDetail(rentalNo);
            
            if (rentalDetail == null) {
                PrintWriter out = response.getWriter();
                out.println("<script>alert('存在しないか削除された起案文書です。');location.href='documentList.do';</script>");
                out.close();
                return;
            }
            request.setAttribute("detail", rentalDetail);
            request.getRequestDispatcher("/WEB-INF/views/rentalDetail.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            PrintWriter out = response.getWriter();
            out.println("<script>alert('詳細表示を取得中にエラーが発生しました。');location.href='documentList.do';</script>");
            out.close();
        }
    }

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
