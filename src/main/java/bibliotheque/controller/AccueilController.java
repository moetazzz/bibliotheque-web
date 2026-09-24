package bibliotheque.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccueilController {

    @GetMapping("/")
    public String accueil(Model model, Authentication auth) {
        model.addAttribute("utilisateur", auth != null ? auth.getName() : null);
        return "accueil";
    }
}