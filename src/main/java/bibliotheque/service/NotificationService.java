package bibliotheque.service;

import bibliotheque.modele.Emprunt;
import bibliotheque.modele.Notification;
import bibliotheque.modele.Reservation;
import bibliotheque.modele.Utilisateur;
import bibliotheque.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired private NotificationRepository notificationRepo;
    @Autowired private EmailService emailService;

    // ==================== LECTURE ====================

    public List<Notification> getNotificationsUtilisateur(Long idUser) {
        return notificationRepo.findByIdUtilisateurOrderByDateCreationDesc(idUser);
    }

    public List<Notification> getNotificationsNonLues(Long idUser) {
        return notificationRepo.findByIdUtilisateurAndLueFalseOrderByDateCreationDesc(idUser);
    }

    public long countNonLues(Long idUser) {
        return notificationRepo.countByIdUtilisateurAndLueFalse(idUser);
    }

    public List<Notification> getToutesLesNotifications() {
        return notificationRepo.findAllByOrderByDateCreationDesc();
    }

    // ==================== CRÉER UNE NOTIFICATION ====================

    @Transactional
    public Notification creer(Utilisateur user, String type, String titre, String message,
                              String templateName, Context emailContext) {
        // 1. Sauvegarder en BDD
        Notification notif = new Notification(user.getId(), type, titre, message);
        notificationRepo.save(notif);

        // 2. Tenter d'envoyer l'email
        if (user.getEmail() != null && templateName != null) {
            boolean envoye = emailService.envoyer(user.getEmail(), titre, templateName, emailContext);
            notif.setEmailEnvoye(envoye);
            notificationRepo.save(notif);
        }

        log.debug("Notification créée pour {} : {}", user.getNom(), titre);
        return notif;
    }

    // ==================== RAPPEL DE RETOUR ====================

    public void notifierRappelRetour(Emprunt emprunt) {
        Utilisateur user = emprunt.getUtilisateur();

        Context ctx = new Context();
        ctx.setVariable("nom", user.getNom());
        ctx.setVariable("livre", emprunt.getLivre().getTitre());
        ctx.setVariable("dateRetour", emprunt.getDateRetourPrevue());
        ctx.setVariable("joursRestants", emprunt.joursDeRetard() == 0
                ? java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(),
                    emprunt.getDateRetourPrevue())
                : 0);

        String titre = "⏰ Rappel : livre à rendre bientôt";
        String message = "Le livre '" + emprunt.getLivre().getTitre()
                + "' doit être rendu avant le " + emprunt.getDateRetourPrevue() + ".";

        creer(user, "RAPPEL_RETOUR", titre, message, "rappel-retour", ctx);
    }

    // ==================== RETARD ====================

    public void notifierRetard(Emprunt emprunt) {
        Utilisateur user = emprunt.getUtilisateur();

        Context ctx = new Context();
        ctx.setVariable("nom", user.getNom());
        ctx.setVariable("livre", emprunt.getLivre().getTitre());
        ctx.setVariable("dateRetour", emprunt.getDateRetourPrevue());
        ctx.setVariable("joursRetard", emprunt.joursDeRetard());
        ctx.setVariable("amende", emprunt.joursDeRetard() * 0.50);

        String titre = "⚠️ Retard : livre non rendu";
        String message = "Le livre '" + emprunt.getLivre().getTitre()
                + "' est en retard de " + emprunt.joursDeRetard() + " jour(s). "
                + "Amende actuelle : " + String.format("%.2f", emprunt.joursDeRetard() * 0.50) + "€";

        creer(user, "RETARD", titre, message, "retard", ctx);
    }

    // ==================== RÉSERVATION DISPONIBLE ====================

    public void notifierReservationDisponible(Reservation reservation) {
        Utilisateur user = reservation.getUtilisateur();

        Context ctx = new Context();
        ctx.setVariable("nom", user.getNom());
        ctx.setVariable("livre", reservation.getLivre().getTitre());
        ctx.setVariable("dateExpiration", reservation.getDateExpiration());

        String titre = "🎉 Votre réservation est disponible !";
        String message = "Le livre '" + reservation.getLivre().getTitre()
                + "' que vous avez réservé est maintenant disponible. "
                + "Vous avez jusqu'au " + reservation.getDateExpiration() + " pour venir l'emprunter.";

        creer(user, "RESERVATION_DISPONIBLE", titre, message, "reservation-disponible", ctx);
    }

    // ==================== AMENDE ====================

    public void notifierAmende(Utilisateur user, double montant, String livre) {
        Context ctx = new Context();
        ctx.setVariable("nom", user.getNom());
        ctx.setVariable("montant", String.format("%.2f", montant));
        ctx.setVariable("livre", livre);

        String titre = "💰 Nouvelle amende";
        String message = "Une amende de " + String.format("%.2f", montant)
                + "€ a été ajoutée pour le livre '" + livre + "'.";

        creer(user, "AMENDE", titre, message, "amende", ctx);
    }

    // ==================== MARQUER COMME LUE ====================

    @Transactional
    public void marquerCommeLue(Long id) {
        Notification n = notificationRepo.findById(id).orElse(null);
        if (n != null) {
            n.setLue(true);
            notificationRepo.save(n);
        }
    }

    @Transactional
    public void marquerToutesCommeLues(Long idUser) {
        List<Notification> nonLues = notificationRepo
                .findByIdUtilisateurAndLueFalseOrderByDateCreationDesc(idUser);
        for (Notification n : nonLues) {
            n.setLue(true);
        }
        notificationRepo.saveAll(nonLues);
    }

    // ==================== SUPPRIMER ====================

    public void supprimer(Long id) {
        notificationRepo.deleteById(id);
    }
}