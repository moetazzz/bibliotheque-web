package bibliotheque.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur REST pour l'assistant IA Léa.
 * Expose un endpoint POST /api/assistant/chat
 */
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    /**
     * Endpoint principal du chat.
     * Reçoit {"message": "..."} et retourne {"response": "..."}
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, String> body,
            Authentication auth) {

        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("response", "Message vide."));
        }

        // Identifiant de conversation = email de l'utilisateur connecté
        // Comme ça chaque utilisateur a sa propre mémoire
        String conversationId = (auth != null) ? auth.getName() : "anonyme";

        try {
            String reponse = assistantService.chat(message, conversationId);
            return ResponseEntity.ok(Map.of("response", reponse));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("response",
                        "Désolée, je rencontre un problème technique. Réessayez dans un instant."));
        }
    }
}