const CACHE_NAME = 'dalil-record-cache-v11';

const STATIC_ASSETS = [
  './',
  './index.html',
  './manifest.json',
  './icon-192.png',
  './icon-512.png',
  './apple-touch-icon.png',
  './favicon.ico'
];

self.addEventListener('install', event => {
  self.skipWaiting();
  event.waitUntil(
    caches.open(CACHE_NAME).then(async cache => {
      for (const asset of STATIC_ASSETS) {
        try {
          await cache.add(asset);
        } catch (err) {
          console.warn('Pre-cache skip:', asset, err);
        }
      }
    })
  );
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames
          .filter(name => name !== CACHE_NAME)
          .map(name => caches.delete(name))
      );
    }).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', event => {
  const request = event.request;
  const url = new URL(request.url);

  // 1. Navigation (HTML pages)
  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request)
        .then(networkResponse => {
          if (networkResponse && networkResponse.status === 200) {
            const cloned = networkResponse.clone();
            caches.open(CACHE_NAME).then(c => c.put(request, cloned));
          }
          return networkResponse;
        })
        .catch(async () => {
          return (await caches.match('./index.html')) || 
                 (await caches.match('index.html')) || 
                 (await caches.match('./')) || 
                 (await caches.match('/'));
        })
    );
    return;
  }

  // 2. Google Fonts
  if (url.origin === 'https://fonts.googleapis.com' || url.origin === 'https://fonts.gstatic.com') {
    event.respondWith(
      caches.match(request).then(cached => {
        if (cached) return cached;
        return fetch(request).then(networkResponse => {
          if (networkResponse && (networkResponse.status === 200 || networkResponse.type === 'opaque')) {
            const cloned = networkResponse.clone();
            caches.open(CACHE_NAME).then(c => c.put(request, cloned));
          }
          return networkResponse;
        }).catch(() => new Response('', { status: 408, statusText: 'Offline font' }));
      })
    );
    return;
  }

  // 3. Static assets: Cache First, fallback to Network
  event.respondWith(
    caches.match(request, { ignoreSearch: true }).then(cached => {
      if (cached) return cached;
      return fetch(request).then(networkResponse => {
        if (networkResponse && networkResponse.status === 200) {
          const cloned = networkResponse.clone();
          caches.open(CACHE_NAME).then(c => c.put(request, cloned));
        }
        return networkResponse;
      }).catch(async () => {
        if (request.destination === 'image' && (url.pathname.endsWith('.png') || url.pathname.endsWith('.ico'))) {
          return (await caches.match('./icon-192.png')) || (await caches.match('./icon-512.png'));
        }
      });
    })
  );
});
