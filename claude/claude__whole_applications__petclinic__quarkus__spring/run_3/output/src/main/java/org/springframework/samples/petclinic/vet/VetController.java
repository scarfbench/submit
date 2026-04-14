package org.springframework.samples.petclinic.vet;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class VetController {

    private final VetRepository vetRepository;

    public VetController(VetRepository vetRepository) {
        this.vetRepository = vetRepository;
    }

    @GetMapping("/vets.html")
    public String showVetPage(Model model) {
        List<Vet> vets = vetRepository.findAll();
        model.addAttribute("vets", vets);
        return "vetList";
    }

    @GetMapping("/vets")
    @ResponseBody
    public Vets showVetList() {
        Vets vets = new Vets();
        vets.getVetList().addAll(vetRepository.findAll());
        return vets;
    }
}
