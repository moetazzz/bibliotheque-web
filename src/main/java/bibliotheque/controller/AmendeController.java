package bibliotheque.controller;

import bibliotheque.modele.Utilisateur;
import bibliotheque.service.AuditService;
import bibliotheque.service.EmpruntService;
import bibliotheque.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/amendes")
public class AmendeController {

    @Autowired private EmpruntService empruntService;
    @Autowired private UtilisateurService userService;
    @Autowired private AuditService auditService;

    @GetMapping
    public String liste(Model model, Authentication auth) {
        Utilisateur user = userService.trouverParEmail(auth.getName());
        boolean bib = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BIB"));

        if (bib) {
            model.addAttribute("amendes", empruntService.getToutesLesAmendes());
            model.addAttribute("vue", "BIB");
        } else {
            model.addAttribute("amendes", empruntService.getAmendesUtilisateur(user));
            model.addAttribute("vue", "MEMBRE");
        }
        model.addAttribute("utilisateur", user);
        return "amendes";
    }

    @PostMapping("/payer")
    public String payer(Authentication auth, RedirectAttributes redirectAttrs) {
        Utilisateur user = userService.trouverParEmail(auth.getName());
        empruntService.payerAmendes(user);

        auditService.enregistrer(user.getNom(), user.getId(), "PAIEMENT_AMENDES",
                "Paiement de toutes les amendes");

        redirectAttrs.addFlashAttribute("message", "✅ Toutes vos amendes ont été payées !");
        return "redirect:/amendes";
    }
}