package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;

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
        Pet pet = petRepository.findById(petId).orElseThrow();
        // Load visits for the pet
        Collection<Visit> visits = visitRepository.findByPetId(petId);
        pet.setVisits(new LinkedHashSet<>(visits));
        model.addAttribute("pet", pet);
        model.addAttribute("visit", null);
        return "createOrUpdateVisitForm";
    }

    @PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
    public String processCreationForm(@PathVariable Long ownerId, @PathVariable Long petId,
                                       @RequestParam(required = false) String date,
                                       @RequestParam String description,
                                       Model model) {
        Pet pet = petRepository.findById(petId).orElseThrow();

        if (description == null || description.trim().isEmpty()) {
            Collection<Visit> visits = visitRepository.findByPetId(petId);
            pet.setVisits(new LinkedHashSet<>(visits));
            model.addAttribute("pet", pet);
            model.addAttribute("descriptionError", "must not be empty");
            return "createOrUpdateVisitForm";
        }

        Visit visit = new Visit();
        if (date != null && !date.trim().isEmpty()) {
            visit.setDate(LocalDate.parse(date));
        }
        visit.setDescription(description);
        visit.setPetId(petId);
        visitRepository.save(visit);

        Owner owner = ownerRepository.findById(ownerId).orElseThrow();
        // Load visits for all owner's pets
        for (Pet ownerPet : owner.getPets()) {
            Collection<Visit> petVisits = visitRepository.findByPetId(ownerPet.getId());
            ownerPet.setVisits(new LinkedHashSet<>(petVisits));
        }
        model.addAttribute("owner", owner);
        return "ownerDetails";
    }
}
