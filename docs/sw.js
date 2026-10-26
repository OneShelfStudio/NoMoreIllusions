const CACHE_NAME = "nmi-v25-live";
const ASSETS = [
  "./",
  "./index.html",
  "./styles.css?v=25",
  "./multi_band_data.js?v=25",
  "./music_library.js?v=25",
  "./background_renderer.js?v=25",
  "./beat_orb_renderer.js?v=25",
  "./game_renderer.js?v=25",
  "./engine.js?v=25",
  "./audio_synth.js?v=25",
  "./game_physics.js?v=25",
  "./ui_screens.js?v=25",
  "./tutorial_overlay.js?v=25",
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
  "./assets/track_02.mp3",
  "./assets/near_miss_woosh.wav",
  "./assets/focus_activate.wav",
  "./assets/pulse_hit.wav",
  "./assets/victory_fanfare.wav",
  "./assets/death.wav",
  "./assets/level_up.wav",
  "./assets/powerup.wav"
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
  if (event.request.method !== "GET") return;

  const url = event.request.url;
  if (!url.startsWith("http")) return;
  // Never intercept cross-origin requests (Google AdSense / H5 Games Ads must always hit the network)
  if (new URL(url).origin !== self.location.origin) return;

  // Audio files with range requests should never be cached as 206 partial responses
  const isRangeRequest = event.request.headers.has("range");
  if (isRangeRequest) return;

  // NETWORK-FIRST for HTML, CSS, and JS so updates are ALWAYS immediately visible on the website!
  const isCodeOrMarkup =
    event.request.mode === "navigate" ||
    url.includes(".html") ||
    url.includes(".js") ||
    url.includes(".css") ||
    url.endsWith("/");

  if (isCodeOrMarkup) {
    event.respondWith(
      fetch(event.request, { cache: "no-store" })
        .then((res) => {
          if (res && res.status === 200) {
            const copy = res.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(event.request, copy)).catch(() => {});
          }
          return res;
        })
        .catch(() => caches.match(event.request).then((cached) => cached || new Response("Offline", { status: 503 })))
    );
    return;
  }

  event.respondWith(
    caches.match(event.request).then((cached) => {
      if (cached) return cached;
      return fetch(event.request)
        .then((res) => {
          if (!res || res.status !== 200) {
            return res;
          }
          const copy = res.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(event.request, copy)).catch(() => {});
          return res;
        })
        .catch(() => cached || new Response("Offline", { status: 503 }));
    })
  );
});
