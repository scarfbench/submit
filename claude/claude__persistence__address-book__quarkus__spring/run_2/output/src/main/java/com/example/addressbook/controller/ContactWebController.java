package com.example.addressbook.controller;

import com.example.addressbook.entity.Contact;
import com.example.addressbook.repository.ContactRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web controller for Contact HTML views using Thymeleaf.
 * Replaces the Quarkus JSF views with Spring MVC + Thymeleaf templates.
 */
@Controller
public class ContactWebController {

    private static final int PAGE_SIZE = 10;

    private final ContactRepository contactRepository;

    public ContactWebController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/contacts")
    public String listContacts(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Contact> contactPage = contactRepository.findAll(PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("contacts", contactPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contactPage.getTotalPages());
        model.addAttribute("totalItems", contactPage.getTotalElements());
        model.addAttribute("hasNext", contactPage.hasNext());
        model.addAttribute("hasPrevious", contactPage.hasPrevious());
        return "contact/list";
    }

    @GetMapping("/contacts/create")
    public String showCreateForm(Model model) {
        model.addAttribute("contact", new Contact());
        return "contact/create";
    }

    @PostMapping("/contacts/create")
    public String createContact(@ModelAttribute Contact contact, RedirectAttributes redirectAttributes) {
        contactRepository.save(contact);
        redirectAttributes.addFlashAttribute("message", "Contact was successfully created.");
        return "redirect:/contacts";
    }

    @GetMapping("/contacts/{id}")
    public String viewContact(@PathVariable Long id, Model model) {
        return contactRepository.findById(id)
                .map(contact -> {
                    model.addAttribute("contact", contact);
                    return "contact/view";
                })
                .orElse("redirect:/contacts");
    }

    @GetMapping("/contacts/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return contactRepository.findById(id)
                .map(contact -> {
                    model.addAttribute("contact", contact);
                    return "contact/edit";
                })
                .orElse("redirect:/contacts");
    }

    @PostMapping("/contacts/{id}/edit")
    public String updateContact(@PathVariable Long id, @ModelAttribute Contact contact, RedirectAttributes redirectAttributes) {
        if (!contactRepository.existsById(id)) {
            return "redirect:/contacts";
        }
        contact.setId(id);
        contactRepository.save(contact);
        redirectAttributes.addFlashAttribute("message", "Contact was successfully updated.");
        return "redirect:/contacts/" + id;
    }

    @PostMapping("/contacts/{id}/delete")
    public String deleteContact(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (contactRepository.existsById(id)) {
            contactRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Contact was successfully deleted.");
        }
        return "redirect:/contacts";
    }
}
