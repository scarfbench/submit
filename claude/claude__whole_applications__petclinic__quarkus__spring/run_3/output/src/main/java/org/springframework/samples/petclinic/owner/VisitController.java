package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VisitController {

    private final PetRepository petRepository;
    private final VisitRepository visitRepository;
    private final OwnerRepository ownerRepository;

    public VisitController(PetRepository petRepository, VisitRepository visitRepository,
                           OwnerRepository ownerRepository) {
        this.petRepository = petRepository;
        this.visitRepository = visitRepository;
        this.ownerRepository = ownerRepository;
    }

    @GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
    public String createVisitForm(@PathVariable Long ownerId, @PathVariable Long petId, Model model) {
        Pet pet = petRepository.findById(petId).orElseThrow();
        pet.setVisits(new java.util.LinkedHashSet<>(visitRepository.findByPetId(petId)));
        model.addAttribute("pet", pet);
        model.addAttribute("visit", null);
        model.addAttribute("errors", new HashMap<>());
        return "createOrUpdateVisitForm";
    }

    @PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
    public String processCreationForm(@PathVariable Long ownerId,
                                      @PathVariable Long petId,
                                      @RequestParam(name = "date", required = false) String dateStr,
                                      @RequestParam(name = "description", required = false) String description,
                                      Model model) {
        Pet pet = petRepository.findById(petId).orElseThrow();
        Map<String, String> errors = new HashMap<>();

        if (description == null || description.trim().isEmpty()) {
            errors.put("description", "must not be empty");
        }

        Visit visit = new Visit();
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            visit.setDate(LocalDate.parse(dateStr));
        }
        visit.setDescription(description);
        visit.setPetId(petId);

        if (!errors.isEmpty()) {
            pet.setVisits(new java.util.LinkedHashSet<>(visitRepository.findByPetId(petId)));
            model.addAttribute("pet", pet);
            model.addAttribute("visit", visit);
            model.addAttribute("errors", errors);
            return "createOrUpdateVisitForm";
        }

        visitRepository.save(visit);
        pet.addVisit(visit);

        Owner owner = ownerRepository.findById(ownerId).orElseThrow();
        for (Pet p : owner.getPets()) {
            p.setVisits(new java.util.LinkedHashSet<>(visitRepository.findByPetId(p.getId())));
        }
        model.addAttribute("owner", owner);
        return "ownerDetails";
    }
}
