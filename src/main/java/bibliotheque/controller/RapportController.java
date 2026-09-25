package bibliotheque.controller;

import bibliotheque.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/rapports")
public class RapportController {

    @Autowired private PdfService pdfService;
    @Autowired private LivreService livreService;
    @Autowired private UtilisateurService userService;
    @Autowired private EmpruntService empruntService;
    @Autowired private AuditService auditService;

    private static final DateTimeFormatter FICHIER_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping
    public String page() {
        return "rapports";
    }

    @GetMapping("/livres/pdf")
    public ResponseEntity<byte[]> rapportLivres(Authentication auth) throws Exception {
        byte[] pdf = pdfService.genererRapportLivres(livreService.getTousLesLivres());

        auditService.enregistrer(auth.getName(), null, "EXPORT_PDF",
                "Rapport livres");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"livres-" + LocalDate.now().format(FICHIER_FMT) + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/utilisateurs/pdf")
    public ResponseEntity<byte[]> rapportUtilisateurs(Authentication auth) throws Exception {
        byte[] pdf = pdfService.genererRapportUtilisateurs(userService.getTousLesUtilisateurs());

        auditService.enregistrer(auth.getName(), null, "EXPORT_PDF",
                "Rapport utilisateurs");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"utilisateurs-" + LocalDate.now().format(FICHIER_FMT) + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/emprunts/pdf")
    public ResponseEntity<byte[]> rapportEmprunts(Authentication auth) throws Exception {
        byte[] pdf = pdfService.genererRapportEmprunts(empruntService.getEmpruntsEnCours());

        auditService.enregistrer(auth.getName(), null, "EXPORT_PDF",
                "Rapport emprunts");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"emprunts-" + LocalDate.now().format(FICHIER_FMT) + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/amendes/pdf")
    public ResponseEntity<byte[]> rapportAmendes(Authentication auth) throws Exception {
        byte[] pdf = pdfService.genererRapportAmendes(empruntService.getToutesLesAmendes());

        auditService.enregistrer(auth.getName(), null, "EXPORT_PDF",
                "Rapport amendes");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"amendes-" + LocalDate.now().format(FICHIER_FMT) + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}