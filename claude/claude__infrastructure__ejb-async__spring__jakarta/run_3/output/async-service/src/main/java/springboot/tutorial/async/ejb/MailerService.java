package springboot.tutorial.async.ejb;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ejb.Stateless;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.AsyncResult;
import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.Future;

@Stateless
public class MailerService {

    private static final Logger log = LoggerFactory.getLogger(MailerService.class);

    @Resource(lookup = "java:comp/DefaultMailSession")
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
            return new AsyncResult<>("Sent");
        } catch (Throwable t) {
            log.error("Error sending mail", t);
            return new AsyncResult<>("Encountered an error: " + t.getMessage());
        }
    }
}