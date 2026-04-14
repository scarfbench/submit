package springboot.tutorial.async.ejb;

import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Stateless session bean that sends email asynchronously.
 * Uses Jakarta EE @Asynchronous annotation for async execution.
 */
@Stateless
public class MailerService {

    private static final Logger log = LoggerFactory.getLogger(MailerService.class);

    @Inject
    private Session session;

    @Asynchronous
    public Future<String> sendMessage(String email) {
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

            log.info("Mail sent to {}", email);
            return CompletableFuture.completedFuture("Sent");
        } catch (Throwable t) {
            log.error("Error sending mail", t);
            return CompletableFuture.completedFuture("Encountered an error: " + t.getMessage());
        }
    }
}