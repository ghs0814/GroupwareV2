package com.groupware.config;

import com.groupware.dao.EmployeeDAO;
import com.groupware.dto.EmployeeDTO;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.LocaleResolver;
import java.util.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final EmployeeDAO employeeDAO;
    public WebConfig(EmployeeDAO employeeDAO) { this.employeeDAO=employeeDAO; }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver=new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.KOREAN);
        return resolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            private final Set<String> publicPaths=Set.of("/","/index.jsp","/login.do","/join.do","/join.jsp",
                    "/getEmpInfo.jsp","/findPw.do","/findPw.jsp","/findUser.jsp","/checkUser.do","/resetPw.do","/resetPw.jsp","/error");
            private final Set<String> adminPaths=Set.of("/admin.do","/admin.jsp","/adminAction.do","/insertEmp.do",
                    "/adminEqList.do","/adminEqList.jsp","/insertEq.do","/updateEq.do","/deleteEq.do","/roomUpdate.do","/roomUpdate.jsp","/roomUpdateProcess.do");
            private final Set<String> changingGetPaths=Set.of("/logout.do","/boardDelete.do","/commentDelete.do","/cancelReserve.do","/returnProcess.do");
            @Override
            public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) throws Exception {
                if (request.getDispatcherType()!=jakarta.servlet.DispatcherType.REQUEST) return true;
                String path=request.getServletPath();
                HttpSession session=request.getSession();
                if (session.getAttribute("csrfToken")==null) session.setAttribute("csrfToken",UUID.randomUUID().toString());
                if ("POST".equals(request.getMethod()) || changingGetPaths.contains(path)) {
                    String token=request.getHeader("X-CSRF-Token");
                    if(token==null) token=request.getParameter("csrfToken");
                    if(!session.getAttribute("csrfToken").equals(token)) { response.sendError(403,"화면을 새로 연 후 다시 시도해주세요."); return false; }
                }
                if (publicPaths.contains(path)) return true;
                EmployeeDTO employee=(EmployeeDTO)session.getAttribute("loginEmp");
                if (employee!=null) employee=employeeDAO.getEmployeeByNo(String.valueOf(employee.getEmpNo()));
                if (employee==null || "Y".equals(employee.getRetired())) {
                    session.removeAttribute("loginEmp");
                    if(path.equals("/holdReserve.do") || path.equals("/checkReservedTime.do")) response.sendError(401);
                    else response.sendRedirect(request.getContextPath()+"/index.jsp");
                    return false;
                }
                session.setAttribute("loginEmp",employee);
                if(adminPaths.contains(path) && !"Y".equals(employee.getManager())) { response.sendError(403); return false; }
                return true;
            }
        }).addPathPatterns("/**").excludePathPatterns("/css/**","/images/**","/js/**","/favicon.ico","/WEB-INF/**");
    }
}
