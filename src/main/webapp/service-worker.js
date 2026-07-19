const CACHE_NAME = "ljtrouteflow-static-v4";
const SCOPE = self.registration.scope;
const STATIC_ASSETS = [
  "assets/css/app.css",
  "assets/js/app.js",
  "assets/icons/favicon-32.png",
  "assets/icons/apple-touch-icon.png",
  "assets/icons/icon-192.png",
  "assets/icons/icon-512.png",
  "assets/icons/icon-maskable-512.png",
  "offline.html",
  "manifest.json"
].map(path => new URL(path, SCOPE).href);

self.addEventListener("install", event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(STATIC_ASSETS))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", event => {
  event.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys.filter(key => key !== CACHE_NAME).map(key => caches.delete(key))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", event => {
  const request = event.request;
  if (request.method !== "GET") return;

  const url = new URL(request.url);
  if (request.mode === "navigate") {
    event.respondWith(
      fetch(request, { cache: "no-store" })
        .catch(() => caches.match(new URL("offline.html", SCOPE).href))
    );
    return;
  }

  if (url.origin !== self.location.origin) return;
  if (url.pathname.includes("/documents/download") ||
      url.pathname.includes("/documents/view") ||
      url.pathname.includes("/approvals/")) return;

  const isStatic = url.pathname.includes("/assets/") ||
      url.pathname.endsWith("manifest.json") ||
      url.pathname.endsWith("offline.html");
  if (!isStatic) return;

  event.respondWith(
    caches.match(request).then(cached => cached || fetch(request).then(response => {
      if (response.ok) {
        caches.open(CACHE_NAME).then(cache => cache.put(request, response.clone()));
      }
      return response;
    }))
  );
});
