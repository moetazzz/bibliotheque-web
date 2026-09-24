package bibliotheque.service;

import bibliotheque.modele.Utilisateur;
import bibliotheque.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository userRepo;

    public List<Utilisateur> getTousLesUtilisateurs() { return userRepo.findAll(); }

    public List<Utilisateur> rechercher(String motCle) {
        if (motCle == null || motCle.isBlank()) return userRepo.findAll();
        return userRepo.findByNomContainingIgnoreCaseOrderByNomAsc(motCle);
    }

    public Utilisateur trouverParId(Long id) { return userRepo.findById(id).orElse(null); }

    public Utilisateur trouverParEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    public Utilisateur ajouter(Utilisateur user) { return userRepo.save(user); }

    public void supprimer(Long id) { userRepo.deleteById(id); }

    public boolean emailExiste(String email) { return userRepo.findByEmail(email).isPresent(); }
}