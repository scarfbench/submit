package quarkus.tutorial.async.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Properties;

@Configuration
public class MailSessionProducer {

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private String mailPort;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String mailAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String mailStartTls;

    @Value("${spring.mail.username}")
    private String mailUser;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Bean
    public jakarta.mail.Session mailSession() {
        Properties p = new Properties();
        p.put("mail.smtp.host", mailHost);
        p.put("mail.smtp.port", mailPort);
        p.put("mail.smtp.auth", mailAuth);
        p.put("mail.smtp.starttls.enable", mailStartTls);

        return jakarta.mail.Session.getInstance(p, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(mailUser, mailPassword);
            }
        });
    }
}
