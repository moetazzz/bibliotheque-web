package bibliotheque.controller;

import bibliotheque.modele.Utilisateur;
import bibliotheque.service.AuditService;
import bibliotheque.service.UtilisateurService;
import bibliotheque.util.PasswordUtil;
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

    @Autowired private UtilisateurService userService;
    @Autowired private AuditService auditService;

    @GetMapping
    public String liste(@RequestParam(required = false) String recherche, Model model) {
        model.addAttribute("utilisateurs", userService.rechercher(recherche));
        model.addAttribute("recherche", recherche);
        return "utilisateurs";
    }

    @GetMapping("/nouveau")
    public String formulaireAjout(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "utilisateur-form";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        Utilisateur u = userService.trouverParId(id);
        if (u == null) return "redirect:/utilisateurs";
        model.addAttribute("utilisateur", u);
        return "utilisateur-form";
    }

    @PostMapping
    public String enregistrer(@Valid @ModelAttribute("utilisateur") Utilisateur user,
                              BindingResult result,
                              RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) return "utilisateur-form";

        // Vérifier email unique si nouveau
        boolean estNouveau = (user.getId() == null);
        if (estNouveau && userService.emailExiste(user.getEmail())) {
            result.rejectValue("email", "error.email", "Cet email est déjà utilisé");
            return "utilisateur-form";
        }

        // Hasher le mot de passe s'il est modifié
        if (user.getMotDePasse() != null && !user.getMotDePasse().startsWith("$2a$")) {
            user.setMotDePasse(PasswordUtil.hasher(user.getMotDePasse()));
        }

        userService.ajouter(user);

        auditService.enregistrer(user.getNom(), user.getId(),
                estNouveau ? "AJOUT_USER" : "MODIF_USER",
                (estNouveau ? "Ajout : " : "Modif : ") + user.getEmail());

        redirectAttrs.addFlashAttribute("message",
                estNouveau ? "Utilisateur ajouté avec succès !" : "Utilisateur modifié avec succès !");
        return "redirect:/utilisateurs";
    }

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