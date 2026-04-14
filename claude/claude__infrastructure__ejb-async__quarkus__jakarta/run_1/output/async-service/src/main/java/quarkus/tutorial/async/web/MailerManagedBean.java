package quarkus.tutorial.async.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import quarkus.tutorial.async.ejb.MailerBean;

@Named
@SessionScoped
public class MailerManagedBean implements Serializable {

    private static final Logger logger = Logger.getLogger(MailerManagedBean.class.getName());

    @EJB
    MailerBean mailerBean;

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
            logger.severe(ex.getMessage());
            this.setStatus("Encountered an error: " + ex.getMessage());
        }
        return "response?faces-redirect=true";
    }
}