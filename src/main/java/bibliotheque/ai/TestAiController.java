package bibliotheque.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-ai")
public class TestAiController {

    private final ChatClient chatClient;

    public TestAiController(ChatClient.Builder builder, BibliothequeTools tools) {
        this.chatClient = builder
                .defaultTools(tools)   // 👈 Enregistre les outils
                .build();
    }

    @GetMapping("/bonjour")
    public String bonjour(@RequestParam(defaultValue = "Bonjour, qui es-tu ?") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}