package springboot.tutorial.async.ejb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@ApplicationScoped
public class MailerService {

    private static final Logger log = Logger.getLogger(MailerService.class);

    @Inject
    Session session;

    public Future<String> sendMessage(String email) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String subject = "Test message from async example";
                String ts = LocalDateTime.now()
                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT));
                String body = "This is a test message from the async example of the Jakarta EE Tutorial. "
                        + "It was sent on " + ts + ".";

                MimeMessage msg = new MimeMessage(session);
                msg.setFrom(); // uses mail.from if set in Session props; otherwise system user@host
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
                msg.setSubject(subject);
                msg.setHeader("X-Mailer", "Jakarta Mail");
                msg.setText(body);
                msg.setSentDate(java.util.Date.from(java.time.Instant.now()));
                Transport.send(msg);

                log.infof("Mail sent to %s", email);
                return "Sent";
            } catch (Throwable t) {
                log.error("Error sending mail", t);
                return "Encountered an error: " + t.getMessage();
            }
        });
    }
}