package mca.fincorebanking.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mca.fincorebanking.config.NumericConfigStore;
import mca.fincorebanking.entity.Loan;
import mca.fincorebanking.entity.Role;
import mca.fincorebanking.entity.Transaction;
import mca.fincorebanking.entity.User;
import mca.fincorebanking.service.AccountService;
import mca.fincorebanking.service.BeneficiaryService;
import mca.fincorebanking.service.CardService;
import mca.fincorebanking.service.KycService;
import mca.fincorebanking.service.LoanService;
import mca.fincorebanking.service.PdfService;
import mca.fincorebanking.service.TransactionService;
import mca.fincorebanking.service.UserService;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final AccountService accountService;
    private final LoanService loanService;
    private final KycService kycService;
    private final CardService cardService;
    private final BeneficiaryService beneficiaryService;
    private final UserService userService;
    private final PdfService pdfService;
    private final TransactionService transactionService;

    public ManagerController(AccountService accountService, LoanService loanService, KycService kycService,
            CardService cardService, BeneficiaryService beneficiaryService, UserService userService,
            PdfService pdfService,TransactionService transactionService) {
        this.accountService = accountService;
        this.loanService = loanService;
        this.kycService = kycService;
        this.cardService = cardService;
        this.beneficiaryService = beneficiaryService;
        this.userService = userService;
        this.pdfService = pdfService;
        this.transactionService = transactionService;
    }

    @GetMapping("/approvals")
    public String approvalsHub(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());

        model.addAttribute("cntAccounts", accountService.getPendingAccounts().size());
        model.addAttribute("cntLoans", loanService.getPendingLoans().size());
        model.addAttribute("cntKyc", kycService.getPendingKycs().size());
        model.addAttribute("cntCheques", cardService.getAllPendingChequeRequests().size());
        return "manager-approvals";
    }

    @GetMapping("/reports")
    public String reportsDashboard(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());

        model.addAttribute("totalDeposits", NumericConfigStore.get().managerReports.totalDeposits);
        model.addAttribute("totalWithdrawals", NumericConfigStore.get().managerReports.totalWithdrawals);
        model.addAttribute("newAccountsThisMonth",
                accountService.getPendingAccounts().size() + NumericConfigStore.get().managerReports.newAccountsOffset);
        model.addAttribute("loansDisbursed", loanService.countByStatus("APPROVED"));

        return "manager-reports";
    }

    @GetMapping("/reports/transactions")
    public void downloadBranchTransactions(HttpServletResponse response) throws IOException {
        // Set up the CSV response
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=branch-transactions-report.csv");

        // Fetch all transactions (you can filter this by month in the service layer if needed)
        List<Transaction> transactions = transactionService.getAllTransactions(); 

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Transaction ID,Account Number,Date,Type,Amount,Balance After");
            
            for (Transaction tx : transactions) {
                writer.printf("%d,%s,%s,%s,%.2f,%.2f%n",
                        tx.getId(),
                        tx.getAccount().getAccountNumber(),
                        tx.getTransactionTime(),
                        tx.getType(),
                        tx.getAmount(),
                        tx.getBalanceAfter());
            }
        }
    }

    @GetMapping("/reports/loans")
    public void downloadLoanAgingReport(HttpServletResponse response) throws IOException {
        // Set up the CSV response
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=branch-loan-aging-report.csv");

        // Fetch all active/approved loans
        List<Loan> loans = loanService.getLoansByStatus("APPROVED");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Loan ID,Customer Username,Account Number,Loan Type,Amount,Tenure (Months),Status");
            
            for (Loan loan : loans) {
                // Ensure we don't crash if an older loan doesn't have a linked account
                String accNum = (loan.getAccount() != null) ? loan.getAccount().getAccountNumber() : "N/A";
                
                writer.printf("%d,%s,%s,%s,%.2f,%d,%s%n",
                        loan.getId(),
                        loan.getUser().getUsername(),
                        accNum,
                        loan.getLoanType(),
                        loan.getAmount(),
                        loan.getTenureMonths(),
                        loan.getStatus());
            }
        }
    }


    
    @GetMapping("/accounts")
    public String pendingAccounts(Model model, HttpServletRequest request) {

        model.addAttribute("accounts", accountService.getPendingAccounts());
        model.addAttribute("currentUri", request.getRequestURI());
        return "manager-account-approval";
    }

    @PostMapping("/accounts/{id}/approve")
    public String approveAccount(@PathVariable Long id, RedirectAttributes redirect) {

        accountService.approveAccount(id);
        redirect.addFlashAttribute("success", "Account Activated Successfully.");
        return "redirect:/manager/accounts";
    }

    @GetMapping("/loans")
    public String pendingLoans(Model model, HttpServletRequest request) {
        model.addAttribute("loans", loanService.getPendingLoans());
        model.addAttribute("currentUri", request.getRequestURI());
        return "manager-loan-list";
    }

    @PostMapping("/loans/{id}/approve")
    public String approveLoan(@PathVariable Long id, RedirectAttributes redirect) {

        loanService.approveLoan(id);
        redirect.addFlashAttribute("success", "Loan Approved & Disbursed.");
        return "redirect:/manager/loans";
    }

    @PostMapping("/loans/{id}/reject")
    public String rejectLoan(@PathVariable Long id, RedirectAttributes redirect) {

        loanService.rejectLoan(id);
        redirect.addFlashAttribute("error", "Loan rejected.");
        return "redirect:/manager/loans";
    }

    @GetMapping("/kyc")
    public String pendingKyc(Model model, HttpServletRequest request) {
        model.addAttribute("kycList", kycService.getKycsByStatus("PENDING"));
        model.addAttribute("currentUri", request.getRequestURI());
        return "manager-kyc-list";
    }

    @PostMapping("/kyc/approve")
    public String approveKyc(@RequestParam("id") Long id, RedirectAttributes redirect) {
        kycService.updateKycStatus(id, "VERIFIED");
        redirect.addFlashAttribute("success", "KYC Verified Successfully.");
        return "redirect:/manager/kyc";
    }

    @PostMapping("/kyc/reject")
    public String rejectKyc(@RequestParam("id") Long id, RedirectAttributes redirect) {
        kycService.updateKycStatus(id, "REJECTED");
        redirect.addFlashAttribute("error", "KYC Rejected.");
        return "redirect:/manager/kyc";
    }

    @GetMapping("/beneficiaries")
    public String pendingBeneficiaries(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("beneficiaries", beneficiaryService.getBeneficiariesByStatus("PENDING"));
        return "manager-beneficiaries";
    }

    @PostMapping("/beneficiaries/{id}/approve")
    public String forwardBeneficiary(@PathVariable Long id, RedirectAttributes redirect) {
        beneficiaryService.updateBeneficiaryStatus(id, "APPROVED");
        redirect.addFlashAttribute("success", "Beneficiary Verified Successfully.");
        return "redirect:/manager/beneficiaries";
    }

    @PostMapping("/beneficiaries/{id}/reject")
    public String rejectBeneficiary(@PathVariable Long id, RedirectAttributes redirect) {
        beneficiaryService.updateBeneficiaryStatus(id, "REJECTED");
        redirect.addFlashAttribute("error", "Beneficiary rejected.");
        return "redirect:/manager/beneficiaries";
    }

    @GetMapping("/services")
    public String pendingServices(Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("requests", cardService.getChequeRequestsByStatus("PENDING"));
        return "manager-service-requests";
    }

    @PostMapping("/services/approve")
    public String forwardService(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        cardService.updateChequeRequestStatus(id, "APPROVED");
        redirectAttributes.addFlashAttribute("success", "Request verified and approved.");
        return "redirect:/manager/services";
    }

    @PostMapping("/services/reject")
    public String rejectService(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        cardService.updateChequeRequestStatus(id, "REJECTED");
        redirectAttributes.addFlashAttribute("error", "Request Rejected.");
        return "redirect:/manager/services";
    }

    @GetMapping("/users")
    public String searchUsers(HttpServletRequest request, Model model, @RequestParam(value = "search", required = false) String search) {

        List<User> customer ;

        if(search != null && !search.isEmpty()){
            customer = userService.searchCustomers(search);
        } else {
            customer = userService.getAllUsers();
        }

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("users", customer);
        model.addAttribute("searchKeyword", search); // Add this to keep the input populated
        return "manager-users";
    }

    @GetMapping("/customers/create")
    public String showCustomerForm(Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("user", new User());
        return "manager-customer-create";
    }

    @PostMapping("/customers/create")
    public String createCustomer(@ModelAttribute User user, RedirectAttributes redirect) {
        try {

            user.setRole(Role.CUSTOMER);

            userService.saveUser(user);

            redirect.addFlashAttribute("success", "Customer '" + user.getUsername()
                    + "' onboarded successfully. You can now open accounts for them.");
            return "redirect:/manager/users";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Onboarding Failed: " + e.getMessage());
            return "redirect:/manager/customers/create";
        }
    }

    @GetMapping("/api/check-username")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(Collections.singletonMap("available", isAvailable));
    }

    @GetMapping("/api/check-email")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = userService.isEmailAvailable(email);
        return ResponseEntity.ok(Collections.singletonMap("available", isAvailable));
    }

    @GetMapping("/loans/{id}/download-sanction")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.InputStreamResource> downloadSanctionLetter(
            @PathVariable Long id) {

        mca.fincorebanking.entity.Loan loan = loanService.findById(id);

        if (!"APPROVED".equals(loan.getStatus())) {
            throw new RuntimeException("Loan is not approved yet.");
        }

        java.io.ByteArrayInputStream bis = pdfService.generateLoanSanctionLetter(loan);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=sanction-letter-" + id + ".pdf");

        return org.springframework.http.ResponseEntity
                .ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(new org.springframework.core.io.InputStreamResource(bis));
    }

}
