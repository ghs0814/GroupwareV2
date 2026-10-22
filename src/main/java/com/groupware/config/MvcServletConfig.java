package com.groupware.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletRegistrationBean;

@Configuration
public class MvcServletConfig {
    @Bean
    public DispatcherServletRegistrationBean dispatcherServletRegistration(DispatcherServlet dispatcherServlet, jakarta.servlet.MultipartConfigElement multipartConfig) {
        DispatcherServletRegistrationBean registration=new DispatcherServletRegistrationBean(dispatcherServlet,"/") {
            @Override
            protected void configure(jakarta.servlet.ServletRegistration.Dynamic servlet) {
                super.configure(servlet);
                // 기존 .jsp 주소는 MVC에서, WEB-INF 안의 실제 JSP는 Jasper에서 처리합니다.
                servlet.addMapping("/index.jsp","/join.jsp","/findPw.jsp","/findUser.jsp","/resetPw.jsp","/changePw.jsp",
                "/getEmpInfo.jsp","/main.jsp","/myPage.jsp","/myReservation.jsp","/myRental.jsp","/myLeave.jsp",
                "/officeMap.jsp","/admin.jsp","/adminEqList.jsp","/addressBook.jsp","/boardList.jsp","/boardDetail.jsp",
                "/boardWrite.jsp","/documentList.jsp","/equipmentList.jsp","/reserve.jsp","/roomUpdate.jsp","/rentForm.jsp",
                "/rentalDetail.jsp","/leaveForm.jsp","/leaveDetail.jsp");
            }
        };
        registration.setName("dispatcherServlet");
        registration.setMultipartConfig(multipartConfig);
        registration.setLoadOnStartup(1);
        return registration;
    }
}
