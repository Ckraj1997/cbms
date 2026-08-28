package mca.fincorebanking.controller;

import jakarta.servlet.http.HttpServletRequest;
import mca.fincorebanking.service.SuperAdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/super-admin")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    public SuperAdminController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
    }

    @GetMapping
    public String dashboard(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("userRole", "ROLE_SUPER_ADMIN");
        model.addAttribute("stats", superAdminService.getSystemStats());
        return "dashboard";
    }

    @GetMapping("/sql")
    public String sqlConsole(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        return "super-admin-sql";
    }

    @PostMapping("/sql/execute")
    public String executeQuery(@RequestParam("query") String query, Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("lastQuery", query);

        try {
            List<Map<String, Object>> results = superAdminService.executeSql(query);
            model.addAttribute("results", results);
            if (!results.isEmpty()) {
                model.addAttribute("columns", results.get(0).keySet());
            }
            model.addAttribute("success", "Query executed successfully. Rows returned: " + results.size());
        } catch (Exception e) {
            model.addAttribute("error", "SQL Error: " + e.getMessage());
        }

        return "super-admin-sql";
    }
}