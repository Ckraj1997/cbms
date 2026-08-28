package mca.fincorebanking.controller;

import jakarta.servlet.http.HttpServletRequest;
import mca.fincorebanking.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String viewNotifications(HttpServletRequest request,
            Model model,
            Authentication authentication) {

        model.addAttribute("currentUri", request.getRequestURI());

        model.addAttribute(
                "notifications",
                notificationService.getUserNotifications(
                        authentication.getName()));

        return "notification-list";
    }
}
