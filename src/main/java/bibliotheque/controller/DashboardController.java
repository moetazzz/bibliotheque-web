package bibliotheque.controller;

import bibliotheque.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    @Autowired private LivreService livreService;
    @Autowired private UtilisateurService userService;
    @Autowired private EmpruntService empruntService;
    @Autowired private AuditService auditService;
    @Autowired private StatsService statsService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Stats basiques
        model.addAttribute("nbLivres", livreService.getTousLesLivres().size());
        model.addAttribute("nbUtilisateurs", userService.getTousLesUtilisateurs().size());
        model.addAttribute("nbEmpruntsEnCours", empruntService.getEmpruntsEnCours().size());
        model.addAttribute("nbAmendesImpayees", empruntService.getAmendesImpayees().size());

        // Stats avancées
        model.addAttribute("nbEnRetard", statsService.countEnRetard());
        model.addAttribute("totalAmendesImpayees", statsService.totalAmendesImpayees());
        model.addAttribute("totalAmendesPayees", statsService.totalAmendesPayees());

        // Data pour graphiques (passées en JSON via Thymeleaf)
        Map<String, List<Object>> dataEmprunts = statsService.empruntsParMois();
        model.addAttribute("empruntsLabels", dataEmprunts.get("labels"));
        model.addAttribute("empruntsValues", dataEmprunts.get("values"));

        Map<String, List<Object>> dataTopLivres = statsService.top5Livres();
        model.addAttribute("topLivresLabels", dataTopLivres.get("labels"));
        model.addAttribute("topLivresValues", dataTopLivres.get("values"));

        Map<String, List<Object>> dataTopUsers = statsService.top5Utilisateurs();
        model.addAttribute("topUsersLabels", dataTopUsers.get("labels"));
        model.addAttribute("topUsersValues", dataTopUsers.get("values"));

        Map<String, List<Object>> dataCategories = statsService.empruntsParCategorie();
        model.addAttribute("categoriesLabels", dataCategories.get("labels"));
        model.addAttribute("categoriesValues", dataCategories.get("values"));

        Map<String, List<Object>> dataAmendes = statsService.amendesParMois();
        model.addAttribute("amendesLabels", dataAmendes.get("labels"));
        model.addAttribute("amendesValues", dataAmendes.get("values"));

        // Dernières actions
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