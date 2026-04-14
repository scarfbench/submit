package jakarta.tutorial.web.servlet;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Greeting {

    @GetMapping("/greeting")
    public ResponseEntity<String> doGet(@RequestParam(required = false) String name) {

        if (name == null || name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String greeting = "Hello, " + name + "!";

        return ResponseEntity.ok()
                .header("Content-Type", "text/plain")
                .body(greeting);
    }
}
