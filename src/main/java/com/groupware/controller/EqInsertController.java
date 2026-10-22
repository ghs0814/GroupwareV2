package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.groupware.dao.EquipmentDAO;
import com.groupware.dao.RentalDAO;
import com.groupware.dto.EquipmentDTO;
import com.groupware.dto.RentalHistoryDTO;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/insertEq.do")
public class EqInsertController {
    @Autowired
    private com.groupware.dao.EquipmentDAO equipmentDAO;

    private static final long serialVersionUID = 1L;

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        String eqName = request.getParameter("eqName");
        int totalCount = Integer.parseInt(request.getParameter("totalCount")); 
        EquipmentDTO dto = new EquipmentDTO();
        dto.setEqName(eqName);
        dto.setTotalCount(totalCount); 
        EquipmentDAO dao = equipmentDAO;
        boolean isSuccess = dao.insertEquipment(dto);
        PrintWriter out = response.getWriter();
        out.println("<script>");
        if (isSuccess) {
            out.println("alert('新規備品が正常に登録されました。');");
            out.println("location.href='adminEqList.do';");
        } else {
            out.println("alert('登録に失敗しました。');");
            out.println("history.back();");
        }
        out.println("</script>");
        out.flush();
        out.close();
    }
}
