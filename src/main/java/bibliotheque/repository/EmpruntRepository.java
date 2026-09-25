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

    // ==================== STATISTIQUES ====================

    /** Nombre d'emprunts par mois sur les N derniers mois. Retourne [annee, mois, count] */
    @Query(value = """
        SELECT EXTRACT(YEAR FROM date_emprunt) as annee,
               EXTRACT(MONTH FROM date_emprunt) as mois,
               COUNT(*) as total
        FROM emprunts
        WHERE date_emprunt >= CURRENT_DATE - INTERVAL '12 months'
        GROUP BY annee, mois
        ORDER BY annee, mois
        """, nativeQuery = true)
    List<Object[]> countEmpruntsParMois();

    /** Top 5 livres les plus empruntés. Retourne [titre, count] */
    @Query(value = """
        SELECT l.titre, COUNT(e.id) as total
        FROM emprunts e
        JOIN livres l ON l.id = e.id_livre
        GROUP BY l.id, l.titre
        ORDER BY total DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> top5Livres();

    /** Top 5 utilisateurs les plus actifs. Retourne [nom, count] */
    @Query(value = """
        SELECT u.nom, COUNT(e.id) as total
        FROM emprunts e
        JOIN utilisateurs u ON u.id = e.id_utilisateur
        GROUP BY u.id, u.nom
        ORDER BY total DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> top5Utilisateurs();

    /** Répartition des emprunts par catégorie. Retourne [categorie, count] */
    @Query(value = """
        SELECT l.categorie, COUNT(e.id) as total
        FROM emprunts e
        JOIN livres l ON l.id = e.id_livre
        WHERE l.categorie IS NOT NULL
        GROUP BY l.categorie
        ORDER BY total DESC
        """, nativeQuery = true)
    List<Object[]> countParCategorie();

    /** Nombre d'emprunts en retard */
    @Query("""
        SELECT COUNT(e) FROM Emprunt e
        WHERE e.dateRetourEffective IS NULL
          AND e.dateRetourPrevue < CURRENT_DATE
        """)
    long countEnRetard();
}