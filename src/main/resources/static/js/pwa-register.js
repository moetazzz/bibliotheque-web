// ==========================================================
// PWA REGISTER - Enregistre le service worker + bannière
// ==========================================================

(function() {
    'use strict';

    // ==================== ENREGISTREMENT SERVICE WORKER ====================
    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => {
            navigator.serviceWorker.register('/service-worker.js')
                .then(registration => {
                    console.log('✅ Service Worker enregistré :', registration.scope);
                })
                .catch(error => {
                    console.error('❌ Erreur Service Worker :', error);
                });
        });
    }

    // ==================== BANNIÈRE D'INSTALLATION ====================
    let deferredPrompt = null;

    window.addEventListener('beforeinstallprompt', (e) => {
        // Empêcher la bannière par défaut
        e.preventDefault();
        // Sauvegarder l'événement pour plus tard
        deferredPrompt = e;
        // Afficher notre bannière custom
        afficherBanniereInstallation();
    });

    function afficherBanniereInstallation() {
        // Éviter les doublons
        if (document.getElementById('pwa-install-banner')) return;

        const banner = document.createElement('div');
        banner.id = 'pwa-install-banner';
        banner.className = 'pwa-banner';
        banner.innerHTML = `
            <div class="pwa-banner-content">
                <img src="/icons/icon-192.png" alt="Icone" class="pwa-banner-icon">
                <div class="pwa-banner-text">
                    <strong>Installer l'application</strong>
                    <span>Accès rapide depuis votre écran d'accueil</span>
                </div>
                <button id="pwa-install-btn" class="pwa-btn-install">Installer</button>
                <button id="pwa-close-btn" class="pwa-btn-close">✕</button>
            </div>
        `;
        document.body.appendChild(banner);

        // Animation d'entrée
        setTimeout(() => banner.classList.add('pwa-banner-visible'), 100);

        // Bouton Installer
        document.getElementById('pwa-install-btn').addEventListener('click', async () => {
            if (!deferredPrompt) return;
            deferredPrompt.prompt();
            const { outcome } = await deferredPrompt.userChoice;
            console.log('Choix utilisateur :', outcome);
            deferredPrompt = null;
            fermerBanniere();
        });

        // Bouton Fermer
        document.getElementById('pwa-close-btn').addEventListener('click', () => {
            fermerBanniere();
            // Ne plus afficher pendant 7 jours
            localStorage.setItem('pwa-dismissed', Date.now());
        });
    }

    function fermerBanniere() {
        const banner = document.getElementById('pwa-install-banner');
        if (banner) {
            banner.classList.remove('pwa-banner-visible');
            setTimeout(() => banner.remove(), 300);
        }
    }

    // Ne pas afficher si déjà fermée récemment
    const dismissed = localStorage.getItem('pwa-dismissed');
    if (dismissed) {
        const sevenDays = 7 * 24 * 60 * 60 * 1000;
        if (Date.now() - parseInt(dismissed) < sevenDays) {
            // Ne pas afficher
            window.addEventListener('beforeinstallprompt', (e) => e.preventDefault());
        }
    }

    // ==================== MISE À JOUR DISPONIBLE ====================
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.addEventListener('controllerchange', () => {
            console.log('🔄 Mise à jour disponible — rechargez la page');
        });
    }
})();