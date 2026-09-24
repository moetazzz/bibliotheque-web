package bibliotheque.controller;

import bibliotheque.modele.Utilisateur;
import bibliotheque.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/utilisateurs")
public class UtilisateurController {

    @Autowired
    private UtilisateurService userService;

    @Autowired
    private bibliotheque.service.AuditService auditService;

    // ==================== LISTE ====================

    @GetMapping
    public String liste(@RequestParam(required = false) String recherche, Model model) {
        model.addAttribute("utilisateurs", userService.rechercher(recherche));
        model.addAttribute("recherche", recherche);
        return "utilisateurs";
    }

    // ==================== FORMULAIRE ====================

    @GetMapping("/nouveau")
    public String formulaireAjout(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "utilisateur-form";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        Utilisateur user = userService.trouverParId(id);
        if (user == null) {
            return "redirect:/utilisateurs";
        }
        model.addAttribute("utilisateur", user);
        return "utilisateur-form";
    }

    // ==================== ENREGISTREMENT ====================

    @PostMapping
    public String enregistrer(@Valid @ModelAttribute("utilisateur") Utilisateur user,
                              BindingResult result,
                              RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            return "utilisateur-form";
        }
        boolean estNouveau = (user.getId() == null);
        userService.ajouter(user);

        String action = estNouveau ? "AJOUT_USER" : "MODIF_USER";
        auditService.enregistrer(user.getNom(), user.getId(), action,
                (estNouveau ? "Ajout : " : "Modif : ") + user.getEmail());

        redirectAttrs.addFlashAttribute("message",
                estNouveau ? "Utilisateur ajouté avec succès !" : "Utilisateur modifié avec succès !");
        return "redirect:/utilisateurs";
    }

    // ==================== SUPPRESSION ====================

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        Utilisateur u = userService.trouverParId(id);
        String nom = u != null ? u.getNom() : "?";
        userService.supprimer(id);

        auditService.enregistrer(nom, id, "SUPPR_USER", "Suppression : " + nom);

        redirectAttrs.addFlashAttribute("message", "Utilisateur supprimé avec succès !");
        return "redirect:/utilisateurs";
    }
}