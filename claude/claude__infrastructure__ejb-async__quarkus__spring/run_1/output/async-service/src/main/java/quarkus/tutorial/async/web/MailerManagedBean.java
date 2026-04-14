package quarkus.tutorial.async.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import quarkus.tutorial.async.ejb.MailerBean;

@Controller
@SessionAttributes({"email", "status", "mailStatus"})
public class MailerManagedBean {

    private static final Logger logger = Logger.getLogger(MailerManagedBean.class.getName());

    @Autowired
    private MailerBean mailerBean;

    private Future<String> mailStatus;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/send")
    public String send(@RequestParam("email") String email, Model model) {
        try {
            mailStatus = mailerBean.sendMessage(email);
            model.addAttribute("email", email);
            model.addAttribute("status", "Processing... (refresh to check again)");
            model.addAttribute("mailStatus", mailStatus);
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            model.addAttribute("status", "Encountered an error: " + ex.getMessage());
        }
        return "redirect:/response";
    }

    @GetMapping("/response")
    public String response(Model model) {
        if (mailStatus != null && mailStatus.isDone()) {
            try {
                String status = mailStatus.get();
                model.addAttribute("status", status);
            } catch (ExecutionException | CancellationException | InterruptedException ex) {
                String status = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
                model.addAttribute("status", status);
            }
        } else if (!model.containsAttribute("status")) {
            model.addAttribute("status", "No status available");
        }
        return "response";
    }
}
