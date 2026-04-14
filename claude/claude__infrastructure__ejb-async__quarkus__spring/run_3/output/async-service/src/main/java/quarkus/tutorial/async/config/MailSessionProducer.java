package quarkus.tutorial.async.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Properties;

@Configuration
public class MailSessionProducer {

    @Value("${spring.mail.host:localhost}")
    private String mailHost;

    @Value("${spring.mail.port:3025}")
    private String mailPort;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private String mailAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private String startTls;

    @Value("${spring.mail.username:jack}")
    private String mailUsername;

    @Value("${spring.mail.password:changeMe}")
    private String mailPassword;

    @Bean
    public jakarta.mail.Session mailSession() {
        Properties p = new Properties();
        p.put("mail.smtp.host", mailHost);
        p.put("mail.smtp.port", mailPort);
        p.put("mail.smtp.auth", mailAuth);
        p.put("mail.smtp.starttls.enable", startTls);

        return jakarta.mail.Session.getInstance(p, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(mailUsername, mailPassword);
            }
        });
    }
}