package com.example.addressbook.controller;

import com.example.addressbook.entity.Contact;
import com.example.addressbook.repository.ContactRepository;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring MVC controller for Contact CRUD operations.
 * Replaces the Jakarta EE JSF-based ContactController.
 */
@Controller
public class ContactController {

    private static final int PAGE_SIZE = 10;

    private final ContactRepository contactRepository;

    public ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/contact/list")
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Contact> contactPage = contactRepository.findAll(
                PageRequest.of(page, PAGE_SIZE, Sort.by("id")));
        model.addAttribute("contacts", contactPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contactPage.getTotalPages());
        model.addAttribute("totalItems", contactPage.getTotalElements());
        model.addAttribute("hasNext", contactPage.hasNext());
        model.addAttribute("hasPrevious", contactPage.hasPrevious());
        return "contact/list";
    }

    @GetMapping("/contact/create")
    public String createForm(Model model) {
        model.addAttribute("contact", new Contact());
        return "contact/create";
    }

    @PostMapping("/contact/create")
    public String create(@Valid @ModelAttribute Contact contact,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (result.hasErrors()) {
            return "contact/create";
        }
        contactRepository.save(contact);
        redirectAttributes.addFlashAttribute("successMessage", "Contact was successfully created.");
        return "redirect:/contact/list";
    }

    @GetMapping("/contact/view/{id}")
    public String view(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return contactRepository.findById(id)
                .map(contact -> {
                    model.addAttribute("contact", contact);
                    return "contact/view";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Contact not found.");
                    return "redirect:/contact/list";
                });
    }

    @GetMapping("/contact/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return contactRepository.findById(id)
                .map(contact -> {
                    model.addAttribute("contact", contact);
                    return "contact/edit";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Contact not found.");
                    return "redirect:/contact/list";
                });
    }

    @PostMapping("/contact/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Contact contact,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            contact.setId(id);
            return "contact/edit";
        }
        contact.setId(id);
        contactRepository.save(contact);
        redirectAttributes.addFlashAttribute("successMessage", "Contact was successfully updated.");
        return "redirect:/contact/view/" + id;
    }

    @PostMapping("/contact/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (contactRepository.existsById(id)) {
            contactRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Contact was successfully deleted.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Contact not found.");
        }
        return "redirect:/contact/list";
    }
}
