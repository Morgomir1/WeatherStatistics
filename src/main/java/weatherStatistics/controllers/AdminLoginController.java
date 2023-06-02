package weatherStatistics.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import weatherStatistics.entity.Admin;
import weatherStatistics.repo.AdminRepo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminLoginController {

    @GetMapping("/adminRedirect")
    public String registrationRedirect() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/admin";
        } else {
            return "redirect:/login";
        }
    }

    @PostMapping("/logOut")
    public String logOut() {
        SecurityContextHolder.clearContext();
        return "redirect:/main";
    }
}