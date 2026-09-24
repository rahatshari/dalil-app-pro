const CACHE_NAME = 'dalil-record-v9';

const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/manifest.json',
  '/icon.png'
];

self.addEventListener('install', event => {
  self.skipWaiting();
  event.waitUntil(
    caches.open(CACHE_NAME).then(async cache => {
      // Cache each asset individually to prevent any single asset from failing SW installation
      for (const asset of STATIC_ASSETS) {
        try {
          await cache.add(asset);
        } catch (e) {
          console.warn('Pre-cache skip for:', asset, e);
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

  // For HTML navigation requests, try network first, fallback to cached index.html
  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request)
        .then(networkResponse => {
          if (networkResponse && networkResponse.status === 200) {
            const clonedResponse = networkResponse.clone();
            caches.open(CACHE_NAME).then(cache => cache.put(request, clonedResponse));
          }
          return networkResponse;
        })
        .catch(() => {
          return caches.match('/index.html') || caches.match('/');
        })
    );
    return;
  }

  // Handle Google Fonts (cache-first)
  if (url.origin === 'https://fonts.googleapis.com' || url.origin === 'https://fonts.gstatic.com') {
    event.respondWith(
      caches.match(request).then(cachedResponse => {
        if (cachedResponse) return cachedResponse;
        return fetch(request).then(networkResponse => {
          if (networkResponse && (networkResponse.status === 200 || networkResponse.type === 'opaque')) {
            const clonedResponse = networkResponse.clone();
            caches.open(CACHE_NAME).then(cache => cache.put(request, clonedResponse));
          }
          return networkResponse;
        }).catch(() => {
          return new Response('', { status: 408, statusText: 'Offline font unavailable' });
        });
      })
    );
    return;
  }

  // For single icon and other local assets: Cache first, fallback to network
  event.respondWith(
    caches.match(request, { ignoreSearch: true }).then(cachedResponse => {
      if (cachedResponse) {
        return cachedResponse;
      }
      return fetch(request).then(networkResponse => {
        if (networkResponse && networkResponse.status === 200) {
          const clonedResponse = networkResponse.clone();
          caches.open(CACHE_NAME).then(cache => cache.put(request, clonedResponse));
        }
        return networkResponse;
      }).catch(() => {
        if (request.destination === 'image' && url.pathname.includes('icon')) {
          return caches.match('/icon.png');
        }
        if (request.destination === 'document' || request.mode === 'navigate') {
          return caches.match('/index.html');
        }
      });
    })
  );
});
