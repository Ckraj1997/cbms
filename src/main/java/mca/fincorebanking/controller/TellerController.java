package mca.fincorebanking.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.TellerTransaction;
import mca.fincorebanking.entity.User;
import mca.fincorebanking.service.AccountService;
import mca.fincorebanking.service.KycService;
import mca.fincorebanking.service.PdfService;
import mca.fincorebanking.service.TellerDrawerService;
import mca.fincorebanking.service.TellerService;
import mca.fincorebanking.service.TransactionService;
import mca.fincorebanking.service.UserService;

@Controller
@RequestMapping("/teller")
public class TellerController {

    private final TellerService tellerService;
    private final AccountService accountService;
    private final UserService userService;
    private final PdfService pdfService;
    private final TransactionService transactionService;
    private final KycService kycService;
    private final TellerDrawerService tellerDrawerService;

    public TellerController(TellerService tellerService, AccountService accountService, UserService userService,
            PdfService pdfService, TransactionService transactionService, KycService kycService, TellerDrawerService tellerDrawerService) {
        this.tellerService = tellerService;
        this.accountService = accountService;
        this.userService = userService;
        this.pdfService = pdfService;
        this.transactionService = transactionService;
        this.kycService = kycService;
        this.tellerDrawerService = tellerDrawerService;
    }

    @GetMapping
    public String showTellerDashboard(Model model, Principal principal,
            HttpServletRequest request,
            @RequestParam(required = false) String searchAccountNo) {

        String username = principal.getName();
        User teller = userService.findByUsername(username);

        Double liveCash = tellerDrawerService.getCurrentCash();
    model.addAttribute("cashInDrawer", liveCash);
    
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("userRole", "ROLE_TELLER");
        model.addAttribute("username", username);

        if (searchAccountNo != null && !searchAccountNo.isEmpty()) {
            try {
                Account customerAccount = accountService.findByAccountNumber(searchAccountNo);
                String blockReason = getTellerTransactionBlockReason(customerAccount);
                if (blockReason == null) {
                    model.addAttribute("customerAccount", customerAccount);
                } else {
                    model.addAttribute("error", blockReason);
                }
            } catch (RuntimeException ex) {
                model.addAttribute("error", "Account not found!");
            }

            model.addAttribute("searchAccountNo", searchAccountNo);
        }

        List<TellerTransaction> history = tellerService.getTellerHistory(teller.getId());
        model.addAttribute("todayTransactions", history);

        // model.addAttribute("cashInDrawer", NumericConfigStore.get().dashboard.tellerCashInDrawer);
        model.addAttribute("customersServed", history.size());

        return "dashboard";
    }

    private String getTellerTransactionBlockReason(Account account) {
        if (!"ACTIVE".equals(account.getStatus())) {
            return "Only active accounts can transact at the teller counter. Current account status: "
                    + account.getStatus();
        }

        boolean kycVerified = kycService.getKycByUser(account.getUser())
                .map(t -> t.getStatus())
                .filter("VERIFIED"::equals)
                .isPresent();

        if (!kycVerified) {
            return "Customer KYC is not verified. Teller transactions are allowed only for KYC verified customers.";
        }

        return null;
    }

    @GetMapping("/history")
    public String showTellerHistory(Model model, Principal principal, HttpServletRequest request) {
        String username = principal.getName();
        User teller = userService.findByUsername(username);

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("userRole", "ROLE_TELLER");
        model.addAttribute("username", username);

        List<TellerTransaction> history = tellerService.getTellerHistory(teller.getId());
        model.addAttribute("fullHistory", history);

        return "teller-history";
    }

    @PostMapping("/deposit")
    public String performDeposit(@RequestParam String accountNumber,
            @RequestParam Double amount,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            User teller = userService.findByUsername(principal.getName());
            tellerService.depositCash(teller.getId(), accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", "Cash Deposited Successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/teller?searchAccountNo=" + accountNumber;
    }

    @PostMapping("/withdraw")
    public String performWithdrawal(@RequestParam String accountNumber,
            @RequestParam Double amount,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            User teller = userService.findByUsername(principal.getName());
            tellerService.withdrawCash(teller.getId(), accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", "Cash Withdrawn Successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teller?searchAccountNo=" + accountNumber;
    }

    @GetMapping("/transaction/{id}/receipt")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.InputStreamResource> downloadReceipt(
            @PathVariable Long id) {

        mca.fincorebanking.entity.Transaction transaction = transactionService.findById(id);

        java.io.ByteArrayInputStream bis = pdfService.generateTransactionReceipt(transaction);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=receipt-" + id + ".pdf");

        return org.springframework.http.ResponseEntity
                .ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(new org.springframework.core.io.InputStreamResource(bis));
    }
}
