package com.example.addressbook.controller;

import com.example.addressbook.entity.Contact;
import com.example.addressbook.service.ContactService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Spring MVC Controller for Contact web pages.
 * Replaces the Quarkus JSF ContactController.
 */
@Controller
@RequestMapping("/contact")
public class ContactController {

    private static final int PAGE_SIZE = 10;
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping({"", "/", "/List"})
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Contact> contactPage = contactService.findPage(page, PAGE_SIZE);
        model.addAttribute("contacts", contactPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contactPage.getTotalPages());
        model.addAttribute("totalItems", contactPage.getTotalElements());
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("hasNext", contactPage.hasNext());
        model.addAttribute("hasPrevious", contactPage.hasPrevious());
        return "contact/List";
    }

    @GetMapping("/View/{id}")
    public String view(@PathVariable Long id, Model model) {
        Contact contact = contactService.find(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        model.addAttribute("contact", contact);
        return "contact/View";
    }

    @GetMapping("/Create")
    public String createForm(Model model) {
        model.addAttribute("contact", new Contact());
        return "contact/Create";
    }

    @PostMapping("/Create")
    public String create(@ModelAttribute Contact contact,
                         @RequestParam(required = false) String birthdayStr,
                         RedirectAttributes redirectAttributes) {
        try {
            if (birthdayStr != null && !birthdayStr.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                contact.setBirthday(sdf.parse(birthdayStr));
            }
            contactService.create(contact);
            redirectAttributes.addFlashAttribute("successMessage", "Contact was successfully created.");
            return "redirect:/contact/List";
        } catch (ParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid date format. Use MM/dd/yyyy.");
            return "redirect:/contact/Create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "A persistence error occurred.");
            return "redirect:/contact/Create";
        }
    }

    @GetMapping("/Edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Contact contact = contactService.find(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        model.addAttribute("contact", contact);
        return "contact/Edit";
    }

    @PostMapping("/Edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Contact contact,
                         @RequestParam(required = false) String birthdayStr,
                         RedirectAttributes redirectAttributes) {
        try {
            contact.setId(id);
            if (birthdayStr != null && !birthdayStr.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                contact.setBirthday(sdf.parse(birthdayStr));
            }
            contactService.update(contact);
            redirectAttributes.addFlashAttribute("successMessage", "Contact was successfully updated.");
            return "redirect:/contact/View/" + id;
        } catch (ParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid date format. Use MM/dd/yyyy.");
            return "redirect:/contact/Edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "A persistence error occurred.");
            return "redirect:/contact/Edit/" + id;
        }
    }

    @PostMapping("/Delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contactService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Contact was successfully deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "A persistence error occurred.");
        }
        return "redirect:/contact/List";
    }
}
