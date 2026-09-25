package bibliotheque.repository;

import bibliotheque.modele.Emprunt;
import bibliotheque.modele.Livre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {

    List<Emprunt> findByDateRetourEffectiveIsNullOrderByDateRetourPrevueAsc();

    List<Emprunt> findAllByOrderByDateEmpruntDesc();

    Emprunt findByLivreAndDateRetourEffectiveIsNull(Livre livre);

    long countByUtilisateurIdAndDateRetourEffectiveIsNull(Long utilisateurId);

    /**
     * Recherche avancée paginée pour emprunts.
     * @param statut : "TOUS", "EN_COURS", "RENDU"
     */
    @Query("""
        SELECT e FROM Emprunt e
        WHERE (:statut = 'TOUS'
               OR (:statut = 'EN_COURS' AND e.dateRetourEffective IS NULL)
               OR (:statut = 'RENDU' AND e.dateRetourEffective IS NOT NULL))
          AND (:idUtilisateur IS NULL OR e.utilisateur.id = :idUtilisateur)
        """)
    Page<Emprunt> rechercherAvancee(
            @Param("statut") String statut,
            @Param("idUtilisateur") Long idUtilisateur,
            Pageable pageable);
}