package bibliotheque.repository;

import bibliotheque.modele.Amende;
import bibliotheque.modele.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmendeRepository extends JpaRepository<Amende, Long> {

    List<Amende> findByUtilisateurOrderByDateCreationDesc(Utilisateur utilisateur);

    List<Amende> findByPayeeFalseOrderByDateCreationDesc();

    long countByUtilisateurAndPayeeFalse(Utilisateur utilisateur);

    List<Amende> findAllByOrderByDateCreationDesc();
}