package com.addressbook.controller;

import com.addressbook.entity.Contact;
import com.addressbook.service.ContactService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring MVC Controller for web UI.
 * Replaces the JSF ContactController with Thymeleaf-based views.
 * Preserves the same CRUD functionality and pagination.
 */
@Controller
@RequestMapping("/contacts")
public class ContactWebController {

    private static final int PAGE_SIZE = 10;

    private final ContactService contactService;

    public ContactWebController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Contact> contactPage = contactService.findAll(PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("contacts", contactPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contactPage.getTotalPages());
        model.addAttribute("totalItems", contactPage.getTotalElements());
        model.addAttribute("hasPrevious", contactPage.hasPrevious());
        model.addAttribute("hasNext", contactPage.hasNext());
        return "contact/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        return contactService.find(id)
                .map(contact -> {
                    model.addAttribute("contact", contact);
                    return "contact/view";
                })
                .orElse("redirect:/contacts");
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("contact", new Contact());
        return "contact/create";
    }

    @PostMapping
    public String create(@ModelAttribute Contact contact, RedirectAttributes redirectAttributes) {
        try {
            contact.setId(null);
            contactService.create(contact);
            redirectAttributes.addFlashAttribute("successMessage", "Contact was successfully created.");
            return "redirect:/contacts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "A persistence error occurred.");
            return "redirect:/contacts/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        return contactService.find(id)
                .map(contact -> {
                    model.addAttribute("contact", contact);
                    return "contact/edit";
                })
                .orElse("redirect:/contacts");
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Contact contact,
                         RedirectAttributes redirectAttributes) {
        try {
            return contactService.find(id)
                    .map(existing -> {
                        existing.setFirstName(contact.getFirstName());
                        existing.setLastName(contact.getLastName());
                        existing.setEmail(contact.getEmail());
                        existing.setMobilePhone(contact.getMobilePhone());
                        existing.setHomePhone(contact.getHomePhone());
                        existing.setBirthday(contact.getBirthday());
                        contactService.update(existing);
                        redirectAttributes.addFlashAttribute("successMessage",
                                "Contact was successfully updated.");
                        return "redirect:/contacts/" + id;
                    })
                    .orElse("redirect:/contacts");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "A persistence error occurred.");
            return "redirect:/contacts/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contactService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Contact was successfully deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "A persistence error occurred.");
        }
        return "redirect:/contacts";
    }
}
