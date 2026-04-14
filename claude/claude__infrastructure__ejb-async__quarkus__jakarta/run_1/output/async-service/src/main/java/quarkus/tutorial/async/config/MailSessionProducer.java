package quarkus.tutorial.async.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.util.Properties;

@ApplicationScoped
public class MailSessionProducer {

    @Produces
    @ApplicationScoped
    jakarta.mail.Session mailSession() {
        Properties p = new Properties();
        p.put("mail.smtp.host", System.getProperty("mail.smtp.host", "localhost"));
        p.put("mail.smtp.port", System.getProperty("mail.smtp.port", "3025"));
        p.put("mail.smtp.auth", System.getProperty("mail.smtp.auth", "true"));
        p.put("mail.smtp.starttls.enable", System.getProperty("mail.smtp.starttls.enable", "false"));

        String user = System.getProperty("mail.smtp.user", "jack");
        String pass = System.getProperty("mail.smtp.password", "changeMe");

        return jakarta.mail.Session.getInstance(p, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(user, pass);
            }
        });
    }
}