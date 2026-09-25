package bibliotheque.service;

import bibliotheque.modele.Emprunt;
import bibliotheque.modele.Livre;
import bibliotheque.modele.Reservation;
import bibliotheque.modele.Utilisateur;
import bibliotheque.repository.EmpruntRepository;
import bibliotheque.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private static final int JOURS_VALIDITE = 7;

    @Autowired private ReservationRepository reservationRepo;
    @Autowired private EmpruntRepository empruntRepo;
    @Autowired private NotificationService notificationService;

    // ==================== LECTURE ====================

    public List<Reservation> getReservationsUtilisateur(Utilisateur user) {
        return reservationRepo.findByUtilisateurOrderByDateReservationDesc(user);
    }

    public List<Reservation> getToutesLesReservationsActives() {
        return reservationRepo.findByStatutInOrderByDateReservationAsc(
                List.of("EN_ATTENTE", "DISPONIBLE"));
    }

    public List<Reservation> getReservationsActivesPourLivre(Livre livre) {
        return reservationRepo.findByLivreAndStatutInOrderByDateReservationAsc(
                livre, List.of("EN_ATTENTE", "DISPONIBLE"));
    }

    public long countReservationsActives(Livre livre) {
        return reservationRepo.countByLivreAndStatutIn(
                livre, List.of("EN_ATTENTE", "DISPONIBLE"));
    }

    // ==================== CRÉER UNE RÉSERVATION ====================

    @Transactional
    public Reservation reserver(Livre livre, Utilisateur utilisateur) {
        // Vérifier que le livre est bien emprunté
        Emprunt empruntEnCours = empruntRepo.findByLivreAndDateRetourEffectiveIsNull(livre);
        if (empruntEnCours == null) {
            throw new RuntimeException(
                    "Ce livre est déjà disponible, vous pouvez l'emprunter directement");
        }

        // Vérifier que ce n'est pas lui qui l'a emprunté
        if (empruntEnCours.getUtilisateur().getId().equals(utilisateur.getId())) {
            throw new RuntimeException("Vous avez déjà ce livre emprunté");
        }

        // Vérifier qu'il n'a pas déjà réservé
        boolean dejaReserve = reservationRepo.existsByLivreAndUtilisateurAndStatutIn(
                livre, utilisateur, List.of("EN_ATTENTE", "DISPONIBLE"));
        if (dejaReserve) {
            throw new RuntimeException("Vous avez déjà réservé ce livre");
        }

        Reservation reservation = new Reservation(livre, utilisateur, JOURS_VALIDITE);
        Reservation saved = reservationRepo.save(reservation);

        log.info("Réservation créée : {} pour {}", livre.getTitre(), utilisateur.getNom());
        return saved;
    }

    // ==================== ANNULER ====================

    @Transactional
    public void annuler(Long reservationId, Utilisateur user) {
        Reservation r = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

        boolean estBibliothecaire = "BIB".equals(user.getRole());
        if (!estBibliothecaire && !r.getUtilisateur().getId().equals(user.getId())) {
            throw new RuntimeException("Vous ne pouvez annuler que vos réservations");
        }

        if (!r.estActive()) {
            throw new RuntimeException("Cette réservation n'est plus active");
        }

        r.setStatut("ANNULEE");
        reservationRepo.save(r);
    }

    // ==================== MARQUER DISPONIBLE ====================

    @Transactional
    public void notifierRetour(Livre livre) {
        List<Reservation> enAttente = reservationRepo.findByLivreAndStatutInOrderByDateReservationAsc(
                livre, List.of("EN_ATTENTE"));

        if (!enAttente.isEmpty()) {
            Reservation prochaine = enAttente.get(0);
            prochaine.setStatut("DISPONIBLE");
            prochaine.setDateExpiration(LocalDate.now().plusDays(JOURS_VALIDITE));
            reservationRepo.save(prochaine);

            // Envoyer la notification
            notificationService.notifierReservationDisponible(prochaine);

            log.info("Livre {} disponible pour {}",
                livre.getTitre(), prochaine.getUtilisateur().getNom());
            }
    }

    // ==================== SUPPRIMER ====================

    public void supprimer(Long id) {
        reservationRepo.deleteById(id);
    }
}