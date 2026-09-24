package bibliotheque.config;

import bibliotheque.modele.Utilisateur;
import bibliotheque.repository.UtilisateurRepository;
import bibliotheque.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeed implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeed.class);

    @Autowired
    private UtilisateurRepository userRepo;


    @Autowired
    private bibliotheque.repository.LivreRepository livreRepo;   // ← AJOUT

    @Override
    public void run(String... args) {
        // Créer les utilisateurs s'ils n'existent pas
        creerSiAbsent("Admin", "admin@biblio.com", "Admin123!", "BIB");
        creerSiAbsent("Alice", "alice@mail.com", "Alice123!", "MEMBRE");
        creerSiAbsent("Bob", "bob@mail.com", "Bob12345!", "MEMBRE");

        log.info("=== Base de données : {} utilisateur(s) ===", userRepo.count());
    }

    private void creerSiAbsent(String nom, String email, String mdp, String role) {
        if (userRepo.findByEmail(email).isEmpty()) {
            Utilisateur u = new Utilisateur(nom, email, PasswordUtil.hasher(mdp), role);
            userRepo.save(u);
            log.info("✅ Utilisateur créé : {} / {} ({})", email, mdp, role);
        } else {
            log.info("ℹ️ Utilisateur déjà existant : {}", email);
        }
                // Ajouter des livres de test s'il n'y en a pas
        if (livreRepo.count() == 0) {
            livreRepo.save(new bibliotheque.modele.Livre(
                    "Le Petit Prince", "Antoine de Saint-Exupéry",
                    "978-2-07-040850-4", "JEUNESSE", 1943));
            livreRepo.save(new bibliotheque.modele.Livre(
                    "1984", "George Orwell",
                    "978-2-07-036822-8", "SCIENCE_FICTION", 1949));
            livreRepo.save(new bibliotheque.modele.Livre(
                    "Dune", "Frank Herbert",
                    "978-0441013593", "SCIENCE_FICTION", 1965));
            log.info("✅ 3 livres de test créés");
        }
    }
    
}