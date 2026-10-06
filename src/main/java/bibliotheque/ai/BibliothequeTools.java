package bibliotheque.ai;

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

    public BibliothequeTools(LivreRepository livreRepo, EmpruntRepository empruntRepo) {
        this.livreRepo = livreRepo;
        this.empruntRepo = empruntRepo;
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
}