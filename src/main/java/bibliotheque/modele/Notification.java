package bibliotheque.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_utilisateur", nullable = false)
    private Long idUtilisateur;

    @Column(nullable = false, length = 30)
    private String type;  // RAPPEL_RETOUR, RETARD, RESERVATION_DISPONIBLE, AMENDE, INFO

    @Column(nullable = false)
    private String titre;

    @Column(length = 1000, nullable = false)
    private String message;

    @Column(nullable = false)
    private Boolean lue = false;

    @Column(name = "email_envoye", nullable = false)
    private Boolean emailEnvoye = false;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    public Notification() {}

    public Notification(Long idUtilisateur, String type, String titre, String message) {
        this.idUtilisateur = idUtilisateur;
        this.type = type;
        this.titre = titre;
        this.message = message;
        this.lue = false;
        this.emailEnvoye = false;
        this.dateCreation = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(Long idUtilisateur) { this.idUtilisateur = idUtilisateur; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getLue() { return lue; }
    public void setLue(Boolean lue) { this.lue = lue; }
    public Boolean getEmailEnvoye() { return emailEnvoye; }
    public void setEmailEnvoye(Boolean emailEnvoye) { this.emailEnvoye = emailEnvoye; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Transient
    public String getIcone() {
        return switch (type) {
            case "RAPPEL_RETOUR" -> "⏰";
            case "RETARD" -> "⚠️";
            case "RESERVATION_DISPONIBLE" -> "🎉";
            case "AMENDE" -> "💰";
            default -> "ℹ️";
        };
    }

    @Transient
    public String getCouleur() {
        return switch (type) {
            case "RAPPEL_RETOUR" -> "warning";
            case "RETARD" -> "danger";
            case "RESERVATION_DISPONIBLE" -> "success";
            case "AMENDE" -> "danger";
            default -> "info";
        };
    }
}