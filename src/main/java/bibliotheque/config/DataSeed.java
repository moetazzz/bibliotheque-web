package bibliotheque.config;

import bibliotheque.modele.Livre;
import bibliotheque.modele.Utilisateur;
import bibliotheque.repository.LivreRepository;
import bibliotheque.repository.UtilisateurRepository;
import bibliotheque.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Crée les données par défaut si la base est vide.
 * ⚠️ À COMMENTAIRES en production pour ne pas réinitialiser.
 */
@Component
public class DataSeed implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeed.class);

    @Autowired
    private UtilisateurRepository userRepo;

    @Autowired
    private LivreRepository livreRepo;

    @Override
    public void run(String... args) {
        long userCount = userRepo.count();
        long livreCount = livreRepo.count();
        log.info("=== Base de données : {} utilisateur(s), {} livre(s) ===", userCount, livreCount);

        // ⚠️ Décommentez pour créer les données initiales (1ère fois uniquement)
        
        if (userCount == 0) {
            creerUtilisateur("Admin", "admin@biblio.com", "Admin123!", "BIB");
            creerUtilisateur("Alice", "alice@mail.com", "Alice123!", "MEMBRE");
            creerUtilisateur("Bob", "bob@mail.com", "Bob12345!", "MEMBRE");
        }
        if (livreCount ==3) {
            creerLivre("Le Petit Prince", "Antoine de Saint-Exupéry", "978-2-07-040850-4", "JEUNESSE", 1943);
            creerLivre("1984", "George Orwell", "978-2-07-036822-8", "SCIENCE_FICTION", 1949);
            creerLivre("Dune", "Frank Herbert", "978-0441013593", "SCIENCE_FICTION", 1965);
            creerLivre("Harry Potter à l'école des sorciers", "J.K. Rowling", "978-2070584628", "JEUNESSE", 1997);
            creerLivre("Le Hobbit", "J.R.R. Tolkien", "978-2266150941", "SCIENCE_FICTION", 1937);
            creerLivre("Fondation", "Isaac Asimov", "978-2070415700", "SCIENCE_FICTION", 1951);
            creerLivre("Les Misérables", "Victor Hugo", "978-2253096337", "ROMAN", 1862);
            creerLivre("L'Étranger", "Albert Camus", "978-2070360024", "ROMAN", 1942);
            creerLivre("Le Comte de Monte-Cristo", "Alexandre Dumas", "978-2253004226", "ROMAN", 1844);
            creerLivre("Voyage au bout de la nuit", "Louis-Ferdinand Céline", "978-2070360284", "ROMAN", 1932);
            creerLivre("La Peste", "Albert Camus", "978-2070360420", "ROMAN", 1947);
            creerLivre("Le Rouge et le Noir", "Stendhal", "978-2253004226", "ROMAN", 1830);
        }
        
    }

    private void creerUtilisateur(String nom, String email, String mdp, String role) {
        if (userRepo.findByEmail(email).isEmpty()) {
            Utilisateur u = new Utilisateur(nom, email, PasswordUtil.hasher(mdp), role);
            userRepo.save(u);
            log.info("✅ Utilisateur créé : {} / {} ({})", email, mdp, role);
        }
    }

    private void creerLivre(String titre, String auteur, String isbn,
                             String categorie, Integer annee) {
        Livre l = new Livre(titre, auteur, isbn, categorie, annee);
        livreRepo.save(l);
        log.info("✅ Livre créé : {}", titre);
    }
}