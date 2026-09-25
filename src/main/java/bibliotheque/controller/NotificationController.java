package bibliotheque.controller;

import bibliotheque.modele.Utilisateur;
import bibliotheque.service.NotificationService;
import bibliotheque.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired private NotificationService notificationService;
    @Autowired private UtilisateurService userService;

    @GetMapping
    public String liste(Model model, Authentication auth) {
        Utilisateur user = userService.trouverParEmail(auth.getName());
        model.addAttribute("notifications", notificationService.getNotificationsUtilisateur(user.getId()));
        model.addAttribute("nbNonLues", notificationService.countNonLues(user.getId()));
        return "notifications";
    }

    @PostMapping("/{id}/lue")
    public String marquerLue(@PathVariable Long id) {
        notificationService.marquerCommeLue(id);
        return "redirect:/notifications";
    }

    @PostMapping("/tout-lire")
    public String toutLire(Authentication auth, RedirectAttributes redirectAttrs) {
        Utilisateur user = userService.trouverParEmail(auth.getName());
        notificationService.marquerToutesCommeLues(user.getId());
        redirectAttrs.addFlashAttribute("message", "✅ Toutes les notifications marquées comme lues");
        return "redirect:/notifications";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        notificationService.supprimer(id);
        return "redirect:/notifications";
    }
}