package bibliotheque.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "amendes")
public class Amende {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_emprunt", nullable = false)
    private Emprunt emprunt;

    @Column(nullable = false)
    private Double montant;

    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation;

    @Column(nullable = false)
    private Boolean payee = false;

    public Amende() {}

    public Amende(Utilisateur utilisateur, Emprunt emprunt, Double montant) {
        this.utilisateur = utilisateur;
        this.emprunt = emprunt;
        this.montant = montant;
        this.dateCreation = LocalDate.now();
        this.payee = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public Emprunt getEmprunt() { return emprunt; }
    public void setEmprunt(Emprunt emprunt) { this.emprunt = emprunt; }
    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }
    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
    public Boolean getPayee() { return payee; }
    public void setPayee(Boolean payee) { this.payee = payee; }

    @Transient
    public String getStatut() { return payee ? "PAYEE" : "IMPAYEE"; }
}