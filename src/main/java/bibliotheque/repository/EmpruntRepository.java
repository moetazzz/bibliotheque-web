package bibliotheque.repository;

import bibliotheque.modele.Emprunt;
import bibliotheque.modele.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {

    List<Emprunt> findByDateRetourEffectiveIsNullOrderByDateRetourPrevueAsc();

    List<Emprunt> findAllByOrderByDateEmpruntDesc();

    Emprunt findByLivreAndDateRetourEffectiveIsNull(Livre livre);

    long countByUtilisateurIdAndDateRetourEffectiveIsNull(Long utilisateurId);
}