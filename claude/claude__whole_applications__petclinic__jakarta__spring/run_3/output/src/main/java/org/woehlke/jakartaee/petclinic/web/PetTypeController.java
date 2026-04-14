package org.woehlke.jakartaee.petclinic.web;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.pettype.PetType;
import org.woehlke.jakartaee.petclinic.pettype.db.PetTypeService;

import java.util.List;

@Log
@Controller
@RequestMapping("/petTypes")
public class PetTypeController {

    private final PetTypeService petTypeService;

    @Autowired
    public PetTypeController(PetTypeService petTypeService) {
        this.petTypeService = petTypeService;
    }

    @GetMapping
    public String listPetTypes(@RequestParam(value = "search", required = false) String search, Model model) {
        List<PetType> petTypes;
        if (search != null && !search.isEmpty()) {
            petTypes = petTypeService.search(search);
        } else {
            petTypes = petTypeService.getAll();
        }
        model.addAttribute("petTypes", petTypes);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Pet Types");
        return "petTypes/list";
    }

    @GetMapping("/{id}")
    public String showPetType(@PathVariable Long id, Model model) {
        PetType petType = petTypeService.findById(id);
        model.addAttribute("petType", petType);
        model.addAttribute("pageTitle", "Pet Type Details");
        return "petTypes/details";
    }
}
