package org.woehlke.jakartaee.petclinic.web;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;
import org.woehlke.jakartaee.petclinic.specialty.db.SpecialtyService;

import java.util.List;

@Log
@Controller
@RequestMapping("/specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @Autowired
    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @GetMapping
    public String listSpecialties(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Specialty> specialties;
        if (search != null && !search.isEmpty()) {
            specialties = specialtyService.search(search);
        } else {
            specialties = specialtyService.getAll();
        }
        model.addAttribute("specialties", specialties);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Specialties");
        return "specialties/list";
    }

    @GetMapping("/{id}")
    public String showSpecialty(@PathVariable Long id, Model model) {
        Specialty specialty = specialtyService.findById(id);
        model.addAttribute("specialty", specialty);
        model.addAttribute("pageTitle", "Specialty Details");
        return "specialties/details";
    }
}
