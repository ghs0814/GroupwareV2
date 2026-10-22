package com.groupware.controller;
import com.groupware.dto.EmployeeDTO;
import com.groupware.service.WorkflowService;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
@Controller
public class ProcessApprovalController {
    private final WorkflowService workflow;
    public ProcessApprovalController(WorkflowService workflow) { this.workflow=workflow; }
    @GetMapping("/processApproval.do")
    public String index() { return "redirect:documentList.do?tab=equipment"; }
    @PostMapping("/processApproval.do")
    public void approve(@RequestParam int rentalNo,@RequestParam int eqNo,@RequestParam int step,@RequestParam boolean isApprove,HttpSession session,HttpServletResponse response) throws Exception {
        EmployeeDTO actor=(EmployeeDTO)session.getAttribute("loginEmp");
        workflow.approveRental(rentalNo,eqNo,step,actor.getEmpNo(),isApprove);
        AccountController.alert(response,isApprove?"결재가 승인되었습니다.":"반려되었습니다. 재고가 복구되었습니다.","documentList.do?tab=equipment");
    }
}
