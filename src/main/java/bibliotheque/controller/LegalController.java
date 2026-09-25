package bibliotheque.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegalController {

    @GetMapping("/mentions-legales")
    public String mentionsLegales() {
        return "legal/mentions-legales";
    }

    @GetMapping("/cgu")
    public String cgu() {
        return "legal/cgu";
    }

    @GetMapping("/confidentialite")
    public String confidentialite() {
        return "legal/confidentialite";
    }
}