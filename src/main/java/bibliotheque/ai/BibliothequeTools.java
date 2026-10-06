package bibliotheque.ai;

import bibliotheque.service.AuditService;
import bibliotheque.service.EmpruntService;
import bibliotheque.service.UtilisateurService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import bibliotheque.modele.Emprunt;
import bibliotheque.modele.Livre;
import bibliotheque.repository.EmpruntRepository;
import bibliotheque.repository.LivreRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Outils que l'agent IA peut utiliser pour interroger la bibliothèque.
 * Chaque méthode annotée @Tool est visible par le LLM.
 */
@Service
public class BibliothequeTools {

    private final LivreRepository livreRepo;
    private final EmpruntRepository empruntRepo;
    private final EmpruntService empruntService;
    private final UtilisateurService userService;
    private final AuditService auditService;

    public BibliothequeTools(LivreRepository livreRepo,
                            EmpruntRepository empruntRepo,
                            EmpruntService empruntService,
                            UtilisateurService userService,
                            AuditService auditService) {
        this.livreRepo = livreRepo;
        this.empruntRepo = empruntRepo;
        this.empruntService = empruntService;
        this.userService = userService;
        this.auditService = auditService;
    }
    // ==================== RECHERCHE DE LIVRES ====================

    @Tool(description = """
        Recherche des livres par leur titre (recherche partielle, insensible à la casse).
        Utilise cet outil quand l'utilisateur cherche un livre par son titre.
        """)
    public List<Livre> rechercherLivresParTitre(
            @ToolParam(description = "Le titre ou une partie du titre à rechercher")
            String titre) {
        return livreRepo.findByTitreContainingIgnoreCaseOrderByTitreAsc(titre);
    }

    @Tool(description = """
        Recherche des livres par auteur (recherche partielle, insensible à la casse).
        Utilise cet outil quand l'utilisateur cherche les livres d'un auteur.
        """)
    public List<Livre> rechercherLivresParAuteur(
            @ToolParam(description = "Le nom ou partie du nom de l'auteur")
            String auteur) {
        return livreRepo.findAll().stream()
                .filter(l -> l.getAuteur() != null
                        && l.getAuteur().toLowerCase().contains(auteur.toLowerCase()))
                .toList();
    }

    @Tool(description = """
        Liste les livres d'une catégorie donnée.
        Catégories valides : ROMAN, SCIENCE_FICTION, BIOGRAPHIE, HISTOIRE, JEUNESSE, BD, AUTRE.
        Utilise cet outil quand l'utilisateur veut voir les livres d'un genre précis.
        """)
    public List<Livre> rechercherLivresParCategorie(
            @ToolParam(description = "La catégorie (ex: ROMAN, SCIENCE_FICTION, BD)")
            String categorie) {
        return livreRepo.findAll().stream()
                .filter(l -> categorie.equalsIgnoreCase(l.getCategorie()))
                .toList();
    }

    @Tool(description = """
        Compte le nombre total de livres dans la bibliothèque.
        Utilise cet outil quand l'utilisateur demande "combien de livres ?" ou des statistiques générales.
        """)
    public long compterLivres() {
        return livreRepo.count();
    }

    // ==================== DISPONIBILITÉ ====================

    @Tool(description = """
        Vérifie si un livre est disponible à l'emprunt en cherchant par son titre exact.
        Retourne un message clair indiquant si le livre est disponible ou emprunté.
        Utilise cet outil quand l'utilisateur demande si un livre est disponible.
        """)
    public String verifierDisponibilite(
            @ToolParam(description = "Le titre exact du livre à vérifier")
            String titre) {
        List<Livre> livres = livreRepo.findByTitreContainingIgnoreCaseOrderByTitreAsc(titre);

        if (livres.isEmpty()) {
            return "Aucun livre trouvé avec le titre '" + titre + "'.";
        }

        Livre livre = livres.get(0);
        Emprunt empruntEnCours = empruntRepo.findByLivreAndDateRetourEffectiveIsNull(livre);

        if (empruntEnCours == null) {
            return "Le livre '" + livre.getTitre() + "' est DISPONIBLE.";
        } else {
            return "Le livre '" + livre.getTitre() + "' est actuellement EMPRUNTÉ par "
                    + empruntEnCours.getUtilisateur().getNom()
                    + " jusqu'au " + empruntEnCours.getDateRetourPrevue() + ".";
        }
    }

    // ==================== EMPRUNTS ====================

