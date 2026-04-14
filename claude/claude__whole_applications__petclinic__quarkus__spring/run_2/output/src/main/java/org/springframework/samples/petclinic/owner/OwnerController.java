package org.springframework.samples.petclinic.owner;

import java.util.Collection;
import java.util.List;

import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;

@Controller
public class OwnerController {

    private final OwnerRepository ownerRepository;
    private final VisitRepository visitRepository;

    public OwnerController(OwnerRepository ownerRepository, VisitRepository visitRepository) {
        this.ownerRepository = ownerRepository;
        this.visitRepository = visitRepository;
    }

    @GetMapping("/owners/find")
    public String findTemplate(Model model) {
        return "findOwners";
    }

    @GetMapping("/owners/new")
    public String createTemplate(Model model) {
        model.addAttribute("owner", new Owner());
        return "createOrUpdateOwnerForm";
    }

    @GetMapping("/owners/{ownerId}/edit")
    public String editTemplate(@PathVariable Long ownerId, Model model) {
        Owner owner = ownerRepository.findById(ownerId).orElseThrow();
        model.addAttribute("owner", owner);
        return "createOrUpdateOwnerForm";
    }

    @GetMapping("/owners/{ownerId}")
    public String showOwner(@PathVariable Long ownerId, Model model) {
        Owner owner = ownerRepository.findById(ownerId).orElseThrow();
        loadVisitsForOwner(owner);
        model.addAttribute("owner", owner);
        return "ownerDetails";
    }

    @PostMapping("/owners/new")
    public String processCreationForm(@Valid Owner owner, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("owner", owner);
            return "createOrUpdateOwnerForm";
        }
        Owner saved = ownerRepository.save(owner);
        model.addAttribute("owner", saved);
        return "ownerDetails";
    }

    @PostMapping("/owners/{ownerId}/edit")
    public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result,
                                          @PathVariable Long ownerId, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("owner", owner);
            return "createOrUpdateOwnerForm";
        }
        owner.setId(ownerId);
        Owner saved = ownerRepository.save(owner);
        loadVisitsForOwner(saved);
        model.addAttribute("owner", saved);
        return "ownerDetails";
    }

    @GetMapping("/owners")
    public String processFindForm(@RequestParam(required = false) String lastName, Model model) {
        Collection<Owner> owners;

        if (lastName == null || lastName.trim().isEmpty()) {
            owners = ownerRepository.findAll();
        } else {
            owners = ownerRepository.findByLastName(lastName);
        }

        if (owners.isEmpty()) {
            model.addAttribute("noResults", true);
            return "findOwners";
        }
        if (owners.size() == 1) {
            Owner owner = owners.iterator().next();
            loadVisitsForOwner(owner);
            model.addAttribute("owner", owner);
            return "ownerDetails";
        }

        model.addAttribute("owners", owners);
        return "ownersList";
    }

    @GetMapping("/owners/api/list")
    @ResponseBody
    public List<Owner> listOwners() {
        return ownerRepository.findAll();
    }

    private void loadVisitsForOwner(Owner owner) {
        for (Pet pet : owner.getPets()) {
            Collection<Visit> visits = visitRepository.findByPetId(pet.getId());
            pet.setVisits(new java.util.LinkedHashSet<>(visits));
        }
    }
}
