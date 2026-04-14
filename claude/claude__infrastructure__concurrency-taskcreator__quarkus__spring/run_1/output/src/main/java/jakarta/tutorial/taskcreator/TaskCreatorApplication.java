package jakarta.tutorial.taskcreator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class TaskCreatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskCreatorApplication.class, args);
    }
}
