package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.groupware.dao.EquipmentDAO;
import com.groupware.dto.EquipmentDTO;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/updateEq.do")
public class EqUpdateController {
    @Autowired
    private com.groupware.dao.EquipmentDAO equipmentDAO;

    private static final long serialVersionUID = 1L;

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        int eqNo = Integer.parseInt(request.getParameter("eqNo"));
        String eqName = request.getParameter("eqName");
        int totalCount = Integer.parseInt(request.getParameter("totalCount")); 
        int remainCount = Integer.parseInt(request.getParameter("remainCount")); 
        EquipmentDTO dto = new EquipmentDTO();
        dto.setEqNo(eqNo);
        dto.setEqName(eqName);
        dto.setTotalCount(totalCount); 
        dto.setRemainCount(remainCount); 
        EquipmentDAO dao = equipmentDAO;
        boolean isSuccess = dao.updateEquipment(dto);
        PrintWriter out = response.getWriter();
        out.println("<script>");
        if (isSuccess) {
            out.println("alert('備品情報が正常に修正されました。');");
            out.println("location.href='adminEqList.do';");
        } else {
            out.println("alert('修正に失敗しました.');");
            out.println("history.back();");
        }
        out.println("</script>");
        out.flush();
        out.close();
    }
}
