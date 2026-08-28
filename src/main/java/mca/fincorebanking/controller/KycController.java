package mca.fincorebanking.controller;

import mca.fincorebanking.entity.KycDocument;
import mca.fincorebanking.entity.User;
import mca.fincorebanking.service.KycService;
import mca.fincorebanking.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/kyc")
public class KycController {

    private final KycService kycService;
    private final UserService userService;

    public KycController(KycService kycService, UserService userService) {
        this.kycService = kycService;
        this.userService = userService;
    }

    @GetMapping
    public String showKycPage(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);

        Optional<KycDocument> existingKyc = kycService.getKycByUser(user);

        if (existingKyc.isPresent()) {
            model.addAttribute("kyc", existingKyc.get());
            return "kyc-status";
        } else {
            return "kyc-form";
        }
    }

    @PostMapping("/upload")
    public String uploadKyc(@RequestParam("docType") String docType,
            @RequestParam("docNumber") String docNumber,
            @RequestParam("file") MultipartFile file,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username);

            kycService.uploadKycDocument(user.getId(), docType, docNumber, file);

            redirectAttributes.addFlashAttribute("success",
                    "KYC Document uploaded successfully! Pending verification.");
            return "redirect:/kyc";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Upload failed: " + e.getMessage());
            return "redirect:/kyc";
        }
    }
}