package com.example.addressbook.controller;

import com.example.addressbook.entity.Contact;
import com.example.addressbook.service.ContactService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Contact operations.
 * Provides JSON endpoints for programmatic access and smoke testing.
 */
@RestController
@RequestMapping("/api/contacts")
public class ContactApiController {

    private final ContactService contactService;

    public ContactApiController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public List<Contact> getAllContacts() {
        return contactService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContact(@PathVariable Long id) {
        return contactService.find(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        Contact created = contactService.create(contact);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(@PathVariable Long id, @RequestBody Contact contact) {
        return contactService.find(id)
                .map(existing -> {
                    contact.setId(id);
                    return ResponseEntity.ok(contactService.update(contact));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        return contactService.find(id)
                .map(existing -> {
                    contactService.delete(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    public long getCount() {
        return contactService.count();
    }
}
