package bibliotheque.controller;

import bibliotheque.modele.Livre;
import bibliotheque.service.AuditService;
import bibliotheque.service.LivreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/livres")
public class LivreController {

    @Autowired private LivreService livreService;
    @Autowired private AuditService auditService;

    @GetMapping
    public String liste(@RequestParam(required = false) String recherche, Model model) {
        model.addAttribute("livres", livreService.rechercher(recherche));
        model.addAttribute("recherche", recherche);
        return "livres";
    }

    @GetMapping("/nouveau")
    public String formulaireAjout(Model model) {
        model.addAttribute("livre", new Livre());
        return "livre-form";
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        Livre livre = livreService.trouverParId(id);
        if (livre == null) return "redirect:/livres";
        model.addAttribute("livre", livre);
        return "livre-form";
    }

    @PostMapping
    public String enregistrer(@Valid @ModelAttribute("livre") Livre livre,
                              BindingResult result,
                              RedirectAttributes redirectAttrs,
                              Principal principal) {
        if (result.hasErrors()) return "livre-form";

        boolean estNouveau = (livre.getId() == null);
        livreService.ajouter(livre);

        String user = principal != null ? principal.getName() : "Système";
        auditService.enregistrer(user, null,
                estNouveau ? "AJOUT_LIVRE" : "MODIF_LIVRE",
                (estNouveau ? "Ajout : " : "Modif : ") + livre.getTitre());

        redirectAttrs.addFlashAttribute("message",
                estNouveau ? "Livre ajouté avec succès !" : "Livre modifié avec succès !");
        return "redirect:/livres";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttrs,
                            Principal principal) {
        Livre l = livreService.trouverParId(id);
        String titre = l != null ? l.getTitre() : "?";
        livreService.supprimer(id);

        String user = principal != null ? principal.getName() : "Système";
        auditService.enregistrer(user, null, "SUPPR_LIVRE", "Suppression : " + titre);

        redirectAttrs.addFlashAttribute("message", "Livre supprimé avec succès !");
        return "redirect:/livres";
    }
}