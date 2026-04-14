package com.addressbook.controller;

import com.addressbook.entity.Contact;
import com.addressbook.service.ContactService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for Contact CRUD operations.
 * Provides JSON API endpoints at /api/contacts.
 */
@RestController
@RequestMapping("/api/contacts")
public class ContactRestController {

    private final ContactService contactService;

    public ContactRestController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public List<Contact> list() {
        return contactService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> get(@PathVariable Long id) {
        return contactService.find(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Contact> create(@RequestBody Contact contact) {
        contact.setId(null); // Ensure new entity
        Contact created = contactService.create(contact);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contact> update(@PathVariable Long id, @RequestBody Contact contact) {
        return contactService.find(id)
                .map(existing -> {
                    existing.setFirstName(contact.getFirstName());
                    existing.setLastName(contact.getLastName());
                    existing.setEmail(contact.getEmail());
                    existing.setMobilePhone(contact.getMobilePhone());
                    existing.setHomePhone(contact.getHomePhone());
                    existing.setBirthday(contact.getBirthday());
                    Contact updated = contactService.update(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return contactService.find(id)
                .map(existing -> {
                    contactService.delete(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
