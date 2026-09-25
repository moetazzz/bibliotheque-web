package bibliotheque.repository;

import bibliotheque.modele.Amende;
import bibliotheque.modele.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmendeRepository extends JpaRepository<Amende, Long> {

    List<Amende> findByUtilisateurOrderByDateCreationDesc(Utilisateur utilisateur);

    List<Amende> findByPayeeFalseOrderByDateCreationDesc();

    List<Amende> findAllByOrderByDateCreationDesc();

    long countByUtilisateurAndPayeeFalse(Utilisateur utilisateur);

    // ==================== STATISTIQUES ====================

    /** Total des amendes impayées (montant cumulé) */
    @Query("SELECT COALESCE(SUM(a.montant), 0) FROM Amende a WHERE a.payee = false")
    Double totalAmendesImpayees();

    /** Total des amendes payées (montant cumulé) */
    @Query("SELECT COALESCE(SUM(a.montant), 0) FROM Amende a WHERE a.payee = true")
    Double totalAmendesPayees();

    /** Amendes par mois (6 derniers mois). Retourne [annee, mois, total] */
    @Query(value = """
        SELECT EXTRACT(YEAR FROM date_creation) as annee,
               EXTRACT(MONTH FROM date_creation) as mois,
               COALESCE(SUM(montant), 0) as total
        FROM amendes
        WHERE date_creation >= CURRENT_DATE - INTERVAL '6 months'
        GROUP BY annee, mois
        ORDER BY annee, mois
        """, nativeQuery = true)
    List<Object[]> amendesParMois();
}