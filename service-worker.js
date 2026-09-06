const CACHE_NAME = 'dalil-app-offline-v10'; // ভার্সন নম্বর

const urlsToCache = [
  './',
  './index.html',
  './manifest.json',
  './icon.png'
];

// ইনস্টল হওয়ার সাথে সাথে ক্যাশ সেভ করবে
self.addEventListener('install', event => {
  self.skipWaiting(); // পুরনো ক্যাশ বাদ দিয়ে সাথে সাথে নতুনটা চালু করবে
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        return cache.addAll(urlsToCache);
      })
  );
});

// পুরনো ফাইল ডিলিট করে নতুন ফাইল আপডেট করবে
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
  self.clients.claim(); // সাথে সাথে পেজ কন্ট্রোল করবে
});

// অফলাইনে চালানোর কমান্ড
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => {
        // মেমোরিতে থাকলে ইন্টারনেট ছাড়াই লোড করবে
        if (response) {
          return response;
        }
        // না থাকলে ইন্টারনেট থেকে আনবে
        return fetch(event.request).catch(() => {
          // ইন্টারনেট না থাকলে সরাসরি index.html দেখাবে
          if (event.request.mode === 'navigate') {
            return caches.match('./index.html');
          }
        });
      })
  );
});