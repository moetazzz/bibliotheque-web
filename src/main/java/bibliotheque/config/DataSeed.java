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
    }
}