package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PetController {

    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final PetTypeRepository petTypeRepository;

    public PetController(OwnerRepository ownerRepository, PetRepository petRepository,
                         PetTypeRepository petTypeRepository) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.petTypeRepository = petTypeRepository;
    }

    @GetMapping("/owners/{ownerId}/pets/new")
    public String createTemplate(@PathVariable Long ownerId, Model model) {
        Owner owner = ownerRepository.findById(ownerId).orElseThrow();
        List<PetType> petTypes = petTypeRepository.findAll();
        model.addAttribute("owner", owner);
        model.addAttribute("pet", null);
        model.addAttribute("petTypes", petTypes);
        return "createOrUpdatePetForm";
    }

    @GetMapping("/owners/{ownerId}/pets/{petId}/edit")
    public String editTemplate(@PathVariable Long ownerId, @PathVariable Long petId, Model model) {
        Owner owner = ownerRepository.findById(ownerId).orElseThrow();
        Pet pet = petRepository.findById(petId).orElseThrow();
        List<PetType> petTypes = petTypeRepository.findAll();
        model.addAttribute("owner", owner);
        model.addAttribute("pet", pet);
        model.addAttribute("petTypes", petTypes);
        return "createOrUpdatePetForm";
    }

    @PostMapping("/owners/{ownerId}/pets/new")
    public String processCreationForm(@PathVariable Long ownerId,
                                       @RequestParam String name,
                                       @RequestParam(required = false) String birthDate,
                                       @RequestParam String type,
                                       Model model) {
        Owner owner = ownerRepository.findById(ownerId).orElseThrow();

        Pet pet = new Pet();
        pet.setName(name);
        if (birthDate != null && !birthDate.trim().isEmpty()) {
            pet.setBirthDate(LocalDate.parse(birthDate));
        }
        PetType petType = petTypeRepository.findByName(type)
                .orElseThrow(() -> new IllegalArgumentException("type not found: " + type));
        pet.setType(petType);

        if (name == null || name.trim().isEmpty()) {
            List<PetType> petTypes = petTypeRepository.findAll();
            model.addAttribute("owner", owner);
            model.addAttribute("pet", null);
            model.addAttribute("petTypes", petTypes);
            model.addAttribute("nameError", "must not be empty");
            return "createOrUpdatePetForm";
        }

        pet.setOwner(owner);
        petRepository.save(pet);
        owner.addPet(pet);

        loadOwnerForDetails(owner, model);
        return "ownerDetails";
    }

    @PostMapping("/owners/{ownerId}/pets/{petId}/edit")
    public String processUpdateForm(@PathVariable Long ownerId, @PathVariable Long petId,
                                     @RequestParam String name,
                                     @RequestParam(required = false) String birthDate,
                                     @RequestParam String type,
                                     Model model) {
        Pet pet = petRepository.findById(petId).orElseThrow();
        pet.setName(name);
        if (birthDate != null && !birthDate.trim().isEmpty()) {
            pet.setBirthDate(LocalDate.parse(birthDate));
        }
        PetType petType = petTypeRepository.findByName(type)
                .orElseThrow(() -> new IllegalArgumentException("type not found: " + type));
        pet.setType(petType);

        if (name == null || name.trim().isEmpty()) {
            Owner owner = ownerRepository.findById(ownerId).orElseThrow();
            List<PetType> petTypes = petTypeRepository.findAll();
            model.addAttribute("owner", owner);
            model.addAttribute("pet", pet);
            model.addAttribute("petTypes", petTypes);
            model.addAttribute("nameError", "must not be empty");
            return "createOrUpdatePetForm";
        }

        petRepository.save(pet);
        Owner owner = ownerRepository.findById(ownerId).orElseThrow();
        loadOwnerForDetails(owner, model);
        return "ownerDetails";
    }

    private void loadOwnerForDetails(Owner owner, Model model) {
        model.addAttribute("owner", owner);
    }
}
