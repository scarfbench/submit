package org.woehlke.jakartaee.petclinic.web;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.owner.Owner;
import org.woehlke.jakartaee.petclinic.owner.db.OwnerService;

import java.util.List;

@Log
@Controller
@RequestMapping("/owners")
public class OwnerController {

    private final OwnerService ownerService;

    @Autowired
    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GetMapping
    public String listOwners(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Owner> owners;
        if (search != null && !search.isEmpty()) {
            owners = ownerService.search(search);
        } else {
            owners = ownerService.getAll();
        }
        model.addAttribute("owners", owners);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Owners");
        return "owners/list";
    }

    @GetMapping("/{id}")
    public String showOwner(@PathVariable Long id, Model model) {
        Owner owner = ownerService.findById(id);
        model.addAttribute("owner", owner);
        model.addAttribute("pets", ownerService.getPetsAsList(owner));
        model.addAttribute("pageTitle", "Owner Details");
        return "owners/details";
    }
}
