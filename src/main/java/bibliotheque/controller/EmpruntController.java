package bibliotheque.controller;

import bibliotheque.modele.Emprunt;
import bibliotheque.modele.Livre;
import bibliotheque.modele.Utilisateur;
import bibliotheque.service.*;
import bibliotheque.util.PaginationInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import bibliotheque.util.PaginationInfo;
import java.util.List;

@Controller
@RequestMapping("/emprunts")
public class EmpruntController {

    @Autowired private EmpruntService empruntService;
    @Autowired private LivreService livreService;
    @Autowired private UtilisateurService userService;
    @Autowired private AuditService auditService;
    @Autowired private ReservationService reservationService;

    private boolean estBibliothecaire(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BIB"));
    }

    @GetMapping
    public String liste(
            @RequestParam(required = false) String statut,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int taille,
            @RequestParam(required = false) String tri,
            @RequestParam(required = false) String ordre,
            Model model, Authentication auth) {

        Utilisateur user = userService.trouverParEmail(auth.getName());
        boolean bib = estBibliothecaire(auth);

        // Statut par défaut
        String s = (statut == null || statut.isBlank())
                ? (bib ? "EN_COURS" : "EN_COURS")
                : statut;

        // Si membre : filtrer sur ses emprunts uniquement
        Long idUser = bib ? null : user.getId();

        PaginationInfo<Emprunt> pagination = empruntService.rechercherAvancee(
                s, idUser, page, taille, tri, ordre);

        model.addAttribute("pagination", pagination);
        model.addAttribute("emprunts", pagination.getItems());
        model.addAttribute("estBibliothecaire", bib);
        model.addAttribute("statut", s);
        model.addAttribute("taille", taille);
        model.addAttribute("tri", tri);
        model.addAttribute("ordre", ordre);
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

            Livre livre = emprunt.getLivre();
            emprunt = empruntService.retourner(id);

            // Notifier la prochaine réservation
            reservationService.notifierRetour(livre);

            auditService.enregistrer(emprunt.getUtilisateur().getNom(),
                    emprunt.getUtilisateur().getId(), "RETOUR",
                    "Livre : " + livre.getTitre()
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