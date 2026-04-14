package springboot.tutorial.async.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import org.jboss.logging.Logger;

import springboot.tutorial.async.ejb.MailerService;

@Named("mailerManagedBean")     // JSF EL: #{mailerManagedBean}
@SessionScoped                   // CDI session scope
public class MailerManagedBean implements Serializable {

    private static final Logger logger = Logger.getLogger(MailerManagedBean.class);

    @Inject
    MailerService mailerService;

    private String email;
    private String status;
    private CompletionStage<String> mailStatus;

    public String getStatus() {
        if (mailStatus != null && mailStatus.toCompletableFuture().isDone()) {
            try {
                this.setStatus(mailStatus.toCompletableFuture().get());
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