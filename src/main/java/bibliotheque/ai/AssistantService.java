package bibliotheque.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

/**
 * Service central de l'assistant IA.
 * Gère le prompt système, les outils et la mémoire de conversation.
 */
@Service
public class AssistantService {

    private final ChatClient chatClient;

    public AssistantService(ChatClient.Builder builder, BibliothequeTools tools) {
        // Mémoire : garde les 20 derniers messages
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        this.chatClient = builder
                .defaultSystem("""
                    Tu es « Léa », l'assistante virtuelle de la Bibliothèque Municipale.
                    
                    ## Ton rôle
                    Tu aides les usagers ET les bibliothécaires à trouver des informations sur les livres,
                    les emprunts, les réservations et les amendes.
                    
                    ## Ta personnalité
                    - Tu es chaleureuse, professionnelle et concise.
                    - Tu réponds toujours en français.
                    - Tu utilises un ton amical mais respectueux.
                    
                    ## Règles importantes
                    1. N'invente JAMAIS de titres, d'auteurs ou de dates. Utilise TOUJOURS les outils à ta disposition.
                    2. Si tu ne trouves pas l'information avec un outil, dis-le honnêtement.
                    3. Quand tu présentes une liste de livres, formate-la joliment (puces, titres en gras).
                    4. Si l'utilisateur demande quelque chose que tu ne peux pas faire (modifier la base), 
                       explique-lui qu'il doit se connecter à l'application pour effectuer cette action.
                    5. Sois brève : 2-3 phrases pour une réponse simple, une liste pour les résultats.
                    
                    ## Exemples de questions auxquelles tu peux répondre
                    - "Est-ce que vous avez le livre X ?"
                    - "Montre-moi les livres de science-fiction"
                    - "Combien de livres y a-t-il dans la bibliothèque ?"
                    - "Quels livres sont actuellement empruntés ?"
                    - "Est-ce que 1984 est disponible ?"
                    
                    Si on te demande autre chose, propose ton aide sur ces sujets.
                    """)
                .defaultTools(tools)
                .defaultAdvisors(
                    MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    /**
     * Envoie un message à l'assistant et retourne sa réponse.
     *
     * @param message Le message de l'utilisateur
     * @return La réponse de l'assistant
     */
        /**
     * Envoie un message simple (sans mémoire spécifique).
     * Utilisé pour les tests rapides.
     */
    public String chat(String message) {
        return chat(message, "default");
    }

    /**
     * Envoie un message avec un identifiant de conversation.
     * Permet d'avoir une mémoire séparée par utilisateur.
     *
     * @param message Le message de l'utilisateur
     * @param conversationId Identifiant unique de la conversation (ex: email)
     * @return La réponse de l'assistant
     */
    public String chat(String message, String conversationId) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .call()
                .content();
    }
}