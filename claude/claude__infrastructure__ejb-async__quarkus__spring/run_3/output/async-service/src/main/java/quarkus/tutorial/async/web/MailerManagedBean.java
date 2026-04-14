package quarkus.tutorial.async.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import quarkus.tutorial.async.ejb.MailerBean;

@Component("mailerManagedBean")
@Scope("session")
public class MailerManagedBean implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(MailerManagedBean.class);

    @Autowired
    private MailerBean mailerBean;

    private String email;
    private String status;
    private Future<String> mailStatus;

    public String getStatus() {
        if (mailStatus != null && mailStatus.isDone()) {
            try {
                this.setStatus(mailStatus.get());
            } catch (ExecutionException | CancellationException | InterruptedException ex) {
                this.setStatus(ex.getCause() != null ? ex.getCause().toString() : ex.toString());
            }
        }
        return status;
    }

    public void setStatus(String status) { this.status = status; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String send() {
        try {
            mailStatus = mailerBean.sendMessage(this.getEmail());
            this.setStatus("Processing... (refresh to check again)");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            this.setStatus("Encountered an error: " + ex.getMessage());
        }
        return "response?faces-redirect=true";
    }
}