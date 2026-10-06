// ==========================================================
// WIDGET ASSISTANT IA — Léa
// ==========================================================

(function() {
    'use strict';

    // ==================== CRÉATION DU HTML ====================
    function creerWidget() {
        // Bulle flottante
        const bubble = document.createElement('button');
        bubble.id = 'lea-bubble';
        bubble.title = 'Discuter avec Léa';
        bubble.innerHTML = '💬';
        bubble.setAttribute('aria-label', 'Ouvrir l\'assistant IA');

        // Fenêtre de chat
        const window_ = document.createElement('div');
        window_.id = 'lea-window';
        window_.innerHTML = `
            <div id="lea-header">
                <div class="avatar">👩‍💼</div>
                <div class="info">
                    <div class="name">Léa</div>
                    <div class="status">En ligne</div>
                </div>
                <button id="lea-close" title="Fermer">✕</button>
            </div>
            <div id="lea-messages"></div>
            <div id="lea-input-zone">
                <input type="text" id="lea-input"
                       placeholder="Posez votre question..."
                       autocomplete="off">
                <button id="lea-send" title="Envoyer">➤</button>
            </div>
        `;

        document.body.appendChild(bubble);
        document.body.appendChild(window_);

        return { bubble, window_ };
    }

    // ==================== LOGIQUE ====================
    document.addEventListener('DOMContentLoaded', () => {
        const { bubble, window_ } = creerWidget();
        const messagesEl = document.getElementById('lea-messages');
        const inputEl = document.getElementById('lea-input');
        const sendBtn = document.getElementById('lea-send');
        const closeBtn = document.getElementById('lea-close');

        let isOpen = false;
        let isSending = false;
        let welcomeShown = false;

        // ---------- Ouvrir / Fermer ----------
        function openChat() {
            isOpen = true;
            window_.classList.add('open');
            bubble.classList.add('open');
            bubble.innerHTML = '✕';
            inputEl.focus();

            // Message de bienvenue (une seule fois)
            if (!welcomeShown) {
                welcomeShown = true;
                ajouterMessage('bot',
                    'Bonjour ! Je suis <strong>Léa</strong>, votre assistante. ' +
                    'Posez-moi une question sur les livres, les emprunts ou les amendes. 😊');
            }
        }

        function closeChat() {
            isOpen = false;
            window_.classList.remove('open');
            bubble.classList.remove('open');
            bubble.innerHTML = '💬';
        }

        bubble.addEventListener('click', () => isOpen ? closeChat() : openChat());
        closeBtn.addEventListener('click', closeChat);

        // ---------- Ajouter un message ----------
        function ajouterMessage(type, contenu) {
            const msg = document.createElement('div');
            msg.className = 'lea-message ' + type;
            msg.innerHTML = contenu;
            messagesEl.appendChild(msg);
            messagesEl.scrollTop = messagesEl.scrollHeight;
            return msg;
        }

        function afficherTyping() {
            const typing = document.createElement('div');
            typing.className = 'lea-typing';
            typing.id = 'lea-typing';
            typing.innerHTML = '<span></span><span></span><span></span>';
            messagesEl.appendChild(typing);
            messagesEl.scrollTop = messagesEl.scrollHeight;
            return typing;
        }

        function retirerTyping() {
            const typing = document.getElementById('lea-typing');
            if (typing) typing.remove();
        }

        // ---------- Envoyer un message ----------
        async function envoyerMessage() {
            const texte = inputEl.value.trim();
            if (!texte || isSending) return;

            // Afficher le message utilisateur
            ajouterMessage('user', echapperHtml(texte));
            inputEl.value = '';
            inputEl.focus();

            // Désactiver l'envoi
            isSending = true;
            sendBtn.disabled = true;

            // Afficher "Léa écrit..."
            afficherTyping();

            try {
                // Récupérer le token CSRF
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

                const headers = { 'Content-Type': 'application/json' };
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                const response = await fetch('/api/assistant/chat', {
                    method: 'POST',
                    headers: headers,
                    body: JSON.stringify({ message: texte })
                });

                retirerTyping();

                if (!response.ok) {
                    ajouterMessage('bot',
                        '😕 Désolée, je n\'arrive pas à répondre pour le moment. ' +
                        'Réessayez dans quelques instants.');
                    return;
                }

                const data = await response.json();
                ajouterMessage('bot', formaterReponse(data.response));

            } catch (e) {
                retirerTyping();
                console.error('Erreur Léa:', e);
                ajouterMessage('bot',
                    '😕 Je rencontre un problème de connexion. Vérifiez votre réseau.');
            } finally {
                isSending = false;
                sendBtn.disabled = false;
            }
        }

        // ---------- Utilitaires ----------
        function echapperHtml(txt) {
            const div = document.createElement('div');
            div.textContent = txt;
            return div.innerHTML;
        }

        // Transforme les **gras**, les tirets, et les retours ligne en HTML
        function formaterReponse(txt) {
            if (!txt) return '';
            let html = echapperHtml(txt);
            html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
            html = html.replace(/\n- /g, '<br>• ');
            html = html.replace(/\n/g, '<br>');
            return html;
        }

        // ---------- Événements ----------
        sendBtn.addEventListener('click', envoyerMessage);
        inputEl.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                envoyerMessage();
            }
        });

        // Raccourci Échap pour fermer
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && isOpen) closeChat();
        });
    });
})();