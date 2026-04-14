package org.springframework.samples.petclinic.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.ui.Model;

@ControllerAdvice
public class ErrorController {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorController.class);

    @ExceptionHandler(Exception.class)
    public String handleException(Exception exception, Model model) {
        LOG.error("Internal application error", exception);
        model.addAttribute("message", exception.getMessage());
        return "error";
    }
}
