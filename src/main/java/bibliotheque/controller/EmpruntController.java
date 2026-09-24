package bibliotheque.controller;

import bibliotheque.modele.Emprunt;
import bibliotheque.modele.Livre;
import bibliotheque.modele.Utilisateur;
import bibliotheque.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/emprunts")
public class EmpruntController {

    @Autowired private EmpruntService empruntService;
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
            model.addAttribute("emprunts", empruntService.getEmpruntsEnCours());
        } else {
            List<Emprunt> miens = empruntService.getEmpruntsEnCours().stream()
                    .filter(e -> e.getUtilisateur().getId().equals(user.getId()))
                    .toList();
            model.addAttribute("emprunts", miens);
        }
        model.addAttribute("estBibliothecaire", bib);
        return "emprunts";
    }

    @GetMapping("/historique")
    public String historique(Model model, Authentication auth) {
        Utilisateur user = userService.trouverParEmail(auth.getName());
        boolean bib = estBibliothecaire(auth);

        if (bib) {
            model.addAttribute("emprunts", empruntService.getTousLesEmprunts());
        } else {
            List<Emprunt> miens = empruntService.getTousLesEmprunts().stream()
                    .filter(e -> e.getUtilisateur().getId().equals(user.getId()))
                    .toList();
            model.addAttribute("emprunts", miens);
        }
        model.addAttribute("historique", true);
        model.addAttribute("estBibliothecaire", bib);
        return "emprunts";
    }

    @GetMapping("/nouveau")
    public String formulaireEmprunt(Model model, Authentication auth) {
        Utilisateur user = userService.trouverParEmail(auth.getName());
        model.addAttribute("livres", livreService.getTousLesLivres());
        model.addAttribute("utilisateurs", userService.getTousLesUtilisateurs());
        model.addAttribute("utilisateurConnecte", user);
        return "emprunt-form";
    }

    @PostMapping
    public String emprunter(@RequestParam Long livreId,
                            @RequestParam(required = false) Long utilisateurId,
                            Authentication auth,
                            RedirectAttributes redirectAttrs) {
        try {
            Utilisateur demandeur = userService.trouverParEmail(auth.getName());
            boolean bib = estBibliothecaire(auth);

            Long cibleId = bib ? utilisateurId : demandeur.getId();
            if (cibleId == null) throw new RuntimeException("Veuillez sélectionner un emprunteur");

            Livre livre = livreService.trouverParId(livreId);
            Utilisateur cible = userService.trouverParId(cibleId);

            if (livre == null || cible == null)
                throw new RuntimeException("Livre ou utilisateur introuvable");

            empruntService.verifierPermissionEmprunt(cible, demandeur);
            empruntService.emprunter(livre, cible);

            auditService.enregistrer(cible.getNom(), cible.getId(), "EMPRUNT",
                    "Livre : " + livre.getTitre());

            redirectAttrs.addFlashAttribute("message", "Emprunt enregistré avec succès !");

        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/emprunts";
    }

    @PostMapping("/{id}/retourner")
    public String retourner(@PathVariable Long id, Authentication auth,
                            RedirectAttributes redirectAttrs) {
        try {
            Emprunt emprunt = empruntService.trouverParId(id);
            Utilisateur demandeur = userService.trouverParEmail(auth.getName());

            if (emprunt == null) throw new RuntimeException("Emprunt introuvable");

            if (!estBibliothecaire(auth) &&
                !emprunt.getUtilisateur().getId().equals(demandeur.getId())) {
                throw new RuntimeException("Vous ne pouvez retourner que vos propres emprunts");
            }

            emprunt = empruntService.retourner(id);

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

    @PostMapping("/{id}/prolonger")
    public String prolonger(@PathVariable Long id, Authentication auth,
                            RedirectAttributes redirectAttrs) {
        try {
            Emprunt emprunt = empruntService.trouverParId(id);
            Utilisateur demandeur = userService.trouverParEmail(auth.getName());

            if (emprunt == null) throw new RuntimeException("Emprunt introuvable");

            if (!estBibliothecaire(auth) &&
                !emprunt.getUtilisateur().getId().equals(demandeur.getId())) {
                throw new RuntimeException("Vous ne pouvez prolonger que vos propres emprunts");
            }

            empruntService.prolonger(id);

            auditService.enregistrer(emprunt.getUtilisateur().getNom(),
                    emprunt.getUtilisateur().getId(), "PROLONGATION",
                    "Livre : " + emprunt.getLivre().getTitre());

            redirectAttrs.addFlashAttribute("message", "⏩ Prolongation accordée (+7 jours)");

        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/emprunts";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        empruntService.supprimer(id);
        redirectAttrs.addFlashAttribute("message", "Emprunt supprimé");
        return "redirect:/emprunts";
    }
}