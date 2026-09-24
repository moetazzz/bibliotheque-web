package bibliotheque.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccueilController {

    @GetMapping("/")
    public String accueil(Model model) {
        model.addAttribute("titre", "Bibliothèque Municipale");
        model.addAttribute("message", "Bienvenue sur l'application de gestion de bibliothèque !");
        return "accueil";  // → affiche templates/accueil.html
    }
}