package bibliotheque.service;

import bibliotheque.modele.Amende;
import bibliotheque.modele.Emprunt;
import bibliotheque.modele.Livre;
import bibliotheque.modele.Utilisateur;
import bibliotheque.repository.AmendeRepository;
import bibliotheque.repository.EmpruntRepository;
import bibliotheque.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmpruntService {

    // Règles métier
    private static final int DUREE_EMPRUNT_JOURS = 14;
    private static final int LIMITE_EMPRUNTS = 3;
    private static final int DUREE_PROLONGATION_JOURS = 7;
    private static final int MAX_PROLONGATIONS = 1;
    private static final double AMENDE_PAR_JOUR = 0.50;
    private static final double SEUIL_BLACKLIST = 10.0;

    @Autowired
    private EmpruntRepository empruntRepo;

    @Autowired
    private AmendeRepository amendeRepo;

    @Autowired
    private UtilisateurRepository userRepo;

    // ==================== LECTURE ====================

    public List<Emprunt> getEmpruntsEnCours() {
        return empruntRepo.findByDateRetourEffectiveIsNullOrderByDateRetourPrevueAsc();
    }

    public List<Emprunt> getTousLesEmprunts() {
        return empruntRepo.findAllByOrderByDateEmpruntDesc();
    }

    public Emprunt trouverParId(Long id) {
        return empruntRepo.findById(id).orElse(null);
    }

    public boolean estDisponible(Livre livre) {
        return empruntRepo.findByLivreAndDateRetourEffectiveIsNull(livre) == null;
    }

    public long countEnCours(Utilisateur user) {
        return empruntRepo.countByUtilisateurIdAndDateRetourEffectiveIsNull(user.getId());
    }

    // ==================== EMPRUNTER ====================

    public Emprunt emprunter(Livre livre, Utilisateur utilisateur) {
        // Vérification 1 : livre disponible ?
        if (!estDisponible(livre)) {
            throw new RuntimeException("Le livre '" + livre.getTitre() + "' est déjà emprunté");
        }

        // Vérification 2 : blacklist amendes
        if (utilisateur.getSoldeAmendes() != null && utilisateur.getSoldeAmendes() >= SEUIL_BLACKLIST) {
            throw new RuntimeException(
                "Amendes impayées : " + String.format("%.2f", utilisateur.getSoldeAmendes()) + "€. Emprunt bloqué."
            );
        }

        // Vérification 3 : limite d'emprunts
        if (countEnCours(utilisateur) >= LIMITE_EMPRUNTS) {
            throw new RuntimeException("Limite de " + LIMITE_EMPRUNTS + " emprunts simultanés atteinte");
        }

        LocalDate today = LocalDate.now();
        Emprunt emprunt = new Emprunt(livre, utilisateur, today, today.plusDays(DUREE_EMPRUNT_JOURS));
        return empruntRepo.save(emprunt);
    }

    // ==================== RETOURNER (avec amende auto) ====================

    @Transactional
    public Emprunt retourner(Long empruntId) {
        Emprunt emprunt = empruntRepo.findById(empruntId)
                .orElseThrow(() -> new RuntimeException("Emprunt introuvable"));

        if (emprunt.estRendu()) {
            throw new RuntimeException("Ce livre a déjà été rendu");
        }

        emprunt.setDateRetourEffective(LocalDate.now());
        empruntRepo.save(emprunt);

        // Créer une amende si retard
        long joursRetard = emprunt.joursDeRetard();
        if (joursRetard > 0) {
            creerAmende(emprunt, joursRetard);
        }

        return emprunt;
    }

    // ==================== PROLONGER ====================

    public Emprunt prolonger(Long empruntId) {
        Emprunt emprunt = empruntRepo.findById(empruntId)
                .orElseThrow(() -> new RuntimeException("Emprunt introuvable"));

        if (emprunt.estRendu()) {
            throw new RuntimeException("Ce livre a déjà été rendu");
        }

        if (emprunt.getProlongations() >= MAX_PROLONGATIONS) {
            throw new RuntimeException("Maximum de prolongations atteint (1)");
        }

        // Vérifier si le livre est réservé par quelqu'un d'autre (à ajouter plus tard)

        emprunt.setDateRetourPrevue(emprunt.getDateRetourPrevue().plusDays(DUREE_PROLONGATION_JOURS));
        emprunt.setProlongations(emprunt.getProlongations() + 1);
        return empruntRepo.save(emprunt);
    }

    // ==================== AMENDES ====================

    private void creerAmende(Emprunt emprunt, long joursRetard) {
        double montant = joursRetard * AMENDE_PAR_JOUR;

        Amende amende = new Amende(emprunt.getUtilisateur(), emprunt, montant);
        amendeRepo.save(amende);

        // Mettre à jour le solde de l'utilisateur
        Utilisateur user = emprunt.getUtilisateur();
        Double soldeActuel = user.getSoldeAmendes() == null ? 0.0 : user.getSoldeAmendes();
        user.setSoldeAmendes(soldeActuel + montant);
        userRepo.save(user);
    }

    public List<Amende> getAmendesUtilisateur(Utilisateur user) {
        return amendeRepo.findByUtilisateurOrderByDateCreationDesc(user);
    }

    public List<Amende> getAmendesImpayees() {
        return amendeRepo.findByPayeeFalseOrderByDateCreationDesc();
    }

    public List<Amende> getToutesLesAmendes() {
        return amendeRepo.findAllByOrderByDateCreationDesc();
    }

    @Transactional
    public void payerAmendes(Utilisateur user) {
        List<Amende> amendes = amendeRepo.findByUtilisateurOrderByDateCreationDesc(user);
        for (Amende amende : amendes) {
            if (!amende.getPayee()) {
                amende.setPayee(true);
                amendeRepo.save(amende);
            }
        }
        user.setSoldeAmendes(0.0);
        userRepo.save(user);
    }

    // ==================== SUPPRESSION ====================

    public void supprimer(Long id) {
        empruntRepo.deleteById(id);
    }
}