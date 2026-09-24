package bibliotheque.service;

import bibliotheque.modele.JournalAudit;
import bibliotheque.repository.JournalAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private JournalAuditRepository auditRepo;

    /**
     * Enregistre une action dans le journal d'audit.
     */
    public void enregistrer(String nomUtilisateur, Long idUtilisateur, String action, String details) {
        try {
            JournalAudit entry = new JournalAudit(nomUtilisateur, idUtilisateur, action, details);
            auditRepo.save(entry);
            log.debug("AUDIT | {} | {} | {}", nomUtilisateur, action, details);
        } catch (Exception e) {
            log.error("Impossible d'enregistrer l'audit", e);
        }
    }

    public List<JournalAudit> getToutLeJournal() {
        return auditRepo.findAllByOrderByDateActionDesc();
    }

    public long compterActions() {
        return auditRepo.count();
    }
}