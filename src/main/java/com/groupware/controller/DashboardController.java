package com.groupware.controller;

import com.groupware.service.DashboardService;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;

@Controller
public class DashboardController {
    private final DashboardService dashboard;
    public DashboardController(DashboardService dashboard) { this.dashboard=dashboard; }

    @GetMapping({"/main.do","/main.jsp"})
    public String main(HttpServletRequest request,HttpServletResponse response) throws IOException {
        dashboard.prepareMain(request,response); return "main";
    }
    @GetMapping({"/myPage.do","/myPage.jsp"})
    public String myPage(HttpServletRequest request,HttpServletResponse response) throws IOException {
        dashboard.prepareMyPage(request,response); return "myPage";
    }
    @GetMapping("/myReservation.jsp")
    public String reservations(HttpServletRequest request,HttpServletResponse response) throws IOException {
        dashboard.prepareMyReservation(request,response); return "myReservation";
    }
    @GetMapping("/myRental.jsp")
    public String rentals(HttpServletRequest request,HttpServletResponse response) throws IOException {
        dashboard.prepareMyRental(request,response); return "myRental";
    }
    @GetMapping("/myLeave.jsp")
    public String leaves(HttpServletRequest request,HttpServletResponse response) throws IOException {
        dashboard.prepareMyLeave(request,response); return "myLeave";
    }
}
