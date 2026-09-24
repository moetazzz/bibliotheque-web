package bibliotheque.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_livre", nullable = false)
    private Livre livre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "date_reservation", nullable = false)
    private LocalDate dateReservation;

    @Column(name = "date_expiration", nullable = false)
    private LocalDate dateExpiration;

    @Column(nullable = false, length = 20)
    private String statut;

    public Reservation() {}

    public Reservation(Livre livre, Utilisateur utilisateur, int joursValidite) {
        this.livre = livre;
        this.utilisateur = utilisateur;
        this.dateReservation = LocalDate.now();
        this.dateExpiration = LocalDate.now().plusDays(joursValidite);
        this.statut = "EN_ATTENTE";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Livre getLivre() { return livre; }
    public void setLivre(Livre livre) { this.livre = livre; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public LocalDate getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDate dateReservation) { this.dateReservation = dateReservation; }
    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Transient
    public boolean estActive() {
        return "EN_ATTENTE".equals(statut) || "DISPONIBLE".equals(statut);
    }

    @Transient
    public String getStatutLibelle() {
        return switch (statut) {
            case "EN_ATTENTE" -> "⏳ En attente";
            case "DISPONIBLE" -> "✅ Disponible";
            case "ANNULEE" -> "❌ Annulée";
            case "EXPIREE" -> "⌛ Expirée";
            case "CONVERTIE" -> "🎉 Empruntée";
            default -> statut;
        };
    }

    @Transient
    public boolean estExpiree() {
        return "EN_ATTENTE".equals(statut) && LocalDate.now().isAfter(dateExpiration);
    }
}