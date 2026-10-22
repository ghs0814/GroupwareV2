package com.groupware.controller;
import com.groupware.dto.EmployeeDTO;
import com.groupware.service.WorkflowService;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
@Controller
public class LeaveApproveProcessController {
    private final WorkflowService workflow;
    public LeaveApproveProcessController(WorkflowService workflow) { this.workflow=workflow; }
    @PostMapping("/leaveApproveProcess.do")
    public String approve(@RequestParam int leaveNo,@RequestParam int step,@RequestParam String action,HttpSession session) {
        if(!action.equals("approve") && !action.equals("reject")) throw new IllegalArgumentException("결재 동작을 확인해주세요.");
        EmployeeDTO actor=(EmployeeDTO)session.getAttribute("loginEmp");
        workflow.approveLeave(leaveNo,step,actor.getEmpNo(),action.equals("approve"));
        return "redirect:leaveDetail.do?leaveNo="+leaveNo;
    }
}
