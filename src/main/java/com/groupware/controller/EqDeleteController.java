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
@RequestMapping("/deleteEq.do")
public class EqDeleteController {
    @Autowired
    private com.groupware.dao.EquipmentDAO equipmentDAO;

    private static final long serialVersionUID = 1L;

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        int eqNo = Integer.parseInt(request.getParameter("eqNo"));
        EquipmentDAO dao = equipmentDAO;
        int checkEquipment = dao.getCheckEquipment(eqNo);
        System.out.println(checkEquipment);

        PrintWriter out = response.getWriter();
        out.println("<script>");
        try {
			if(checkEquipment > 0)
			{
				  boolean isSuccess = dao.deleteEquipment(eqNo); 
				  
				  if (isSuccess) {
					  	out.println("alert('備品が正常に廃棄されました。');");
				  		out.println("location.href='adminEqList.do';"); 
				  } 
				  else {
					  out.println("alert('廃棄処理に失敗しました。');"); 
					  out.println("history.back();"); 
				  }
			}else {
				out.println("alert('返却を確認してください。廃棄処理に失敗しました。');"); 
				out.println("history.back();");
			}
				
        } catch (Exception e) {
            out.println("alert('現在貸出記録が残っている備品は廃棄できません。');");
            out.println("history.back();");
        }
        out.println("</script>");
        out.flush();
        out.close();
    }
}
