// ==========================================================
// SERVICE WORKER - Bibliothèque Municipale
// Cache les ressources pour un fonctionnement hors-ligne
// ==========================================================

const CACHE_NAME = 'biblio-cache-v1';
const OFFLINE_URL = '/offline';

// Ressources à mettre en cache dès l'installation
const PRECACHE_URLS = [
    '/',
    '/css/style.css',
    '/manifest.json',
    '/icons/icon-192.png',
    '/icons/icon-512.png'
];

// ==================== INSTALLATION ====================
self.addEventListener('install', event => {
    console.log('[Service Worker] Installation...');
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => {
                console.log('[Service Worker] Mise en cache initiale');
                return cache.addAll(PRECACHE_URLS);
            })
            .then(() => self.skipWaiting())
    );
});

// ==================== ACTIVATION ====================
self.addEventListener('activate', event => {
    console.log('[Service Worker] Activation...');
    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames.map(cacheName => {
                    if (cacheName !== CACHE_NAME) {
                        console.log('[Service Worker] Suppression ancien cache:', cacheName);
                        return caches.delete(cacheName);
                    }
                })
            );
        }).then(() => self.clients.claim())
    );
});

// ==================== FETCH (INTERCEPTION) ====================
self.addEventListener('fetch', event => {
    // Ignorer les requêtes non-GET
    if (event.request.method !== 'GET') return;

    // Ignorer les requêtes API POST/PUT/DELETE
    const url = new URL(event.request.url);
    if (url.pathname.startsWith('/api/')) return;

    // Stratégie : Network First, fallback Cache
    event.respondWith(
        fetch(event.request)
            .then(response => {
                // Mettre en cache la réponse fraîche
                if (response.status === 200) {
                    const responseClone = response.clone();
                    caches.open(CACHE_NAME).then(cache => {
                        cache.put(event.request, responseClone);
                    });
                }
                return response;
            })
            .catch(() => {
                // Pas de réseau → essayer le cache
                return caches.match(event.request).then(cachedResponse => {
                    if (cachedResponse) {
                        return cachedResponse;
                    }
                    // Si rien dans le cache → page d'erreur
                    return caches.match(OFFLINE_URL);
                });
            })
    );
});

// ==================== NOTIFICATIONS PUSH ====================
self.addEventListener('push', event => {
    const data = event.data ? event.data.json() : {};
    const title = data.title || 'Bibliothèque';
    const options = {
        body: data.body || 'Nouvelle notification',
        icon: '/icons/icon-192.png',
        badge: '/icons/icon-192.png',
        vibrate: [200, 100, 200],
        data: { url: data.url || '/' }
    };
    event.waitUntil(self.registration.showNotification(title, options));
});

self.addEventListener('notificationclick', event => {
    event.notification.close();
    event.waitUntil(
        clients.openWindow(event.notification.data.url)
    );
});