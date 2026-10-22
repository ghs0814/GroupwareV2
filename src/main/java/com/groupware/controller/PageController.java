package com.groupware.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/officeMap.jsp")
    public String office() { return "officeMap"; }

    @GetMapping({"/admin.jsp","/adminEqList.jsp","/addressBook.jsp","/boardList.jsp","/boardDetail.jsp",
            "/boardWrite.jsp","/documentList.jsp","/equipmentList.jsp","/reserve.jsp","/roomUpdate.jsp",
            "/rentForm.jsp","/rentalDetail.jsp","/leaveForm.jsp","/leaveDetail.jsp"})
    public String legacyPage(HttpServletRequest request) {
        String path=request.getServletPath();
        String target=path.substring(1,path.length()-4)+".do";
        String query=request.getQueryString();
        return "redirect:"+target+(query==null?"":"?"+query);
    }
}
