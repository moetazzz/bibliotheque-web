package bibliotheque.repository;

import bibliotheque.modele.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    List<Utilisateur> findByNomContainingIgnoreCaseOrderByNomAsc(String motCle);

    /**
     * Recherche avancée paginée pour utilisateurs.
     * ⚠️ Paramètres NON-NULL (valeurs par défaut passées depuis le service).
     */
    @Query("""
        SELECT u FROM Utilisateur u
        WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :nom, '%'))
          AND LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))
          AND (:role = '*' OR u.role = :role)
        """)
    Page<Utilisateur> rechercherAvancee(
            @Param("nom") String nom,
            @Param("email") String email,
            @Param("role") String role,
            Pageable pageable);
}