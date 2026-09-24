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
        if (livreCount == 0) {
            creerLivre("Le Petit Prince", "Antoine de Saint-Exupéry",
                    "978-2-07-040850-4", "JEUNESSE", 1943);
            creerLivre("1984", "George Orwell",
                    "978-2-07-036822-8", "SCIENCE_FICTION", 1949);
            creerLivre("Dune", "Frank Herbert",
                    "978-0441013593", "SCIENCE_FICTION", 1965);
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