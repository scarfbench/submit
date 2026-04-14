package org.woehlke.jakartaee.petclinic.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Welcome to Petclinic");
        return "home";
    }

    @GetMapping("/info")
    public String info(Model model) {
        model.addAttribute("pageTitle", "Information");
        return "info";
    }
}