    @Tool(description = """
        Liste tous les emprunts en cours dans la bibliothèque (livres non encore rendus).
        Utilise cet outil quand l'utilisateur demande la liste des emprunts en cours,
        ou veut savoir quels livres sont sortis actuellement.
        """)
    public List<Emprunt> listerEmpruntsEnCours() {
        return empruntRepo.findByDateRetourEffectiveIsNullOrderByDateRetourPrevueAsc();
    }
        // ==================== ACTIONS : PROLONGATION ====================

    @Tool(description = """
        Vérifie si un emprunt peut être prolongé (sans l'exécuter).
        Utilise TOUJOURS cet outil en PREMIER quand l'utilisateur demande de prolonger un emprunt.
        Ensuite, tu dois DEMANDER CONFIRMATION à l'utilisateur avant d'appeler executerProlongation.
        Retourne les détails de l'emprunt et si la prolongation est possible ou non.
        """)
    public String verifierProlongation(
            @ToolParam(description = "L'identifiant de l'emprunt à prolonger")
            Long idEmprunt) {

        // 1. Vérifier que l'utilisateur est bien BIB
        String email = getUtilisateurConnecte();
        if (email == null) {
            return "⚠️ Action impossible : utilisateur non connecté.";
        }

        var utilisateur = userService.trouverParEmail(email);
        if (utilisateur == null || !"BIB".equals(utilisateur.getRole())) {
            return "⛔ Action réservée aux bibliothécaires. Vous n'avez pas les droits nécessaires.";
        }

        // 2. Récupérer l'emprunt
        var emprunt = empruntService.trouverParId(idEmprunt);
        if (emprunt == null) {
            return "❌ Emprunt #" + idEmprunt + " introuvable.";
        }

        // 3. Vérifications métier
        if (emprunt.estRendu()) {
            return "❌ Cet emprunt a déjà été retourné. Aucune prolongation possible.";
        }
        if (emprunt.getProlongations() >= 1) {
            return "❌ Maximum de prolongations atteint (1 seule autorisée).";
        }

        // 4. Tout est OK → on décrit l'action à confirmer
        return String.format(
            "✅ L'emprunt #%d peut être prolongé de 7 jours.%n" +
            "📖 Livre : %s%n" +
            "👤 Emprunteur : %s%n" +
            "📅 Retour actuel : %s → nouveau retour : %s%n" +
            "⚠️ Demande à l'utilisateur de confirmer avant d'exécuter.",
            emprunt.getId(),
            emprunt.getLivre().getTitre(),
            emprunt.getUtilisateur().getNom(),
            emprunt.getDateRetourPrevue(),
            emprunt.getDateRetourPrevue().plusDays(7)
        );
    }

    @Tool(description = """
        Exécute la prolongation d'un emprunt de 7 jours.
        ATTENTION : N'appelle cet outil QU'APRÈS avoir appelé verifierProlongation
        ET reçu une confirmation explicite de l'utilisateur (par "oui", "ok", "confirme"...).
        Si l'utilisateur n'a pas clairement confirmé, ne pas appeler cet outil.
        """)
    @Transactional
    public String executerProlongation(
            @ToolParam(description = "L'identifiant de l'emprunt à prolonger")
            Long idEmprunt) {

        // 1. Vérifier le rôle
        String email = getUtilisateurConnecte();
        if (email == null) {
            return "⚠️ Action impossible : utilisateur non connecté.";
        }

        var utilisateur = userService.trouverParEmail(email);
        if (utilisateur == null || !"BIB".equals(utilisateur.getRole())) {
            return "⛔ Prolongation refusée : droits insuffisants.";
        }

        // 2. Exécuter
        try {
            var emprunt = empruntService.prolonger(idEmprunt);

            // 3. Journal d'audit
            auditService.enregistrer(
                utilisateur.getNom(),
                utilisateur.getId(),
                "PROLONGATION_IA",
                "Via assistant IA — Emprunt #" + idEmprunt
                + " (livre : " + emprunt.getLivre().getTitre() + ")"
            );

            // 4. Message de succès
            return String.format(
                "✅ Prolongation effectuée avec succès !%n" +
                "📖 Livre : %s%n" +
                "📅 Nouvelle date de retour : %s",
                emprunt.getLivre().getTitre(),
                emprunt.getDateRetourPrevue()
            );

        } catch (Exception e) {
            return "❌ Erreur lors de la prolongation : " + e.getMessage();
        }
    }

    // ==================== UTILITAIRE ====================

    private String getUtilisateurConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
            || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return auth.getName();
    }
}