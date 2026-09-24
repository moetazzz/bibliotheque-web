package bibliotheque.modele;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "emprunts")
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_livre", nullable = false)
    private Livre livre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "date_emprunt", nullable = false)
    private LocalDate dateEmprunt;

    @Column(name = "date_retour_prevue", nullable = false)
    private LocalDate dateRetourPrevue;

    @Column(name = "date_retour_effective")
    private LocalDate dateRetourEffective;

    @Column(nullable = false)
    private Integer prolongations = 0;

    public Emprunt() {}

    public Emprunt(Livre livre, Utilisateur utilisateur, LocalDate dateEmprunt, LocalDate dateRetourPrevue) {
        this.livre = livre;
        this.utilisateur = utilisateur;
        this.dateEmprunt = dateEmprunt;
        this.dateRetourPrevue = dateRetourPrevue;
        this.prolongations = 0;
    }

    public boolean estRendu() { return dateRetourEffective != null; }

    public long joursDeRetard() {
        LocalDate ref = estRendu() ? dateRetourEffective : LocalDate.now();
        if (ref.isAfter(dateRetourPrevue)) {
            return ChronoUnit.DAYS.between(dateRetourPrevue, ref);
        }
        return 0;
    }

    public String getStatut() {
        if (estRendu()) return "RENDU";
        if (joursDeRetard() > 0) return "EN_RETARD";
        return "EN_COURS";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Livre getLivre() { return livre; }
    public void setLivre(Livre livre) { this.livre = livre; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public LocalDate getDateEmprunt() { return dateEmprunt; }
    public void setDateEmprunt(LocalDate dateEmprunt) { this.dateEmprunt = dateEmprunt; }
    public LocalDate getDateRetourPrevue() { return dateRetourPrevue; }
    public void setDateRetourPrevue(LocalDate dateRetourPrevue) { this.dateRetourPrevue = dateRetourPrevue; }
    public LocalDate getDateRetourEffective() { return dateRetourEffective; }
    public void setDateRetourEffective(LocalDate dateRetourEffective) { this.dateRetourEffective = dateRetourEffective; }
    public Integer getProlongations() { return prolongations; }
    public void setProlongations(Integer prolongations) { this.prolongations = prolongations; }
}