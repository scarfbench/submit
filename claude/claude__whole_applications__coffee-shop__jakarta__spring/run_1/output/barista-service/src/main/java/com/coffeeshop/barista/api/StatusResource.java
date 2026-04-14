package com.coffeeshop.barista.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/status")
public class StatusResource {
    @GetMapping
    public String ok() {
        return "barista ok";
    }
}
