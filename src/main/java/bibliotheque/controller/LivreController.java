package bibliotheque.controller;

import bibliotheque.modele.Livre;
import bibliotheque.service.AuditService;
import bibliotheque.service.EmpruntService;
import bibliotheque.service.LivreService;
import bibliotheque.util.PaginationInfo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/livres")
public class LivreController {

    @Autowired private LivreService livreService;
    @Autowired private AuditService auditService;
    @Autowired private EmpruntService empruntService;

    @GetMapping
    public String liste(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) String auteur,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) Integer anneeMin,
            @RequestParam(required = false) Integer anneeMax,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int taille,
            @RequestParam(required = false) String tri,
            @RequestParam(required = false) String ordre,
            Model model) {

        PaginationInfo<Livre> pagination = livreService.rechercherAvancee(
                titre, auteur, categorie, anneeMin, anneeMax, page, taille, tri, ordre);

        // Map de disponibilité pour les livres affichés
        Map<Long, Boolean> disponibles = new HashMap<>();
        for (Livre l : pagination.getItems()) {
            try {
                disponibles.put(l.getId(), empruntService.estDisponible(l));
            } catch (Exception e) {
                disponibles.put(l.getId(), true);
            }
        }

        model.addAttribute("pagination", pagination);
        model.addAttribute("livres", pagination.getItems());
        model.addAttribute("disponibles", disponibles);

        // Filtres (pour les réafficher dans le formulaire)
        model.addAttribute("titre", titre);
        model.addAttribute("auteur", auteur);
        model.addAttribute("categorie", categorie);
        model.addAttribute("anneeMin", anneeMin);
        model.addAttribute("anneeMax", anneeMax);
        model.addAttribute("taille", taille);
        model.addAttribute("tri", tri);
        model.addAttribute("ordre", ordre);

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