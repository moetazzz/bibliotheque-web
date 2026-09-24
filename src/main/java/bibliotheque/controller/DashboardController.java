package bibliotheque.controller;

import bibliotheque.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired private LivreService livreService;
    @Autowired private UtilisateurService userService;
    @Autowired private EmpruntService empruntService;
    @Autowired private AuditService auditService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("nbLivres", livreService.getTousLesLivres().size());
        model.addAttribute("nbUtilisateurs", userService.getTousLesUtilisateurs().size());
        model.addAttribute("nbEmpruntsEnCours", empruntService.getEmpruntsEnCours().size());
        model.addAttribute("nbAmendesImpayees", empruntService.getAmendesImpayees().size());
        model.addAttribute("derniersActions", auditService.getToutLeJournal()
                .stream().limit(10).toList());
        return "dashboard";
    }

    @GetMapping("/journal")
    public String journal(Model model) {
        model.addAttribute("entrees", auditService.getToutLeJournal());
        return "journal";
    }
}