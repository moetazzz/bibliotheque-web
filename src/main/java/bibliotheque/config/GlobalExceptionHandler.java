package bibliotheque.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String gererException(Exception e, Model model) {
        log.error("Erreur non gérée : {}", e.getMessage(), e);
        model.addAttribute("message", e.getMessage());
        return "error/500";
    }
}