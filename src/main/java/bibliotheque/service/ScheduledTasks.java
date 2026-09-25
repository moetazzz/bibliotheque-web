package bibliotheque.service;

import bibliotheque.modele.Emprunt;
import bibliotheque.modele.Reservation;
import bibliotheque.repository.EmpruntRepository;
import bibliotheque.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired private EmpruntRepository empruntRepo;
    @Autowired private ReservationRepository reservationRepo;
    @Autowired private NotificationService notificationService;

    /**
     * Envoie les rappels 3 jours avant la date de retour.
     * Exécuté tous les jours à 9h00.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void envoyerRappelsRetour() {
        log.info("🔔 Envoi des rappels de retour...");

        LocalDate cible = LocalDate.now().plusDays(3);
        List<Emprunt> emprunts = empruntRepo
                .findByDateRetourEffectiveIsNullOrderByDateRetourPrevueAsc();

        int count = 0;
        for (Emprunt e : emprunts) {
            long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), e.getDateRetourPrevue());
            if (joursRestants == 3) {
                notificationService.notifierRappelRetour(e);
                count++;
            }
        }
        log.info("✅ {} rappel(s) envoyé(s)", count);
    }

    /**
     * Détecte les retards et notifie.
     * Exécuté tous les jours à 10h00.
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void detecterRetards() {
        log.info("🔔 Détection des retards...");

        List<Emprunt> emprunts = empruntRepo
                .findByDateRetourEffectiveIsNullOrderByDateRetourPrevueAsc();

        int count = 0;
        for (Emprunt e : emprunts) {
            if (e.joursDeRetard() > 0) {
                notificationService.notifierRetard(e);
                count++;
            }
        }
        log.info("✅ {} retard(s) détecté(s)", count);
    }

    /**
     * Expire les réservations dépassées.
     * Exécuté tous les jours à 11h00.
     */
    @Scheduled(cron = "0 0 11 * * *")
    public void expirerReservations() {
        log.info("🔔 Expiration des réservations...");

        List<Reservation> actives = reservationRepo
                .findByStatutInOrderByDateReservationAsc(List.of("EN_ATTENTE", "DISPONIBLE"));

        int count = 0;
        for (Reservation r : actives) {
            if (r.estExpiree() || (r.getStatut().equals("DISPONIBLE")
                    && LocalDate.now().isAfter(r.getDateExpiration()))) {
                r.setStatut("EXPIREE");
                reservationRepo.save(r);
                count++;
            }
        }
        log.info("✅ {} réservation(s) expirée(s)", count);
    }
}