package mca.fincorebanking.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import mca.fincorebanking.config.NumericConfigStore;
import mca.fincorebanking.entity.Role;
import mca.fincorebanking.entity.User;
import mca.fincorebanking.service.AccountService;
import mca.fincorebanking.service.BeneficiaryService;
import mca.fincorebanking.service.FraudService;
import mca.fincorebanking.service.LoanService;
import mca.fincorebanking.service.SuperAdminService;
import mca.fincorebanking.service.TellerService;
import mca.fincorebanking.service.TransactionService;
import mca.fincorebanking.service.UserService;

@Controller
public class HomeController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final LoanService loanService;
    private final UserService userService;
    private final FraudService fraudService;
    private final BeneficiaryService beneficiaryService;
    private final TellerService tellerService;
    private final SuperAdminService superAdminService;

    public HomeController(SuperAdminService superAdminService,
            AccountService accountService,
            TransactionService transactionService,
            LoanService loanService,
            UserService userService,
            FraudService fraudService,
            BeneficiaryService beneficiaryService,
            TellerService tellerService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.loanService = loanService;
        this.userService = userService;
        this.fraudService = fraudService;
        this.beneficiaryService = beneficiaryService;
        this.tellerService = tellerService;
        this.superAdminService = superAdminService;
    }

    @GetMapping({ "/dashboard", "/" })
    public String dashboard(HttpServletRequest request, Model model, Authentication auth) {
        String username = auth.getName();
        String role = auth.getAuthorities().stream()
                .map(t -> t.getAuthority())
                .findFirst()
                .orElse("ROLE_CUSTOMER");

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("username", username);
        model.addAttribute("userRole", role);

        switch (role) {
            case "ROLE_CUSTOMER", "ROLE_CORPORATE" -> loadCustomerData(model, username);
            case "ROLE_ADMIN" -> loadAdminData(model);

            case "ROLE_TELLER" -> loadTellerData(model, username);

            case "ROLE_MANAGER" -> loadManagerData(model);

            case "ROLE_COMPLIANCE", "ROLE_AUDITOR" -> loadComplianceData(model);

            case "ROLE_RELATIONSHIP_MGR" -> {

                model.addAttribute("assignedClients",
                        NumericConfigStore.get().dashboard.relationshipAssignedClients);
                model.addAttribute("totalAum", NumericConfigStore.get().dashboard.relationshipTotalAum);
            }
            case "ROLE_SUPER_ADMIN" -> {
                model.addAttribute("serverStatus", "ONLINE");
                model.addAttribute("dbConnection", "STABLE");
                model.addAttribute("activeSessions", NumericConfigStore.get().dashboard.superAdminActiveSessions);
                model.addAttribute("currentUri", request.getRequestURI());
                model.addAttribute("userRole", "ROLE_SUPER_ADMIN");
                model.addAttribute("stats", superAdminService.getSystemStats());
            }
        }

        return "dashboard";
    }

    private void loadCustomerData(Model model, String username) {
        model.addAttribute("totalAccounts", accountService.countByUsername(username));
        model.addAttribute("totalBalance", accountService.totalBalanceByUsername(username));
        model.addAttribute("totalTransactions", transactionService.countByUsername(username));
        model.addAttribute("activeLoans", loanService.countActiveLoans(username));
        model.addAttribute("recentTransactions", transactionService.findRecentByUser(username,
                NumericConfigStore.get().transactions.recentTransactionsLimit));
    }

    private void loadAdminData(Model model) {
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("activeAccounts", accountService.countActiveAccounts());
        model.addAttribute("pendingApprovals", beneficiaryService.countPending());
        model.addAttribute("fraudCount", fraudService.countFrauds());
        model.addAttribute("pendingAccountCount", accountService.getAccountsByStatus("PENDING_ADMIN").size());

        for (Role role : Role.values()) {
            long count = userService.countByRole(role.name());
            model.addAttribute(role.name().toLowerCase() + "Count", count);
        }
        model.addAttribute("loanApproved", loanService.countByStatus("APPROVED"));
        model.addAttribute("loanPending", loanService.countByStatus("PENDING"));
        model.addAttribute("loanRejected", loanService.countByStatus("REJECTED"));
    }

    private void loadTellerData(Model model, String username) {
        User teller = userService.findByUsername(username);
        var history = tellerService.getTellerHistory(teller.getId());
        model.addAttribute("todayTransactions", history);

        model.addAttribute("cashInDrawer", NumericConfigStore.get().dashboard.tellerCashInDrawer);
        model.addAttribute("customersServed", history.size());
    }

    private void loadManagerData(Model model) {
        model.addAttribute("pendingLoans", loanService.countByStatus("PENDING"));
        model.addAttribute("pendingAccounts", accountService.getPendingAccounts().size());
        model.addAttribute("branchDeposits", NumericConfigStore.get().dashboard.managerBranchDeposits);
    }

    private void loadComplianceData(Model model) {
        model.addAttribute("fraudCount", fraudService.countFrauds());
        model.addAttribute("flaggedTransactions", NumericConfigStore.get().dashboard.complianceFlaggedTransactions);
        model.addAttribute("auditLogCount", NumericConfigStore.get().dashboard.complianceAuditLogCount);
    }
}
