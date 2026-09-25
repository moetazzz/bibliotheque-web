package bibliotheque.service;

import bibliotheque.modele.Utilisateur;
import bibliotheque.repository.UtilisateurRepository;
import bibliotheque.util.PaginationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository userRepo;

    public List<Utilisateur> getTousLesUtilisateurs() { return userRepo.findAll(); }

    public List<Utilisateur> rechercher(String motCle) {
        if (motCle == null || motCle.isBlank()) return userRepo.findAll();
        return userRepo.findByNomContainingIgnoreCaseOrderByNomAsc(motCle);
    }

    public Utilisateur trouverParId(Long id) { return userRepo.findById(id).orElse(null); }

    public Utilisateur trouverParEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    public Utilisateur ajouter(Utilisateur user) { return userRepo.save(user); }

    public void supprimer(Long id) { userRepo.deleteById(id); }

    public boolean emailExiste(String email) { return userRepo.findByEmail(email).isPresent(); }

    /**
     * Recherche avancée paginée avec tri.
     */
    public PaginationInfo<Utilisateur> rechercherAvancee(
            String nom, String email, String role,
            int page, int taille, String triChamp, String triOrdre) {

        String n = (nom == null || nom.isBlank()) ? "" : nom.trim();
        String e = (email == null || email.isBlank()) ? "" : email.trim();
        String r = (role == null || role.isBlank()) ? "*" : role.trim();

        Sort.Direction direction = "desc".equalsIgnoreCase(triOrdre)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        String champ = (triChamp == null || triChamp.isBlank()) ? "nom" : triChamp;
        Sort sort = Sort.by(direction, champ);

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), taille, sort);

        Page<Utilisateur> resultat = userRepo.rechercherAvancee(n, e, r, pageable);

        return new PaginationInfo<>(
                resultat.getContent(),
                page,
                resultat.getTotalPages(),
                (int) resultat.getTotalElements(),
                taille
        );
    }
}