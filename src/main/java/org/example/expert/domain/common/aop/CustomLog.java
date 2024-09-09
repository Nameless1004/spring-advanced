package org.example.expert.domain.common.aop;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j(topic = "Admin Log")
public class CustomLog {

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..))")
    public void adminControllerDeleteComment(){}

    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public void userControllerChangeUserRole(){}

    @Before("adminControllerDeleteComment()|| userControllerChangeUserRole()")
    public void beforeLog(JoinPoint joinPoint){
        ServletRequestAttributes attributes = (ServletRequestAttributes ) RequestContextHolder.getRequestAttributes();

        if(attributes == null){
            throw new NullPointerException("Not Found Attributes");
        }

        HttpServletRequest request = attributes.getRequest();
        Long requestUserId = (Long) request.getAttribute("userId");
        String requestedURI = request.getRequestURI();
        String requestApiTime = LocalDateTime.now().toString();

        log.info("API 사용자 ID : {}", requestUserId);
        log.info("API 요청 시간 : {}", requestApiTime);
        log.info("API 요청 URI : {}", requestedURI);
    }
}
