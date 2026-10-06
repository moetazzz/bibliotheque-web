package bibliotheque.ai;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-ai")
public class TestAiController {

    private final AssistantService assistantService;

    public TestAiController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping("/bonjour")
    public String bonjour(@RequestParam(defaultValue = "Bonjour, qui es-tu ?") String message) {
        return assistantService.chat(message);
    }
}