package com.snackadmin.aspect;

import com.snackadmin.annotation.OperationLog;
import com.snackadmin.entity.SysOperationLog;
import com.snackadmin.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysOperationLog log = new SysOperationLog();
        log.setModule(operationLog.module());
        log.setOperation(operationLog.operation());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.setMethod(signature.getMethod().getName());

        // 请求信息
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            log.setRequestUrl(request.getRequestURI());
            log.setOperatorIp(request.getRemoteAddr());
            // 限制参数长度
            String params = Arrays.toString(joinPoint.getArgs());
            if (params.length() > 1000) params = params.substring(0, 1000) + "...";
            log.setRequestParams(params);
        }

        try {
            Object result = joinPoint.proceed();
            log.setResult("SUCCESS");
            log.setCostTime(System.currentTimeMillis() - startTime);
            return result;
        } catch (Throwable e) {
            log.setResult("FAIL");
            log.setErrorMsg(e.getMessage() != null && e.getMessage().length() > 2000
                    ? e.getMessage().substring(0, 2000) : e.getMessage());
            log.setCostTime(System.currentTimeMillis() - startTime);
            throw e;
        } finally {
            try {
                var auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()) {
                    com.snackadmin.entity.SysUser user = null;
                    // Try to get user via injected service (simplified: use SecurityContext name)
                    log.setOperatorName(auth.getName());
                }
                operationLogService.saveLog(log);
            } catch (Exception ignored) { /* don't block business */ }
        }
    }
}
