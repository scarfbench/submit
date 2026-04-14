package jakarta.tutorial.async.config;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.mail.Session;

@ApplicationScoped
public class MailSessionConfig {

    @Produces
    @Resource(lookup = "java:jboss/mail/Default")
    private Session mailSession;
}