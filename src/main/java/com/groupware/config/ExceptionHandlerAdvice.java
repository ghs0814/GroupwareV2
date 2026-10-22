package com.groupware.config;
import jakarta.servlet.http.*;
import org.springframework.web.bind.annotation.*;
import com.groupware.controller.AccountController;
import java.io.IOException;

@ControllerAdvice
public class ExceptionHandlerAdvice {
    @ExceptionHandler(IllegalArgumentException.class)
    public void invalidInput(IllegalArgumentException exception,HttpServletRequest request,HttpServletResponse response) throws IOException {
        response.setStatus(400);
        AccountController.alert(response,exception.getMessage(),request.getContextPath()+"/main.jsp");
    }
}
