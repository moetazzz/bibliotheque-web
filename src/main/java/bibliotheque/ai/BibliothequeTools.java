package bibliotheque.ai;

import bibliotheque.modele.Livre;
import bibliotheque.repository.LivreRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Outils que l'assistant IA peut utiliser pour interroger la bibliothèque.
 * Chaque méthode annotée @Tool est visible par le LLM.
 */
@Service
public class BibliothequeTools {

    private final LivreRepository livreRepo;

    public BibliothequeTools(LivreRepository livreRepo) {
        this.livreRepo = livreRepo;
    }

    @Tool(description = """
        Recherche des livres dans le catalogue par leur titre (recherche partielle).
        Utilise cet outil quand l'utilisateur demande s'il y a un livre, cherche un titre,
        ou veut savoir si un livre existe dans la bibliothèque.
        """)
    public List<Livre> rechercherLivresParTitre(
            @ToolParam(description = "Le titre ou une partie du titre à rechercher")
            String titre) {
        return livreRepo.findByTitreContainingIgnoreCaseOrderByTitreAsc(titre);
    }
}