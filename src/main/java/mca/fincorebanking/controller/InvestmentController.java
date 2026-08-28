package mca.fincorebanking.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.FixedDeposit;
import mca.fincorebanking.entity.User;
import mca.fincorebanking.service.AccountService;
import mca.fincorebanking.service.InvestmentService;
import mca.fincorebanking.service.UserService;

@Controller
@RequestMapping("/investments")
public class InvestmentController {

    private final InvestmentService investmentService;
    private final UserService userService;
    private final AccountService accountService;

    public InvestmentController(InvestmentService investmentService,
            UserService userService,
            AccountService accountService) {
        this.investmentService = investmentService;
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping
    public String showInvestmentDashboard(Model model, Principal principal) {
        String username = principal.getName();
        List<FixedDeposit> investments = investmentService.getUserInvestments(username);

        model.addAttribute("investments", investments);
        model.addAttribute("username", username);

        return "investment-dashboard";
    }

    @GetMapping("/new")
    public String showCreateInvestmentForm(Model model, Principal principal) {
        String username = principal.getName();
        List<Account> accounts = accountService.findActiveByUsername(username);

        model.addAttribute("accounts", accounts);
        return "investment-create";
    }

    @PostMapping("/create")
    public String createInvestment(@RequestParam String accountNumber,
            @RequestParam Double amount,
            @RequestParam Integer tenure,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username);

            investmentService.createFixedDeposit(user.getId(), accountNumber, amount, tenure);

            redirectAttributes.addFlashAttribute("success", "Fixed Deposit created successfully!");
            return "redirect:/investments";

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/investments/new";
        }
    }
}