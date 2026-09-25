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
     * ⚠️ Les paramètres doivent être NON-NULL (valeurs par défaut passées depuis le service).
     */
    @Query("""
        SELECT l FROM Livre l
        WHERE LOWER(l.titre) LIKE LOWER(CONCAT('%', :titre, '%'))
          AND LOWER(l.auteur) LIKE LOWER(CONCAT('%', :auteur, '%'))
          AND (:categorie = '*' OR l.categorie = :categorie)
          AND l.anneePublication >= :anneeMin
          AND l.anneePublication <= :anneeMax
        """)
    Page<Livre> rechercherAvancee(
            @Param("titre") String titre,
            @Param("auteur") String auteur,
            @Param("categorie") String categorie,
            @Param("anneeMin") Integer anneeMin,
            @Param("anneeMax") Integer anneeMax,
            Pageable pageable);
}