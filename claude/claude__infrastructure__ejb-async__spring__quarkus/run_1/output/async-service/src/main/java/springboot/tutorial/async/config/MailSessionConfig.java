package springboot.tutorial.async.config;

import jakarta.mail.Session;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Properties;

@ApplicationScoped
public class MailSessionConfig {

    @ConfigProperty(name = "quarkus.mailer.host", defaultValue = "localhost")
    String host;

    @ConfigProperty(name = "quarkus.mailer.port", defaultValue = "3025")
    int port;

    @ConfigProperty(name = "quarkus.mailer.username", defaultValue = "")
    String user;

    @ConfigProperty(name = "quarkus.mailer.password", defaultValue = "")
    String pass;

    @ConfigProperty(name = "mail.smtp.auth", defaultValue = "false")
    boolean auth;

    @ConfigProperty(name = "mail.smtp.starttls.enable", defaultValue = "false")
    boolean startTls;

    @ConfigProperty(name = "app.mail.from", defaultValue = "")
    String from;

    @Produces
    @ApplicationScoped
    public Session mailSession() {
        Properties p = new Properties();
        p.put("mail.smtp.host", host);
        p.put("mail.smtp.port", Integer.toString(port));
        p.put("mail.smtp.auth", Boolean.toString(auth));
        p.put("mail.smtp.starttls.enable", Boolean.toString(startTls));
        if (!from.isEmpty()) {
            p.put("mail.from", from); // so msg.setFrom() has a value
        }

        if (auth) {
            return Session.getInstance(p, new jakarta.mail.Authenticator() {
                @Override
                protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new jakarta.mail.PasswordAuthentication(user, pass);
                }
            });
        }
        return Session.getInstance(p);
    }
}