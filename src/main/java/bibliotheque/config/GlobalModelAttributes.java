package bibliotheque.config;

import bibliotheque.modele.Utilisateur;
import bibliotheque.service.NotificationService;
import bibliotheque.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Ajoute automatiquement des attributs communs à toutes les vues.
 * Utilisé notamment pour le badge de notifications dans la navbar.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired private NotificationService notificationService;
    @Autowired private UtilisateurService userService;

    /**
     * Compte les notifications non lues de l'utilisateur connecté.
     * Retourne 0 si personne n'est connecté ou en cas d'erreur.
     */
    @ModelAttribute("nbNotificationsNonLues")
    public long nbNotificationsNonLues(Authentication auth) {
        // Personne connecté (ou page publique)
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            return 0;
        }

        try {
            Utilisateur user = userService.trouverParEmail(auth.getName());
            if (user == null) return 0;
            return notificationService.countNonLues(user.getId());
        } catch (Exception e) {
            // Ne jamais faire planter une page à cause du badge
            return 0;
        }
    }
}