package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    public String createPetForm(@PathVariable Long ownerId, Model model) {
        Owner owner = ownerRepository.findById(ownerId).orElseThrow();
        model.addAttribute("owner", owner);
        model.addAttribute("pet", null);
        model.addAttribute("petTypes", petTypeRepository.findAll());
        model.addAttribute("errors", new HashMap<>());
        return "createOrUpdatePetForm";
    }

    @GetMapping("/owners/{ownerId}/pets/{petId}/edit")
    public String editPetForm(@PathVariable Long ownerId, @PathVariable Long petId, Model model) {
        Owner owner = ownerRepository.findById(ownerId).orElseThrow();
        Pet pet = petRepository.findById(petId).orElseThrow();
        model.addAttribute("owner", owner);
        model.addAttribute("pet", pet);
        model.addAttribute("petTypes", petTypeRepository.findAll());
        model.addAttribute("errors", new HashMap<>());
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
        pet.setType(petTypeRepository.findByName(type).orElseThrow(() ->
                new IllegalArgumentException("type not found: " + type)));

        Map<String, String> errors = new HashMap<>();
        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "must not be empty");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("owner", owner);
            model.addAttribute("pet", null);
            model.addAttribute("petTypes", petTypeRepository.findAll());
            model.addAttribute("errors", errors);
            return "createOrUpdatePetForm";
        }

        pet.setOwner(owner);
        petRepository.save(pet);
        owner.addPet(pet);

        model.addAttribute("owner", owner);
        return "ownerDetails";
    }

    @PostMapping("/owners/{ownerId}/pets/{petId}/edit")
    public String processUpdateForm(@PathVariable Long ownerId,
                                    @PathVariable Long petId,
                                    @RequestParam String name,
                                    @RequestParam(required = false) String birthDate,
                                    @RequestParam String type,
                                    Model model) {
        Map<String, String> errors = new HashMap<>();
        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "must not be empty");
        }

        if (!errors.isEmpty()) {
            Pet oldPet = petRepository.findById(petId).orElseThrow();
            model.addAttribute("owner", oldPet.getOwner());
            model.addAttribute("pet", oldPet);
            model.addAttribute("petTypes", petTypeRepository.findAll());
            model.addAttribute("errors", errors);
            return "createOrUpdatePetForm";
        }

        Pet existing = petRepository.findById(petId).orElseThrow();
        existing.setName(name);
        if (birthDate != null && !birthDate.trim().isEmpty()) {
            existing.setBirthDate(LocalDate.parse(birthDate));
        }
        existing.setType(petTypeRepository.findByName(type).orElseThrow(() ->
                new IllegalArgumentException("type not found: " + type)));

        Pet saved = petRepository.save(existing);
        Owner owner = ownerRepository.findById(ownerId).orElseThrow();
        model.addAttribute("owner", owner);
        return "ownerDetails";
    }
}
