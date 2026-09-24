package bibliotheque.repository;

import bibliotheque.modele.Livre;
import bibliotheque.modele.Reservation;
import bibliotheque.modele.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByLivreAndStatutInOrderByDateReservationAsc(
            Livre livre, List<String> statuts);

    List<Reservation> findByUtilisateurOrderByDateReservationDesc(Utilisateur utilisateur);

    List<Reservation> findByStatutInOrderByDateReservationAsc(List<String> statuts);

    boolean existsByLivreAndUtilisateurAndStatutIn(
            Livre livre, Utilisateur utilisateur, List<String> statuts);

    long countByLivreAndStatutIn(Livre livre, List<String> statuts);
}