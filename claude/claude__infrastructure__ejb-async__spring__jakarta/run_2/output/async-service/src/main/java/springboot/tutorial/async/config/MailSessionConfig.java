package springboot.tutorial.async.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.mail.Session;

import java.util.Properties;

/**
 * Mail session configuration for Jakarta EE.
 * Produces a mail session for CDI injection.
 */
@ApplicationScoped
public class MailSessionConfig {

    /**
     * Produces a mail session for injection.
     * Creates a programmatic mail session for the application.
     */
    @Produces
    @ApplicationScoped
    public Session createMailSession() {
        Properties p = new Properties();
        p.put("mail.smtp.host", "localhost");
        p.put("mail.smtp.port", "3025");
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "false");
        p.put("mail.from", "jack@localhost");

        return Session.getInstance(p, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication("jack", "changeMe");
            }
        });
    }
}