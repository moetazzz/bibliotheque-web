package bibliotheque.controller;

import bibliotheque.modele.Utilisateur;
import bibliotheque.service.AuditService;
import bibliotheque.service.UtilisateurService;
import bibliotheque.util.PaginationInfo;
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
    public String liste(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int taille,
            @RequestParam(required = false) String tri,
            @RequestParam(required = false) String ordre,
            Model model) {

        PaginationInfo<Utilisateur> pagination = userService.rechercherAvancee(
                nom, email, role, page, taille, tri, ordre);

        model.addAttribute("pagination", pagination);
        model.addAttribute("utilisateurs", pagination.getItems());
        model.addAttribute("nom", nom);
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("taille", taille);
        model.addAttribute("tri", tri);
        model.addAttribute("ordre", ordre);
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

        boolean estNouveau = (user.getId() == null);

        // Vérifier email unique si nouveau
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