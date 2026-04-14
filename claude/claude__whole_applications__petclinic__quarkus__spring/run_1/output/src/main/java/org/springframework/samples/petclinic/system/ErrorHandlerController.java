package org.springframework.samples.petclinic.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ErrorHandlerController {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorHandlerController.class);

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception exception) {
        LOG.error("Internal application error", exception);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", exception.getMessage());
        return mav;
    }
}
