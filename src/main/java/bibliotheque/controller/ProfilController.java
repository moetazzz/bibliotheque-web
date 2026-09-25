package bibliotheque.controller;

import bibliotheque.modele.Utilisateur;
import bibliotheque.service.AuditService;
import bibliotheque.service.EmpruntService;
import bibliotheque.service.UtilisateurService;
import bibliotheque.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profil")
public class ProfilController {

    @Autowired private UtilisateurService userService;
    @Autowired private EmpruntService empruntService;
    @Autowired private AuditService auditService;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public String profil(Model model, Authentication auth) {
        Utilisateur user = userService.trouverParEmail(auth.getName());
        if (user == null) return "redirect:/login";

        long empruntsEnCours = empruntService.getEmpruntsEnCours().stream()
                .filter(e -> e.getUtilisateur().getId().equals(user.getId()))
                .count();

        model.addAttribute("utilisateur", user);
        model.addAttribute("empruntsEnCours", empruntsEnCours);
        return "profil";
    }

    @PostMapping("/infos")
    public String modifierInfos(@RequestParam String nom,
                                 @RequestParam String email,
                                 Authentication auth,
                                 RedirectAttributes redirectAttrs) {
        Utilisateur user = userService.trouverParEmail(auth.getName());
        if (user == null) {
            redirectAttrs.addFlashAttribute("erreur", "Utilisateur introuvable");
            return "redirect:/profil";
        }

        if (nom == null || nom.isBlank()) {
            redirectAttrs.addFlashAttribute("erreur", "Le nom est obligatoire");
            return "redirect:/profil";
        }

        // Si l'email change, vérifier qu'il n'est pas déjà pris
        if (!user.getEmail().equalsIgnoreCase(email)) {
            if (userService.emailExiste(email)) {
                redirectAttrs.addFlashAttribute("erreur", "Cet email est déjà utilisé");
                return "redirect:/profil";
            }
            user.setEmail(email);
        }

        user.setNom(nom);
        userService.ajouter(user);

        auditService.enregistrer(user.getNom(), user.getId(), "MODIF_PROFIL",
                "Modification des infos personnelles");

        redirectAttrs.addFlashAttribute("message", "✅ Vos informations ont été mises à jour");
        return "redirect:/profil";
    }

    @PostMapping("/mot-de-passe")
    public String changerMotDePasse(@RequestParam String ancien,
                                     @RequestParam String nouveau,
                                     @RequestParam String confirmation,
                                     Authentication auth,
                                     RedirectAttributes redirectAttrs) {
        Utilisateur user = userService.trouverParEmail(auth.getName());
        if (user == null) {
            redirectAttrs.addFlashAttribute("erreur", "Utilisateur introuvable");
            return "redirect:/profil";
        }

        // Vérifier l'ancien mot de passe
        if (!PasswordUtil.verifier(ancien, user.getMotDePasse())) {
            redirectAttrs.addFlashAttribute("erreur", "❌ Ancien mot de passe incorrect");
            return "redirect:/profil";
        }

        // Vérifier longueur
        if (nouveau == null || nouveau.length() < 6) {
            redirectAttrs.addFlashAttribute("erreur",
                    "❌ Le nouveau mot de passe doit faire au moins 6 caractères");
            return "redirect:/profil";
        }

        // Vérifier confirmation
        if (!nouveau.equals(confirmation)) {
            redirectAttrs.addFlashAttribute("erreur",
                    "❌ La confirmation ne correspond pas");
            return "redirect:/profil";
        }

        // Vérifier que le nouveau n'est pas identique
        if (PasswordUtil.verifier(nouveau, user.getMotDePasse())) {
            redirectAttrs.addFlashAttribute("erreur",
                    "❌ Le nouveau mot de passe doit être différent");
            return "redirect:/profil";
        }

        // Hasher et sauvegarder
        user.setMotDePasse(PasswordUtil.hasher(nouveau));
        userService.ajouter(user);

        auditService.enregistrer(user.getNom(), user.getId(), "MODIF_MDP",
                "Changement de mot de passe");

        redirectAttrs.addFlashAttribute("message", "✅ Mot de passe modifié avec succès");
        return "redirect:/profil";
    }
}