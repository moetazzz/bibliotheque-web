package bibliotheque.controller;

import bibliotheque.modele.Utilisateur;
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

    @Autowired
    private EmpruntService empruntService;

    @Autowired
    private UtilisateurService userService;

    @GetMapping
    public String liste(Model model, Authentication auth) {
        Utilisateur user = userService.trouverParEmail(auth.getName());

        if (user.getRole().equals("BIB")) {
            // Bibliothécaire : voit toutes les amendes impayées
            model.addAttribute("amendes", empruntService.getToutesLesAmendes());
            model.addAttribute("vue", "BIB");
        } else {
            // Membre : voit ses propres amendes
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
        redirectAttrs.addFlashAttribute("message", "✅ Toutes vos amendes ont été payées !");
        return "redirect:/amendes";
    }

    @PostMapping("/{id}/payer")
    public String payerUne(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        // À implémenter : payer une amende spécifique (BIB)
        redirectAttrs.addFlashAttribute("message", "Amende marquée comme payée");
        return "redirect:/amendes";
    }
}