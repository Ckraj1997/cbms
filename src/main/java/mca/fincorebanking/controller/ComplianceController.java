package mca.fincorebanking.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mca.fincorebanking.entity.AuditLog;
import mca.fincorebanking.service.AccountService;
import mca.fincorebanking.service.AuditService;
import mca.fincorebanking.service.FraudService;

@Controller
@RequestMapping("/compliance")
public class ComplianceController {

    private final AuditService auditService;
    private final FraudService fraudService;
    private final AccountService accountService;

    public ComplianceController(AuditService auditService, FraudService fraudService, AccountService accountService) {
        this.auditService = auditService;
        this.fraudService = fraudService;
        this.accountService = accountService;
    }

    @GetMapping("/audit/logs")
    public String viewAuditLogs(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("logs", auditService.getAllLogs());
        return "compliance-audit";
    }

    @GetMapping("/audit/download")
    public void downloadAuditLogsCsv(HttpServletResponse response) throws IOException {
        // 1. Set up the CSV response
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=compliance-audit-logs.csv");

        // 2. Fetch all audit logs
        List<AuditLog> logs = auditService.getAllLogs();

        // 3. Write data to the CSV file
        try (PrintWriter writer = response.getWriter()) {
            writer.println("Username,Action,Timestamp");
            
            for (AuditLog log : logs) {
                writer.printf("%s,%s,%s%n",
                        log.getUsername(),
                        log.getAction(),
                        log.getTimestamp());
            }
        }
    }

    @GetMapping("/reports/fraud")
    public String viewFraudAlerts(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("frauds", fraudService.getAllFraudLogs());
        return "compliance-fraud";
    }

    @PostMapping("/actions/flag-user")
    public String flagUser(@RequestParam String username, @RequestParam String reason, RedirectAttributes redirect) {
        try {
            fraudService.logFraud(username, "MANUAL FLAG: " + reason);

            redirect.addFlashAttribute("success", "User " + username + " flagged as suspicious.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error flagging user: " + e.getMessage());
        }
        return "redirect:/compliance/reports/fraud";
    }

    @PostMapping("/actions/freeze-account")
    public String freezeUser(@RequestParam String username, RedirectAttributes redirect) {
        try {
            accountService.freezeAccountByUsername(username);

            redirect.addFlashAttribute("success", "All accounts for user '" + username + "' have been FROZEN.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Failed to freeze user: " + e.getMessage());
        }
        return "redirect:/compliance/reports/fraud";
    }
}