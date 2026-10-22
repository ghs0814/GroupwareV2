package com.groupware.controller;
import com.groupware.dto.EmployeeDTO;
import com.groupware.service.WorkflowService;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
@Controller
public class ReturnProcessController {
    private final WorkflowService workflow;
    public ReturnProcessController(WorkflowService workflow) { this.workflow=workflow; }
    @RequestMapping(value="/returnProcess.do",method={RequestMethod.GET,RequestMethod.POST})
    public void returnEquipment(@RequestParam int rentalNo,HttpSession session,HttpServletResponse response) throws Exception {
        EmployeeDTO actor=(EmployeeDTO)session.getAttribute("loginEmp");
        workflow.returnRental(rentalNo,actor.getEmpNo());
        AccountController.alert(response,"반납되었습니다.","main.jsp");
    }
}
