const CACHE_NAME = "nmi-v16-dropdowns";
const ASSETS = [
  "./",
  "./index.html",
  "./styles.css",
  "./multi_band_data.js",
  "./music_library.js",
  "./background_renderer.js",
  "./beat_orb_renderer.js",
  "./game_renderer.js",
  "./engine.js",
  "./audio_synth.js",
  "./game_physics.js",
  "./ui_screens.js",
  "./tutorial_overlay.js",
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

  event.respondWith(
    caches.match(event.request).then((cached) => {
      if (cached && !isRangeRequest) return cached;
      return fetch(event.request)
        .then((res) => {
          if (!res || res.status !== 200 || isRangeRequest) {
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
