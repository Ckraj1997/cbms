package mca.fincorebanking.controller;

import jakarta.servlet.http.HttpServletRequest;
import mca.fincorebanking.entity.Beneficiary;
import mca.fincorebanking.service.BeneficiaryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @GetMapping
    public String beneficiaryList(HttpServletRequest request,
            Model model,
            Authentication authentication) {

        model.addAttribute("currentUri", request.getRequestURI());

        model.addAttribute(
                "beneficiaries",
                beneficiaryService.getBeneficiaries(
                        authentication.getName()));

        return "beneficiary-list";
    }

    @GetMapping("/add")
    public String addBeneficiaryForm(Model model) {
        model.addAttribute("beneficiary", new Beneficiary());
        return "beneficiary-add";
    }

    @PostMapping("/add")
    public String addBeneficiary(@ModelAttribute Beneficiary beneficiary,
            Authentication authentication) {

        beneficiaryService.addBeneficiary(
                beneficiary,
                authentication.getName());

        return "redirect:/beneficiaries";
    }
}
