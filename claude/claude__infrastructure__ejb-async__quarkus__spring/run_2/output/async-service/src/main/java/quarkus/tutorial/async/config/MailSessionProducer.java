package quarkus.tutorial.async.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Properties;

@Configuration
public class MailSessionProducer {

    @Value("${spring.mail.host:localhost}")
    private String host;

    @Value("${spring.mail.port:3025}")
    private String port;

    @Value("${spring.mail.username:jack}")
    private String username;

    @Value("${spring.mail.password:changeMe}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private String auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private String starttls;

    @Bean
    public jakarta.mail.Session mailSession() {
        Properties p = new Properties();
        p.put("mail.smtp.host", host);
        p.put("mail.smtp.port", port);
        p.put("mail.smtp.auth", auth);
        p.put("mail.smtp.starttls.enable", starttls);

        return jakarta.mail.Session.getInstance(p, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(username, password);
            }
        });
    }
}