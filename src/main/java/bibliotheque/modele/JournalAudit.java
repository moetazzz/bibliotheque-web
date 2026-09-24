package bibliotheque.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_audit")
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction;

    @Column(name = "id_utilisateur")
    private Long idUtilisateur;

    @Column(nullable = false)
    private String action;

    @Column(length = 500)
    private String details;

    @Column(name = "nom_utilisateur")
    private String nomUtilisateur;

    public JournalAudit() {}

    public JournalAudit(String nomUtilisateur, Long idUtilisateur, String action, String details) {
        this.dateAction = LocalDateTime.now();
        this.nomUtilisateur = nomUtilisateur;
        this.idUtilisateur = idUtilisateur;
        this.action = action;
        this.details = details;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }

    public Long getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(Long idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }
}