package quarkus.tutorial.async.ejb;

import jakarta.ejb.Stateless;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.AsyncResult;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.logging.Level;

@Stateless
public class MailerBean {

    @Inject
    Session session;

    private static final Logger log = Logger.getLogger(MailerBean.class.getName());

    @Asynchronous
    public Future<String> sendMessage(String email) {
        try {
            String subject = "Test message from async example";
            String ts = LocalDateTime.now()
                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT));
            String body = "This is a test message from the async example of the Jakarta EE Tutorial. "
                    + "It was sent on " + ts + ".";

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom();
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
            msg.setSubject(subject);
            msg.setHeader("X-Mailer", "Jakarta Mail");
            msg.setText(body);
            msg.setSentDate(java.util.Date.from(java.time.Instant.now()));
            Transport.send(msg);

            log.log(Level.INFO, "Mail sent to {0}", email);
            return new AsyncResult<>("Sent");
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Error in sending message.", t);
            return new AsyncResult<>("Encountered an error: " + t.getMessage());
        }
    }
}