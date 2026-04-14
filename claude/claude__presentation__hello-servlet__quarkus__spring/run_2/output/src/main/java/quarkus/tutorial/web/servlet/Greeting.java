package quarkus.tutorial.web.servlet;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/greeting")
public class Greeting {

    @GetMapping
    public ResponseEntity<String> greet(@RequestParam(required = false) String name) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: 'name' parameter is required");
        }

        String greeting = "Hello, " + name + "!";
        return ResponseEntity.ok(greeting);
    }
}
