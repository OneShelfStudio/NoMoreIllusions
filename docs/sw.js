const CACHE_NAME = "nmi-v5-ios-1to1-v5";
const ASSETS = [
  "./",
  "./index.html",
  "./multi_band_data.js",
  "./engine.js",
  "./manifest.json",
  "./assets/nmi_logo.png",
  "./assets/mandala_hero_cyber.png",
  "./assets/mandala_hero_solar.png",
  "./assets/mandala_hero_prism.png",
  "./assets/mandala_enemy_pulsar.png",
  "./assets/mandala_enemy_seeker.png",
  "./assets/mandala_enemy_wanderer.png",
  "./assets/btn_icon_pause.png",
  "./assets/lobby_velvet.mp3",
  "./assets/lobby_midnight.mp3",
  "./assets/lobby_crystal.mp3",
  "./assets/track_01.mp3",
  "./assets/track_02.mp3"
];

self.addEventListener("install", (event) => {
  self.skipWaiting();
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(ASSETS)).catch(() => {})
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys.map((k) => {
          if (k !== CACHE_NAME) return caches.delete(k);
        })
      )
    ).then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", (event) => {
  event.respondWith(
    fetch(event.request)
      .then((res) => {
        const copy = res.clone();
        caches.open(CACHE_NAME).then((cache) => cache.put(event.request, copy)).catch(() => {});
        return res;
      })
      .catch(() => caches.match(event.request))
  );
});
