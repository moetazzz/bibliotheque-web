package bibliotheque.repository;

import bibliotheque.modele.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByIdUtilisateurOrderByDateCreationDesc(Long idUtilisateur);

    List<Notification> findByIdUtilisateurAndLueFalseOrderByDateCreationDesc(Long idUtilisateur);

    long countByIdUtilisateurAndLueFalse(Long idUtilisateur);

    List<Notification> findAllByOrderByDateCreationDesc();
}