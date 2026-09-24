package bibliotheque.repository;

import bibliotheque.modele.JournalAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {

    List<JournalAudit> findAllByOrderByDateActionDesc();

    Page<JournalAudit> findAllByOrderByDateActionDesc(Pageable pageable);
}