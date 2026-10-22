package com.groupware.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.time.LocalDate;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.groupware.dao.EquipmentDAO;

import com.groupware.dto.EmployeeDTO;
import com.groupware.dto.EquipmentDTO;
import com.groupware.dto.RentalHistoryDTO;

@Controller
@RequestMapping("/rentInsert.do")
public class RentInsertController {
    @Autowired private com.groupware.service.WorkflowService workflow;
    @Autowired
    private com.groupware.dao.EquipmentDAO equipmentDAO;

    private static final long serialVersionUID = 1L;

    @PostMapping
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        EmployeeDTO loginEmp = (EmployeeDTO) session.getAttribute("loginEmp");
        
        if (loginEmp == null) {
            out.println("<script>alert('ログインが必要なサービスです。');location.href='index.jsp';</script>");
            out.close();
            return;
        }
        
        try {
            int eqNo = Integer.parseInt(request.getParameter("eqNo"));
            String rentCountParam = request.getParameter("rentCount");
            int rentCount = 1; // 기본값 1 세팅
            
            if (rentCountParam != null && !rentCountParam.trim().isEmpty() && !"null".equalsIgnoreCase(rentCountParam)) {
                rentCount = Integer.parseInt(rentCountParam.trim());
            }
            
            String startDateStr = request.getParameter("rentalDate");
            String endDateStr = request.getParameter("returnDate");
            String content = request.getParameter("content"); 
            EquipmentDAO eqDao = equipmentDAO;
            EquipmentDTO equipment = eqDao.getEquipmentDetail(eqNo);
            
            if (equipment == null) {
                throw new Exception("存在しない備品番号です。");
            }
            
            if (equipment.getRemainCount() < rentCount) {
                out.println("<script>alert('貸出可能な在庫が不足しています。(現在の残り数量: " + equipment.getRemainCount() + "個)');history.back();</script>");
                return;
            }
            RentalHistoryDTO rentalDto = new RentalHistoryDTO();
            rentalDto.setEmpNo(loginEmp.getEmpNo());                  // 起案者社員番号
            rentalDto.setEqNo(eqNo);                                 // 備品番号
            rentalDto.setReqCount(rentCount);                        // 申請数量
            rentalDto.setTitle(equipment.getEqName() + " 貸出申請"); // 起案書タイトルを自動構成
            rentalDto.setContent(content);                           // 貸出理由
            rentalDto.setRentalDate(Date.valueOf(LocalDate.now()));
            rentalDto.setReturnDate(Date.valueOf(LocalDate.now().plusDays(7)));
            if (startDateStr != null && !startDateStr.trim().isEmpty() && !"null".equalsIgnoreCase(startDateStr)) {
                rentalDto.setRentalDate(Date.valueOf(startDateStr.trim()));
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty() && !"null".equalsIgnoreCase(endDateStr)) {
                rentalDto.setReturnDate(Date.valueOf(endDateStr.trim()));
            }
            int empLevel = loginEmp.getEmpLevel(); 
            
            if (empLevel == 5) {
                rentalDto.setStatus("대여중"); // DB 내부 상태 로직 연동을 위해 한글 데이터 매핑 유지
                rentalDto.setApprovalStep(6);         // 最終締め切り段階(6)をセット
                rentalDto.setSign5(loginEmp.getEmpName()); 
                rentalDto.setSign5Date(Date.valueOf(LocalDate.now())); 
            } else {
                rentalDto.setStatus("승인대기"); // DB 내부 상태 로직 연동을 위해 한글 데이터 매핑 유지
                rentalDto.setApprovalStep(empLevel + 1); 
                if (empLevel == 1) {
                    rentalDto.setSign1(loginEmp.getEmpName());
                    rentalDto.setSign1Date(Date.valueOf(LocalDate.now()));
                } else if (empLevel == 2) {
                    rentalDto.setSign2(loginEmp.getEmpName());
                    rentalDto.setSign2Date(Date.valueOf(LocalDate.now()));
                } else if (empLevel == 3) {
                    rentalDto.setSign3(loginEmp.getEmpName());
                    rentalDto.setSign3Date(Date.valueOf(LocalDate.now()));
                } else if (empLevel == 4) {
                    rentalDto.setSign4(loginEmp.getEmpName());
                    rentalDto.setSign4Date(Date.valueOf(LocalDate.now()));
                }
            }

            boolean isSuccess = workflow.createRental(rentalDto); 

            if (isSuccess) {
                out.println("<script>alert('備品貸出申請が完了しました。');location.href='documentList.do?tab=equipment';</script>");
            } else {
                throw new Exception("RENTAL_HISTORY テーブルのデータ挿入トランザクションエラー");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<script>alert('貸出申請の処理中にシステムエラーが発生しました。');history.back();</script>");
        } finally {
            out.close();
        }
    }
}
