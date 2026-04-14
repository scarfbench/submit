package jakarta.tutorial.concurrency.jobs;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.springframework.stereotype.Component;

@Component
@ApplicationPath("/webapi")
public class RestApplication extends Application { }
