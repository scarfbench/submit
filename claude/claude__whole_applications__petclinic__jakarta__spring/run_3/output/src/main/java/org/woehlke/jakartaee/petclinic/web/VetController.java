package org.woehlke.jakartaee.petclinic.web;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.vet.Vet;
import org.woehlke.jakartaee.petclinic.vet.db.VetService;

import java.util.List;

@Log
@Controller
@RequestMapping("/vets")
public class VetController {

    private final VetService vetService;

    @Autowired
    public VetController(VetService vetService) {
        this.vetService = vetService;
    }

    @GetMapping
    public String listVets(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Vet> vets;
        if (search != null && !search.isEmpty()) {
            vets = vetService.search(search);
        } else {
            vets = vetService.getAll();
        }
        model.addAttribute("vets", vets);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Veterinarians");
        return "vets/list";
    }

    @GetMapping("/{id}")
    public String showVet(@PathVariable Long id, Model model) {
        Vet vet = vetService.findById(id);
        model.addAttribute("vet", vet);
        model.addAttribute("pageTitle", "Veterinarian Details");
        return "vets/details";
    }
}
