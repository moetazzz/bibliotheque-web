package bibliotheque.repository;

import bibliotheque.modele.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivreRepository extends JpaRepository<Livre, Long> {

    // Méthodes CRUD gratuites : save(), findAll(), findById(), deleteById()...

    // Méthode personnalisée — Spring génère le SQL tout seul !
    List<Livre> findByTitreContainingIgnoreCaseOrderByTitreAsc(String motCle);
}