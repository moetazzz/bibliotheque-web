package bibliotheque.repository;

import bibliotheque.modele.Emprunt;
import bibliotheque.modele.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {

    // Emprunts en cours (non rendus)
    List<Emprunt> findByDateRetourEffectiveIsNullOrderByDateRetourPrevueAsc();

    // Tous les emprunts (rendus + en cours), les plus récents d'abord
    List<Emprunt> findAllByOrderByDateEmpruntDesc();

    // L'emprunt en cours pour un livre donné
    Emprunt findByLivreAndDateRetourEffectiveIsNull(Livre livre);

    // Nombre d'emprunts en cours pour un utilisateur
    long countByUtilisateurIdAndDateRetourEffectiveIsNull(Long utilisateurId);
}