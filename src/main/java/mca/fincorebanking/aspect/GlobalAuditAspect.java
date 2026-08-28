package mca.fincorebanking.aspect;

import mca.fincorebanking.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class GlobalAuditAspect {

    private final AuditService auditService;

    public GlobalAuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Pointcut("execution(* mca.fincorebanking.service..*(..))")
    public void serviceLayer() {
    }

    @Pointcut("execution(* mca.fincorebanking.service.AuditService.*(..))")
    public void auditServiceLayer() {
    }

    @AfterReturning(pointcut = "serviceLayer() && !auditServiceLayer()", returning = "result")
    public void logAfterServiceMethod(JoinPoint joinPoint, Object result) {
        try {

            String username = getCurrentUsername();

            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();

            String action = String.format("Executed: %s.%s()", className, methodName);

            if (joinPoint.getArgs().length > 0 && isCriticalAction(methodName)) {
                String args = Arrays.stream(joinPoint.getArgs())
                        .map(obj -> obj != null ? obj.toString() : "null")
                        .limit(1)
                        .collect(Collectors.joining(", "));
                action += " [Args: " + args + "]";
            }

            auditService.log(username, action);

        } catch (Exception e) {

            System.err.println("Failed to log audit: " + e.getMessage());
        }
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private boolean isCriticalAction(String methodName) {
        methodName = methodName.toLowerCase();
        return methodName.contains("save") ||
                methodName.contains("update") ||
                methodName.contains("delete") ||
                methodName.contains("approve") ||
                methodName.contains("reject") ||
                methodName.contains("block");
    }
}