const CACHE_NAME = 'dalil-app-offline-v1';

// যেসব ফাইল অফলাইনের জন্য সেভ করা হবে
const ASSETS_TO_CACHE = [
  './',
  'index.html',
  'manifest.json',
  'icon.png'
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(ASSETS_TO_CACHE))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cache => {
          if (cache !== CACHE_NAME) {
            return caches.delete(cache);
          }
        })
      );
    })
  );
  self.clients.claim();
});

// ইন্টারনেট না থাকলে ডাইনামিক ক্যাশ থেকে অ্যাপ চালাবে
self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') return;

  event.respondWith(
    caches.match(event.request)
      .then(cachedResponse => {
        if (cachedResponse) {
          return cachedResponse; // মেমোরি থেকে দিচ্ছে
        }
        return fetch(event.request).then(networkResponse => {
          if (!networkResponse || networkResponse.status !== 200 || networkResponse.type !== 'basic') {
            return networkResponse;
          }
          const responseToCache = networkResponse.clone();
          caches.open(CACHE_NAME).then(cache => {
            cache.put(event.request, responseToCache);
          });
          return networkResponse;
        }).catch(() => {
          // ইন্টারনেট না থাকলে হোম পেজ দেখাবে
          if (event.request.mode === 'navigate') {
            return caches.match('index.html');
          }
        });
      })
  );
});