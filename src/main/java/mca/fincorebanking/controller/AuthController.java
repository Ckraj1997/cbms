package mca.fincorebanking.controller;

import mca.fincorebanking.entity.User;
import mca.fincorebanking.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {

        model.addAttribute("user", new User());

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("user") User user,
            RedirectAttributes redirectAttributes) {

        try {

            userService.registerCustomer(user);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Registration successful. Please login.");

            return "redirect:/login";

        } catch (Exception ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage());

            return "redirect:/register";
        }

    }
}
