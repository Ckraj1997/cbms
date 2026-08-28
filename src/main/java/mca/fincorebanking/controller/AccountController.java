package mca.fincorebanking.controller;

import jakarta.servlet.http.HttpServletRequest;
import mca.fincorebanking.config.NumericConfigStore;
import mca.fincorebanking.entity.Account;
import mca.fincorebanking.service.AccountService;
import mca.fincorebanking.service.InterestService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final InterestService interestService;

    public AccountController(AccountService accountService,
            InterestService interestService) {
        this.accountService = accountService;
        this.interestService = interestService;
    }

    @GetMapping
    public String accountList(Authentication auth, Model model, HttpServletRequest request) {
        model.addAttribute(
                "accounts",
                accountService.findByUsername(auth.getName()));
        model.addAttribute("currentUri", request.getRequestURI());

        return "account-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("account", new Account());
        return "account-create";
    }

    @PostMapping("/create")
    public String requestAccount(@ModelAttribute Account account,
            Authentication auth) {

        accountService.requestAccount(account, auth.getName());
        return "redirect:/accounts";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id,
            Model model) {

        model.addAttribute("account",
                accountService.getAccountById(id));

        model.addAttribute("recentTransactions",
                accountService.getRecentTransactions(id));

        model.addAttribute("notifications",
                accountService.getAccountNotifications(id));

        model.addAttribute("currentUri", "/accounts");

        return "account-details";
    }

    @GetMapping("/interest/{id}")
    public String viewInterest(@PathVariable Long id,
            Model model) {

        Account account = accountService.getAccountById(id);

        int months = NumericConfigStore.get().account.defaultInterestMonths;

        double interest = interestService.calculateSavingsInterest(account, months);

        model.addAttribute("account", account);
        model.addAttribute("months", months);
        model.addAttribute("interest", interest);
        model.addAttribute("totalAmount",
                account.getBalance() + interest);

        return "account-interest";
    }

}
