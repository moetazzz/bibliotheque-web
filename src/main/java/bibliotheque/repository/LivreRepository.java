package bibliotheque.repository;

import bibliotheque.modele.Livre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivreRepository extends JpaRepository<Livre, Long> {

    List<Livre> findByTitreContainingIgnoreCaseOrderByTitreAsc(String motCle);

    /**
     * Recherche multi-critères paginée.
     * Tous les paramètres sont optionnels (null = ignoré).
     */
    @Query("""
        SELECT l FROM Livre l
        WHERE (:titre IS NULL OR LOWER(l.titre) LIKE LOWER(CONCAT('%', :titre, '%')))
          AND (:auteur IS NULL OR LOWER(l.auteur) LIKE LOWER(CONCAT('%', :auteur, '%')))
          AND (:categorie IS NULL OR l.categorie = :categorie)
          AND (:anneeMin IS NULL OR l.anneePublication >= :anneeMin)
          AND (:anneeMax IS NULL OR l.anneePublication <= :anneeMax)
        """)
    Page<Livre> rechercherAvancee(
            @Param("titre") String titre,
            @Param("auteur") String auteur,
            @Param("categorie") String categorie,
            @Param("anneeMin") Integer anneeMin,
            @Param("anneeMax") Integer anneeMax,
            Pageable pageable);
}