package bibliotheque.controller;

import bibliotheque.modele.Emprunt;
import bibliotheque.modele.Livre;
import bibliotheque.modele.Utilisateur;
import bibliotheque.service.AuditService;
import bibliotheque.service.EmpruntService;
import bibliotheque.service.LivreService;
import bibliotheque.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/emprunts")
public class EmpruntController {

    @Autowired
    private EmpruntService empruntService;

    @Autowired
    private LivreService livreService;

    @Autowired
    private UtilisateurService userService;

    @Autowired
    private AuditService auditService;

    // ==================== LISTE EN COURS ====================

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("emprunts", empruntService.getEmpruntsEnCours());
        return "emprunts";
    }

    // ==================== HISTORIQUE ====================

    @GetMapping("/historique")
    public String historique(Model model) {
        model.addAttribute("emprunts", empruntService.getTousLesEmprunts());
        model.addAttribute("historique", true);
        return "emprunts";
    }

    // ==================== FORMULAIRE EMPRUNT ====================

    @GetMapping("/nouveau")
    public String formulaireEmprunt(Model model) {
        model.addAttribute("livres", livreService.getTousLesLivres());
        model.addAttribute("utilisateurs", userService.getTousLesUtilisateurs());
        return "emprunt-form";
    }

    // ==================== EMPRUNTER ====================

    @PostMapping
    public String emprunter(@RequestParam Long livreId,
                            @RequestParam Long utilisateurId,
                            RedirectAttributes redirectAttrs) {
        try {
            Livre livre = livreService.trouverParId(livreId);
            Utilisateur user = userService.trouverParId(utilisateurId);

            if (livre == null || user == null) {
                throw new RuntimeException("Livre ou utilisateur introuvable");
            }

            empruntService.emprunter(livre, user);

            auditService.enregistrer(user.getNom(), user.getId(), "EMPRUNT",
                    "Livre : " + livre.getTitre());

            redirectAttrs.addFlashAttribute("message", "Emprunt enregistré avec succès !");

        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("erreur", e.getMessage());
        }

        return "redirect:/emprunts";
    }

    // ==================== RETOURNER ====================

    @PostMapping("/{id}/retourner")
    public String retourner(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            Emprunt emprunt = empruntService.retourner(id);

            auditService.enregistrer(emprunt.getUtilisateur().getNom(),
                    emprunt.getUtilisateur().getId(), "RETOUR",
                    "Livre : " + emprunt.getLivre().getTitre()
                    + " (retard : " + emprunt.joursDeRetard() + "j)");

            String message = emprunt.joursDeRetard() > 0
                    ? "Livre retourné. ⚠️ Retard de " + emprunt.joursDeRetard() + " jours"
                    : "Livre retourné à temps !";
            redirectAttrs.addFlashAttribute("message", message);
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/emprunts";
    }

    // ==================== PROLONGER ====================

    @PostMapping("/{id}/prolonger")
    public String prolonger(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            Emprunt emprunt = empruntService.trouverParId(id);

            empruntService.prolonger(id);

            if (emprunt != null) {
                auditService.enregistrer(emprunt.getUtilisateur().getNom(),
                        emprunt.getUtilisateur().getId(), "PROLONGATION",
                        "Livre : " + emprunt.getLivre().getTitre());
            }

            redirectAttrs.addFlashAttribute("message", "⏩ Prolongation accordée (+7 jours)");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/emprunts";
    }

    // ==================== SUPPRIMER ====================

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        empruntService.supprimer(id);
        redirectAttrs.addFlashAttribute("message", "Emprunt supprimé");
        return "redirect:/emprunts";
    }
}