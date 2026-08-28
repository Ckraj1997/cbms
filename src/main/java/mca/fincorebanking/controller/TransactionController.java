package mca.fincorebanking.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mca.fincorebanking.dto.TransactionReceiptDTO;
import mca.fincorebanking.entity.Account;
import mca.fincorebanking.entity.Transaction;
import mca.fincorebanking.service.AccountService;
import mca.fincorebanking.service.BeneficiaryService;
import mca.fincorebanking.service.TransactionService;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final BeneficiaryService beneficiaryService;
    private final AccountService accountService;

    public TransactionController(TransactionService transactionService,
            BeneficiaryService beneficiaryService,
            AccountService accountService) {
        this.transactionService = transactionService;
        this.beneficiaryService = beneficiaryService;
        this.accountService = accountService;
    }

    @GetMapping
    public String transactionList(
            HttpServletRequest request,
            Model model,
            Authentication auth,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        String username = auth.getName();

        List<Account> accounts = accountService.findActiveByUsername(username);
        model.addAttribute("accounts", accounts);

        if (type != null && type.isBlank()) {
            type = null;
        }

        if (accountId == null) {
            model.addAttribute("txPage", Page.empty());
            model.addAttribute("selectedAccountId", null);
            model.addAttribute("currentUri", request.getRequestURI());
            return "transaction-list";
        }

        LocalDateTime fromDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;

        LocalDateTime toDateTime = (toDate != null) ? toDate.atTime(23, 59, 59) : null;

        Page<Transaction> txPage = transactionService.getTransactions(
                accountId,
                type,
                fromDateTime,
                toDateTime,
                page,
                size);

        model.addAttribute("txPage", txPage);
        model.addAttribute("selectedAccountId", accountId);
        model.addAttribute("currentUri", request.getRequestURI());

        return "transaction-list";
    }

    @GetMapping("/new")
    public String selfTransactionPage(Authentication auth, Model model, HttpServletRequest request) {

        String username = auth.getName();

        model.addAttribute("accounts",
                accountService.findActiveByUsername(username));

        model.addAttribute("currentUri", request.getRequestURI());

        return "transaction-form";
    }

    @PostMapping("/new")
    public String processSelfTransaction(
            Authentication auth,
            @RequestParam Long accountId,
            @RequestParam String type,
            @RequestParam Double amount,
            RedirectAttributes redirect) {

        try {
            transactionService.processSelfTransaction(
                    auth.getName(),
                    accountId,
                    type,
                    amount);
            redirect.addFlashAttribute("success", "Transaction successful");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/transactions/new";
    }

    @GetMapping("/transfer")
    public String transferForm(Authentication auth, Model model, HttpServletRequest request) {

        String username = auth.getName();

        model.addAttribute("accounts",
                accountService.findActiveByUsername(username));

        model.addAttribute("beneficiaries",
                beneficiaryService.findApprovedByUsername(username));

        model.addAttribute("currentUri", request.getRequestURI());

        return "fund-transfer";
    }

    @PostMapping("/transfer")
    public String transfer(
            Authentication auth,
            @RequestParam Long accountId,
            @RequestParam Long beneficiaryId,
            @RequestParam Double amount,
            RedirectAttributes redirect) {

        String username = auth.getName();

        if (amount <= 0) {
            redirect.addFlashAttribute("error", "Invalid transfer amount");
            return "redirect:/transactions/transfer";
        }

        try {
            TransactionReceiptDTO receipt = transactionService.transfer(
                    username,
                    accountId,
                    beneficiaryId,
                    amount);
            redirect.addFlashAttribute("receipt", receipt);
            return "redirect:/transactions/receipt";
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/transactions/transfer";
        }

    }

    @GetMapping("/receipt")
    public String receiptPage(
            Model model) {
        return "transaction-receipt";
    }

    @GetMapping("/export/csv")
    public void exportCsv(
            Authentication auth,
            @RequestParam Long accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            HttpServletResponse response) throws IOException {

        if (type != null && type.isBlank()) {
            type = null;
        }

        response.setContentType("text/csv");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=transaction_statement.csv");

        List<Transaction> transactions = transactionService.getAllTransactions(
                accountId,
                type,
                fromDate != null ? fromDate.atStartOfDay() : null,
                toDate != null ? toDate.atTime(23, 59, 59) : null);

        PrintWriter writer = response.getWriter();

        writer.println("Account Number,Date,Type,Amount,Balance After");

        for (Transaction tx : transactions) {
            writer.printf(
                    "%s,%s,%s,%.2f,%.2f%n",
                    tx.getAccount().getAccountNumber(),
                    tx.getTransactionTime(),
                    tx.getType(),
                    tx.getAmount(),
                    tx.getBalanceAfter());
        }

        writer.flush();
    }

    @GetMapping("/export/pdf")
    public void exportPdf(
            Authentication auth,
            @RequestParam Long accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            HttpServletResponse response) throws Exception {

        if (type != null && type.isBlank()) {
            type = null;
        }

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=transaction_statement.pdf");

        List<Transaction> transactions = transactionService.getAllTransactions(
                accountId,
                type,
                fromDate != null ? fromDate.atStartOfDay() : null,
                toDate != null ? toDate.atTime(23, 59, 59) : null);

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        document.add(new Paragraph("Transaction Statement"));
        document.add(new Paragraph(" "));

        for (Transaction tx : transactions) {
            document.add(new Paragraph(
                    "Account: " + tx.getAccount().getAccountNumber()
                            + " | Date: " + tx.getTransactionTime()
                            + " | Type: " + tx.getType()
                            + " | Amount: " + tx.getAmount()
                            + " | Balance: " + tx.getBalanceAfter()));
        }

        document.close();
    }

}
