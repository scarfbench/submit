package jakarta.tutorial.web.servlet;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Greeting {

    @GetMapping(value = "/greeting", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> doGet(@RequestParam(name = "name", required = false) String name) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        var greeting = "Hello, " + name + "!";
        return ResponseEntity.ok(greeting);
    }
}