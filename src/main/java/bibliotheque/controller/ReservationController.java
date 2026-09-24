package bibliotheque.controller;

import bibliotheque.modele.Livre;
import bibliotheque.modele.Reservation;
import bibliotheque.modele.Utilisateur;
import bibliotheque.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired private ReservationService reservationService;
    @Autowired private LivreService livreService;
    @Autowired private UtilisateurService userService;
    @Autowired private AuditService auditService;

    private boolean estBibliothecaire(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BIB"));
    }

    @GetMapping
    public String liste(Model model, Authentication auth) {
        Utilisateur user = userService.trouverParEmail(auth.getName());
        boolean bib = estBibliothecaire(auth);

        if (bib) {
            model.addAttribute("reservations", reservationService.getToutesLesReservationsActives());
            model.addAttribute("vue", "BIB");
        } else {
            model.addAttribute("reservations", reservationService.getReservationsUtilisateur(user));
            model.addAttribute("vue", "MEMBRE");
        }
        return "reservations";
    }

    @PostMapping
    public String reserver(@RequestParam Long livreId, Authentication auth,
                           RedirectAttributes redirectAttrs) {
        try {
            Utilisateur user = userService.trouverParEmail(auth.getName());
            Livre livre = livreService.trouverParId(livreId);

            if (livre == null) throw new RuntimeException("Livre introuvable");

            reservationService.reserver(livre, user);

            auditService.enregistrer(user.getNom(), user.getId(), "RESERVATION",
                    "Réservation : " + livre.getTitre());

            redirectAttrs.addFlashAttribute("message",
                    "✅ Réservation confirmée pour '" + livre.getTitre()
                    + "'. Vous serez notifié quand il sera disponible.");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/reservations";
    }

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id, Authentication auth,
                          RedirectAttributes redirectAttrs) {
        try {
            Utilisateur user = userService.trouverParEmail(auth.getName());
            reservationService.annuler(id, user);
            redirectAttrs.addFlashAttribute("message", "✅ Réservation annulée");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/reservations";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        reservationService.supprimer(id);
        redirectAttrs.addFlashAttribute("message", "Réservation supprimée");
        return "redirect:/reservations";
    }
}