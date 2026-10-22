package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.groupware.dao.EquipmentDAO;
import com.groupware.dto.EquipmentDTO;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/adminEqList.do")
public class AdminEqListController {
    @Autowired
    private com.groupware.dao.EquipmentDAO equipmentDAO;

    private static final long serialVersionUID = 1L;

    @GetMapping
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EquipmentDAO dao = equipmentDAO;
        List<EquipmentDTO> eqList = dao.getAllEquipments(); 
        request.setAttribute("eqList", eqList);
        request.getRequestDispatcher("/WEB-INF/views/adminEqList.jsp").forward(request, response);
    }
}
