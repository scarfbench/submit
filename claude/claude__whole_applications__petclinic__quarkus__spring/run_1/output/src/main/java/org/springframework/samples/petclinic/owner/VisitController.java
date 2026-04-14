package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;

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
    public String createTemplate(@PathVariable Long ownerId, @PathVariable Long petId, Model model) {
        Pet pet = petRepository.findById(petId).orElse(null);
        model.addAttribute("pet", pet);
        model.addAttribute("visit", null);
        return "createOrUpdateVisitForm";
    }

    @PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
    public String processCreationForm(@PathVariable Long ownerId, @PathVariable Long petId,
                                      @RequestParam(value = "date", required = false) String dateStr,
                                      @RequestParam(value = "description", required = false) String description,
                                      Model model) {
        Pet pet = petRepository.findById(petId).orElse(null);

        if (description == null || description.trim().isEmpty()) {
            model.addAttribute("pet", pet);
            model.addAttribute("visit", null);
            model.addAttribute("descriptionError", "must not be empty");
            return "createOrUpdateVisitForm";
        }

        Visit visit = new Visit();
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            visit.setDate(LocalDate.parse(dateStr));
        }
        visit.setDescription(description);
        visit.setPetId(petId);
        visitRepository.save(visit);

        pet.addVisit(visit);

        Owner owner = ownerRepository.findById(ownerId).orElse(null);
        model.addAttribute("owner", owner);
        return "ownerDetails";
    }
}
