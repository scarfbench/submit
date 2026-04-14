package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.List;

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
    public String createTemplate(@PathVariable Long ownerId, Model model) {
        Owner owner = ownerRepository.findById(ownerId).orElse(null);
        List<PetType> petTypes = petTypeRepository.findAll();
        model.addAttribute("owner", owner);
        model.addAttribute("pet", null);
        model.addAttribute("petTypes", petTypes);
        return "createOrUpdatePetForm";
    }

    @GetMapping("/owners/{ownerId}/pets/{petId}/edit")
    public String editTemplate(@PathVariable Long ownerId, @PathVariable Long petId, Model model) {
        Owner owner = ownerRepository.findById(ownerId).orElse(null);
        Pet pet = petRepository.findById(petId).orElse(null);
        List<PetType> petTypes = petTypeRepository.findAll();
        model.addAttribute("owner", owner);
        model.addAttribute("pet", pet);
        model.addAttribute("petTypes", petTypes);
        return "createOrUpdatePetForm";
    }

    @PostMapping("/owners/{ownerId}/pets/new")
    public String processCreationForm(@PathVariable Long ownerId,
                                      @RequestParam("name") String name,
                                      @RequestParam(value = "birthDate", required = false) String birthDateStr,
                                      @RequestParam("type") String typeName,
                                      Model model) {
        Owner owner = ownerRepository.findById(ownerId).orElse(null);

        Pet pet = new Pet();
        pet.setName(name);
        if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
            pet.setBirthDate(LocalDate.parse(birthDateStr));
        }
        PetType petType = petTypeRepository.findByName(typeName);
        pet.setType(petType);

        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("owner", owner);
            model.addAttribute("pet", null);
            model.addAttribute("petTypes", petTypeRepository.findAll());
            model.addAttribute("nameError", "must not be empty");
            return "createOrUpdatePetForm";
        }

        pet.setOwner(owner);
        petRepository.save(pet);
        owner.addPet(pet);
        model.addAttribute("owner", owner);
        return "ownerDetails";
    }

    @PostMapping("/owners/{ownerId}/pets/{petId}/edit")
    public String processUpdateForm(@PathVariable Long ownerId, @PathVariable Long petId,
                                    @RequestParam("name") String name,
                                    @RequestParam(value = "birthDate", required = false) String birthDateStr,
                                    @RequestParam("type") String typeName,
                                    Model model) {
        Pet pet = petRepository.findById(petId).orElse(null);

        if (name == null || name.trim().isEmpty()) {
            Owner owner = ownerRepository.findById(ownerId).orElse(null);
            model.addAttribute("owner", owner);
            model.addAttribute("pet", pet);
            model.addAttribute("petTypes", petTypeRepository.findAll());
            model.addAttribute("nameError", "must not be empty");
            return "createOrUpdatePetForm";
        }

        pet.setName(name);
        if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
            pet.setBirthDate(LocalDate.parse(birthDateStr));
        }
        PetType petType = petTypeRepository.findByName(typeName);
        pet.setType(petType);

        petRepository.save(pet);
        Owner owner = ownerRepository.findById(ownerId).orElse(null);
        model.addAttribute("owner", owner);
        return "ownerDetails";
    }
}
