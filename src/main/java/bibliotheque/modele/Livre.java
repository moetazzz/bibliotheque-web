package bibliotheque.modele;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "livres")
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 2, max = 200)
    @Column(nullable = false)
    private String titre;

    @NotBlank(message = "L'auteur est obligatoire")
    @Column(nullable = false)
    private String auteur;

    private String isbn;

    private String categorie;

    @Min(0) @Max(2100)
    @Column(name = "annee_publication")
    private Integer anneePublication;

    public Livre() {}

    public Livre(String titre, String auteur, String isbn, String categorie, Integer annee) {
        this.titre = titre;
        this.auteur = auteur;
        this.isbn = isbn;
        this.categorie = categorie;
        this.anneePublication = annee;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public Integer getAnneePublication() { return anneePublication; }
    public void setAnneePublication(Integer anneePublication) { this.anneePublication = anneePublication; }
}