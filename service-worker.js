const CACHE_NAME = 'dalil-app-offline-v8';

const urlsToCache = [
  './',
  './index.html',
  './manifest.json',
  './icon.png'
];

// ১. ইনস্টল করার সময় ফাইলগুলো অফলাইনের জন্য সেভ করবে
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        return cache.addAll(urlsToCache);
      })
  );
  self.skipWaiting();
});

// ২. পুরনো ক্যাশ মুছে নতুন ক্যাশ আপডেট করবে
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

// ৩. ইন্টারনেট না থাকলেও অ্যাপটি রান করানোর ম্যাজিক!
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => {
        // মেমোরিতে সেভ থাকলে ইন্টারনেট ছাড়াই সেটি ওপেন করবে
        if (response) {
          return response;
        }
        // মেমোরিতে না থাকলে ইন্টারনেট থেকে আনার চেষ্টা করবে
        return fetch(event.request).catch(() => {
          // ইন্টারনেট না থাকলে এবং অ্যাপটি ওপেন করলে সরাসরি হোমপেজ দেখিয়ে দেবে
          if (event.request.mode === 'navigate' || event.request.headers.get('accept').includes('text/html')) {
            return caches.match('./index.html');
          }
        });
      })
  );
});
