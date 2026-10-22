package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.groupware.dao.EquipmentDAO;
import com.groupware.dto.EquipmentDTO;

@Controller
@RequestMapping("/rentForm.do")
public class RentFormController {
    @Autowired
    private com.groupware.dao.EquipmentDAO equipmentDAO;

    private static final long serialVersionUID = 1L;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String eqNoStr = request.getParameter("eqNo");
        
        if (eqNoStr != null && !eqNoStr.isEmpty()) {
            try {
                int eqNo = Integer.parseInt(eqNoStr);
                EquipmentDAO dao = equipmentDAO;
                EquipmentDTO equipment = dao.getEquipmentDetail(eqNo);
                
                if (equipment != null) {
                    request.setAttribute("equipment", equipment);
                } else {
                    request.setAttribute("errorMsg", "該当する備品の情報が見つかりません。");
                }
                
            } catch (NumberFormatException e) {
                e.printStackTrace();
                request.setAttribute("errorMsg", "不正な備品番号形式です。");
            }
        } else {
            request.setAttribute("errorMsg", "備品番号パラメータが欠落しています。");
        }
        request.getRequestDispatcher("/WEB-INF/views/rentForm.jsp").forward(request, response);
    }

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
