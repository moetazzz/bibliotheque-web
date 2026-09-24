package bibliotheque.modele;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jakarta.persistence.Transient;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 4, message = "Minimum 4 caractères")
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @NotBlank(message = "Le rôle est obligatoire")
    @Column(nullable = false)
    private String role;  // "MEMBRE" ou "BIB"

    @Column(name = "solde_amendes")
    private Double soldeAmendes = 0.0;

    public Utilisateur() {}

    public Utilisateur(String nom, String email, String motDePasse, String role) {
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.soldeAmendes = 0.0;
    }

    // ==================== Getters/Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Double getSoldeAmendes() { return soldeAmendes; }
    public void setSoldeAmendes(Double soldeAmendes) { this.soldeAmendes = soldeAmendes; }

    // Méthode utilitaire pour l'affichage (NON mappée en BDD)
    @Transient
    public String getRoleLibelle() {
        return "BIB".equals(role) ? "Bibliothécaire" : "Membre";
    }
}