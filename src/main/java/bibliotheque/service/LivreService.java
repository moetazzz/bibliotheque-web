package bibliotheque.service;

import bibliotheque.modele.Livre;
import bibliotheque.repository.LivreRepository;
import bibliotheque.util.PaginationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LivreService {

    @Autowired
    private LivreRepository livreRepo;

    public List<Livre> getTousLesLivres() { return livreRepo.findAll(); }

    public List<Livre> rechercher(String motCle) {
        if (motCle == null || motCle.isBlank()) return livreRepo.findAll();
        return livreRepo.findByTitreContainingIgnoreCaseOrderByTitreAsc(motCle);
    }

    public Livre trouverParId(Long id) { return livreRepo.findById(id).orElse(null); }

    public Livre ajouter(Livre livre) { return livreRepo.save(livre); }

    public void supprimer(Long id) { livreRepo.deleteById(id); }

    /**
     * Recherche avancée paginée avec tri.
     * ⚠️ On passe des valeurs par défaut NON-NULL à la requête
     * pour éviter le bug Hibernate 6 avec ':param IS NULL'.
     */
    public PaginationInfo<Livre> rechercherAvancee(
            String titre, String auteur, String categorie,
            Integer anneeMin, Integer anneeMax,
            int page, int taille, String triChamp, String triOrdre) {

        // Valeurs par défaut non-null
        String t = (titre == null || titre.isBlank()) ? "" : titre.trim();
        String a = (auteur == null || auteur.isBlank()) ? "" : auteur.trim();
        String c = (categorie == null || categorie.isBlank()) ? "*" : categorie.trim();
        Integer min = (anneeMin == null) ? 0 : anneeMin;
        Integer max = (anneeMax == null) ? 9999 : anneeMax;

        // Tri
        Sort.Direction direction = "desc".equalsIgnoreCase(triOrdre)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        String champ = (triChamp == null || triChamp.isBlank()) ? "titre" : triChamp;
        Sort sort = Sort.by(direction, champ);

        // Page (Spring commence à 0)
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), taille, sort);

        Page<Livre> resultat = livreRepo.rechercherAvancee(t, a, c, min, max, pageable);

        return new PaginationInfo<>(
                resultat.getContent(),
                page,
                resultat.getTotalPages(),
                (int) resultat.getTotalElements(),
                taille
        );
    }
}