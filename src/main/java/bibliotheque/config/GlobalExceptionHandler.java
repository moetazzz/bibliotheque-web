package bibliotheque.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Ignorer les ressources statiques manquantes (favicon, screenshots...) */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> gererRessourceManquante(NoResourceFoundException e) {
        // Ne pas polluer les logs : ces erreurs sont bénignes
        return ResponseEntity.notFound().build();
    }

    /** Erreurs non gérées : page 500 */
    @ExceptionHandler(Exception.class)
    public String gererException(Exception e, Model model) {
        log.error("Erreur non gérée : {}", e.getMessage(), e);
        model.addAttribute("message", e.getMessage());
        return "error/500";
    }
}