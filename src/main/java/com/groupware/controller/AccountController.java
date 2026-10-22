package com.groupware.controller;

import com.groupware.dao.EmployeeDAO;
import com.groupware.dto.EmployeeDTO;
import com.groupware.util.CryptoUtil;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@Controller
public class AccountController {
    private final EmployeeDAO employeeDAO;
    private final DataSource dataSource;

    public AccountController(EmployeeDAO employeeDAO, DataSource dataSource) {
        this.employeeDAO = employeeDAO;
        this.dataSource = dataSource;
    }

    @GetMapping({"/", "/index.jsp", "/login.do"})
    public String index() { return "index"; }

    @PostMapping("/login.do")
    public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String number = value(request, "empNo");
        String password = value(request, "password");
        EmployeeDTO employee = number.matches("[0-9]+") ? employeeDAO.getEmployeeByNo(number) : null;
        if (employee == null || "Y".equals(employee.getRetired()) || !CryptoUtil.matches(password, employee.getEmpPw())) {
            request.setAttribute("msg", "사번 또는 비밀번호가 올바르지 않습니다.");
            request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
            return;
        }
        if (!employee.getEmpPw().startsWith("$2")) {
            employeeDAO.updatePassword(employee.getEmpNo(), CryptoUtil.encrypt(password));
        }
        request.changeSessionId();
        request.getSession().setAttribute("loginEmp", employee);
        response.sendRedirect("main.jsp");
    }

    @RequestMapping(value="/logout.do", method={RequestMethod.GET, RequestMethod.POST})
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().invalidate();
        response.sendRedirect("index.jsp");
    }

    @GetMapping({"/join.do", "/join.jsp"})
    public String join() { return "join"; }

    @GetMapping("/getEmpInfo.jsp")
    @ResponseBody
    public Map<String, Object> employeeInfo(@RequestParam(defaultValue="") String empNo, HttpSession session) {
        session.removeAttribute("registrationEmpNo");
        EmployeeDTO employee = empNo.matches("[0-9]+") ? employeeDAO.getEmployeeByNo(empNo) : null;
        if (employee == null || "Y".equals(employee.getRetired()) || (employee.getEmpPw() != null && !employee.getEmpPw().isBlank())) {
            return Map.of("result", "fail");
        }
        session.setAttribute("registrationEmpNo", employee.getEmpNo());
        return Map.of("result", "success", "retired", "N", "name", employee.getEmpName(),
                "dept", employee.getDept() == null ? "미배정" : employee.getDept(), "position", employee.getEmpLevel()+"레벨");
    }

    @PostMapping("/join.do")
    public void register(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Integer verified = (Integer) request.getSession().getAttribute("registrationEmpNo");
        String number = value(request, "empNo"), password = value(request, "password");
        if (verified == null || !number.equals(verified.toString()) || !validPassword(password)) {
            alert(response, "사원 확인 후 비밀번호를 4~72바이트로 입력해주세요.", "join.jsp"); return;
        }
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "UPDATE EMPLOYEE SET EMP_PW=? WHERE EMP_NO=? AND EMP_PW IS NULL AND RETIRED='N'")) {
            statement.setString(1, CryptoUtil.encrypt(password)); statement.setInt(2, verified);
            if (statement.executeUpdate() != 1) { alert(response, "이미 등록되었거나 사용할 수 없는 사원입니다.", "join.jsp"); return; }
        }
        request.getSession().removeAttribute("registrationEmpNo");
        alert(response, "등록되었습니다. 로그인해주세요.", "index.jsp");
    }

    @GetMapping({"/findPw.do", "/findPw.jsp", "/checkUser.do", "/findUser.jsp"})
    public String findPassword() { return "findPw"; }

    @PostMapping({"/findPw.do", "/checkUser.do"})
    public void verify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getSession().removeAttribute("resetEmpNo");
        String number = value(request,"empNo"), name = value(request,"empName");
        EmployeeDTO employee = number.matches("[0-9]+") ? employeeDAO.getEmployeeByNo(number) : null;
        if (employee == null || "Y".equals(employee.getRetired()) || !name.equals(employee.getEmpName())) {
            alert(response,"일치하는 사원 정보가 없습니다.","findPw.do"); return;
        }
        request.changeSessionId();
        request.getSession().setAttribute("resetEmpNo", employee.getEmpNo());
        request.getSession().setAttribute("resetVerifiedAt", System.currentTimeMillis());
        response.sendRedirect("resetPw.do");
    }

    private Integer resetEmployee(HttpSession session) {
        Long verifiedAt = (Long) session.getAttribute("resetVerifiedAt");
        if (verifiedAt == null || System.currentTimeMillis()-verifiedAt > 300000) return null;
        return (Integer) session.getAttribute("resetEmpNo");
    }

    @GetMapping({"/resetPw.do", "/resetPw.jsp"})
    public String resetPage(HttpSession session) {
        return resetEmployee(session) == null ? "redirect:findPw.do" : "resetPw";
    }

    @PostMapping("/resetPw.do")
    public void reset(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Integer employeeNo = resetEmployee(request.getSession());
        if (employeeNo == null) { response.sendError(403); return; }
        change(request,response,employeeNo,false);
    }

    @GetMapping({"/changePw.do", "/changePw.jsp"})
    public String changePage() { return "changePw"; }

    @PostMapping("/changePw.do")
    public void changePassword(HttpServletRequest request, HttpServletResponse response) throws Exception {
        EmployeeDTO employee = (EmployeeDTO)request.getSession().getAttribute("loginEmp");
        change(request,response,employee.getEmpNo(),true);
    }

    private void change(HttpServletRequest request, HttpServletResponse response, int employeeNo, boolean checkCurrent) throws Exception {
        String password=value(request,"newPw"), confirm=value(request,"newPwConfirm");
        EmployeeDTO employee=employeeDAO.getEmployeeByNo(String.valueOf(employeeNo));
        if (employee == null || "Y".equals(employee.getRetired())) { response.sendError(403); return; }
        if (!validPassword(password) || !password.equals(confirm)) {
            alert(response,"새 비밀번호를 확인해주세요.",checkCurrent?"changePw.do":"resetPw.do"); return;
        }
        if (checkCurrent && !CryptoUtil.matches(value(request,"currentPw"),employee.getEmpPw())) {
            alert(response,"현재 비밀번호가 일치하지 않습니다.","changePw.do"); return;
        }
        if (CryptoUtil.matches(password,employee.getEmpPw())) {
            alert(response,"기존과 다른 비밀번호를 입력해주세요.",checkCurrent?"changePw.do":"resetPw.do"); return;
        }
        if (!employeeDAO.updatePassword(employeeNo,CryptoUtil.encrypt(password))) throw new IllegalStateException("비밀번호 변경 실패");
        request.getSession().invalidate();
        alert(response,"변경되었습니다. 다시 로그인해주세요.","index.jsp");
    }

    private boolean validPassword(String password) {
        int size=password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        return size>=4 && size<=72;
    }
    private String value(HttpServletRequest request,String key) {
        String value=request.getParameter(key); return value==null?"":value.trim();
    }
    public static void alert(HttpServletResponse response,String message,String url) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        String safe=message.replace("\\","\\\\").replace("'","\\'").replace("\r"," ").replace("\n"," ").replace("<","\\u003c");
        response.getWriter().print("<script>alert('"+safe+"');location.href='"+url+"';</script>");
    }
}
