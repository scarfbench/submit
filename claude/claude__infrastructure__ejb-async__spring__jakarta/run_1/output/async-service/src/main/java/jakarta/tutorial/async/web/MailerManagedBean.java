package jakarta.tutorial.async.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.tutorial.async.ejb.MailerService;

@Named("mailerManagedBean")
@SessionScoped
public class MailerManagedBean implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(MailerManagedBean.class);

    @Inject
    private MailerService mailerService;

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
            mailStatus = mailerService.sendMessage(this.getEmail());
            this.setStatus("Processing... (refresh to check again)");
        } catch (Exception ex) {
            logger.error("Send failed", ex);
            this.setStatus("Encountered an error: " + ex.getMessage());
        }
        return "response?faces-redirect=true";
    }
}