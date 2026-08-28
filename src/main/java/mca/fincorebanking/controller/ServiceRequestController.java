package mca.fincorebanking.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.ChequeBookRequest;
import mca.fincorebanking.entity.DebitCard;
import mca.fincorebanking.service.AccountService;
import mca.fincorebanking.service.CardService;

@Controller
@RequestMapping("/services")
public class ServiceRequestController {

    private final CardService cardService;
    private final AccountService accountService;

    public ServiceRequestController(CardService cardService, AccountService accountService) {
        this.cardService = cardService;
        this.accountService = accountService;
    }

    @GetMapping
    public String showServicesPage(Model model, Principal principal) {
        String username = principal.getName();

        List<Account> accounts = accountService.findActiveByUsername(username);

        Map<Long, DebitCard> accountCards = new HashMap<>();
        Map<Long, List<ChequeBookRequest>> accountCheques = new HashMap<>();

        for (Account acc : accounts) {

            DebitCard card = cardService.getCardByAccountId(acc.getId());
            if (card != null) {
                accountCards.put(acc.getId(), card);
            }

            List<ChequeBookRequest> cheques = cardService.getChequeRequestsByAccount(acc.getId());
            accountCheques.put(acc.getId(), cheques);
        }

        model.addAttribute("accounts", accounts);
        model.addAttribute("accountCards", accountCards);
        model.addAttribute("accountCheques", accountCheques);

        return "service-request";
    }

    @PostMapping("/card/issue")
    public String issueCard(@RequestParam Long accountId, RedirectAttributes redirectAttributes) {
        try {
            cardService.issueCard(accountId);
            redirectAttributes.addFlashAttribute("success", "Debit Card issued successfully! It is now Active.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to issue card: " + e.getMessage());
        }
        return "redirect:/services";
    }

    @PostMapping("/card/toggle")
    public String toggleCard(@RequestParam Long cardId, RedirectAttributes redirectAttributes) {
        try {
            cardService.toggleCardStatus(cardId);
            redirectAttributes.addFlashAttribute("success", "Card status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating card: " + e.getMessage());
        }
        return "redirect:/services";
    }

    @PostMapping("/cheque/request")
    public String requestChequeBook(@RequestParam Long accountId,
            @RequestParam String leaves,
            RedirectAttributes redirectAttributes) {
        try {
            cardService.requestChequeBook(accountId, leaves);
            redirectAttributes.addFlashAttribute("success",
                    "Cheque Book requested successfully! Waiting for Admin approval.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Request failed: " + e.getMessage());
        }
        return "redirect:/services";
    }
}