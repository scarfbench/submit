package org.springframework.samples.petclinic.owner;

import java.util.Collection;
import java.util.List;

import jakarta.validation.Valid;
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
        Owner owner = ownerRepository.findById(ownerId).orElse(null);
        model.addAttribute("owner", owner);
        return "createOrUpdateOwnerForm";
    }

    @GetMapping("/owners/{ownerId}")
    public String showOwner(@PathVariable Long ownerId, Model model) {
        Owner owner = ownerRepository.findById(ownerId).orElseThrow(() ->
            new RuntimeException("Owner not found with id: " + ownerId));
        setVisits(owner);
        model.addAttribute("owner", owner);
        return "ownerDetails";
    }

    @PostMapping("/owners/new")
    public String processCreationForm(@Valid Owner owner, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("owner", owner);
            return "createOrUpdateOwnerForm";
        }
        Owner savedOwner = ownerRepository.save(owner);
        setVisits(savedOwner);
        model.addAttribute("owner", savedOwner);
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
        // Preserve the existing pets
        Owner existingOwner = ownerRepository.findById(ownerId).orElse(null);
        if (existingOwner != null) {
            owner.setPets(existingOwner.getPets());
        }
        Owner savedOwner = ownerRepository.save(owner);
        setVisits(savedOwner);
        model.addAttribute("owner", savedOwner);
        return "ownerDetails";
    }

    @GetMapping("/owners")
    public String processFindForm(@RequestParam(name = "lastName", required = false) String lastName, Model model) {
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
            setVisits(owner);
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

    private void setVisits(Owner owner) {
        for (Pet pet : owner.getPets()) {
            Collection<Visit> visits = visitRepository.findByPetId(pet.getId());
            pet.setVisits(visits);
        }
    }
}
