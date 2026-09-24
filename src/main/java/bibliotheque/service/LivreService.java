package bibliotheque.service;

import bibliotheque.modele.Livre;
import bibliotheque.repository.LivreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LivreService {

    @Autowired
    private LivreRepository livreRepo;

    public List<Livre> getTousLesLivres() { return livreRepo.findAll(); }

    public List<Livre> rechercher(String motCle) {
        if (motCle == null || motCle.isBlank()) return livreRepo.findAll();
        return livreRepo.findByTitreContainingIgnoreCaseOrderByTitreAsc(motCle);
    }

    public Livre trouverParId(Long id) { return livreRepo.findById(id).orElse(null); }

    public Livre ajouter(Livre livre) { return livreRepo.save(livre); }

    public void supprimer(Long id) { livreRepo.deleteById(id); }
}