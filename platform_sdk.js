/**
 * No More Illusions — Universal Web Platform SDK Adapter
 * Seamlessly integrates Poki SDK (v2), CrazyGames SDK (v3), and Standalone Fallback.
 * Auto-detects hosting environment and binds gameplayStart/Stop, commercial breaks, and rewarded video revives.
 */
(function (window) {
  const PlatformSDK = {
    platform: "standalone", // "poki" | "crazygames" | "standalone"
    initialized: false,
    pokiReady: false,
    crazyGamesReady: false,
    adActive: false,

    async init() {
      if (this.initialized) return;
      this.initialized = true;

      // 1. Detect or initialize Poki SDK
      if (typeof window.PokiSDK !== "undefined") {
        try {
          await window.PokiSDK.init();
          this.platform = "poki";
          this.pokiReady = true;
          console.log("[PlatformSDK] Initialized with Poki SDK");
          return;
        } catch (e) {
          console.warn("[PlatformSDK] Poki SDK init error (possibly adblock):", e);
          this.platform = "poki";
          this.pokiReady = false;
        }
      }

      // 2. Detect or initialize CrazyGames SDK v3
      if (typeof window.CrazyGames !== "undefined" && window.CrazyGames.SDK) {
        try {
          await window.CrazyGames.SDK.init();
          this.platform = "crazygames";
          this.crazyGamesReady = true;
          console.log("[PlatformSDK] Initialized with CrazyGames SDK v3");
          return;
        } catch (e) {
          console.warn("[PlatformSDK] CrazyGames SDK init error:", e);
          this.platform = "crazygames";
          this.crazyGamesReady = false;
        }
      }

      // 3. Standalone mode (GitHub Pages, local dev, custom domain)
      this.platform = "standalone";
      console.log("[PlatformSDK] Initialized in Standalone mode");
    },

    gameplayStart() {
      if (this.platform === "poki" && this.pokiReady) {
        try { window.PokiSDK.gameplayStart(); } catch (e) {}
      } else if (this.platform === "crazygames" && this.crazyGamesReady) {
        try { window.CrazyGames.SDK.game.gameplayStart(); } catch (e) {}
      }
    },

    gameplayStop() {
      if (this.platform === "poki" && this.pokiReady) {
        try { window.PokiSDK.gameplayStop(); } catch (e) {}
      } else if (this.platform === "crazygames" && this.crazyGamesReady) {
        try { window.CrazyGames.SDK.game.gameplayStop(); } catch (e) {}
      }
    },

    /**
     * Midgame Commercial Break / Interstitial
     */
    showCommercial(onComplete) {
      const finish = () => {
        this.adActive = false;
        if (typeof onComplete === "function") onComplete();
      };

      if (this.platform === "poki" && this.pokiReady) {
        this.adActive = true;
        this.gameplayStop();
        if (window.app) window.app.pauseAllAudioForAd();
        window.PokiSDK.commercialBreak(() => {
          if (window.app) window.app.resumeAudioAfterAd();
          this.gameplayStart();
          finish();
        });
        return;
      }

      if (this.platform === "crazygames" && this.crazyGamesReady) {
        this.adActive = true;
        this.gameplayStop();
        if (window.app) window.app.pauseAllAudioForAd();
        window.CrazyGames.SDK.ad.requestAd("midgame", {
          adStarted: () => { this.adActive = true; },
          adFinished: () => {
            if (window.app) window.app.resumeAudioAfterAd();
            this.gameplayStart();
            finish();
          },
          adError: (err) => {
            console.warn("[PlatformSDK] CrazyGames midgame error:", err);
            if (window.app) window.app.resumeAudioAfterAd();
            this.gameplayStart();
            finish();
          }
        });
        return;
      }

      // Standalone mode: Proceed immediately
      finish();
    },

    /**
     * Rewarded Video Ad Break (e.g. for Revive or Double Notes)
     */
    showRewarded(onReward, onDismiss) {
      if (this.platform === "poki" && this.pokiReady) {
        this.adActive = true;
        this.gameplayStop();
        if (window.app) window.app.pauseAllAudioForAd();
        window.PokiSDK.rewardedBreak((success) => {
          this.adActive = false;
          if (window.app) window.app.resumeAudioAfterAd();
          this.gameplayStart();
          if (success) {
            if (typeof onReward === "function") onReward();
          } else {
            if (typeof onDismiss === "function") onDismiss();
          }
        });
        return;
      }

      if (this.platform === "crazygames" && this.crazyGamesReady) {
        this.adActive = true;
        this.gameplayStop();
        if (window.app) window.app.pauseAllAudioForAd();
        window.CrazyGames.SDK.ad.requestAd("rewarded", {
          adStarted: () => { this.adActive = true; },
          adFinished: () => {
            this.adActive = false;
            if (window.app) window.app.resumeAudioAfterAd();
            this.gameplayStart();
            if (typeof onReward === "function") onReward();
          },
          adError: (err) => {
            console.warn("[PlatformSDK] CrazyGames rewarded error:", err);
            this.adActive = false;
            if (window.app) window.app.resumeAudioAfterAd();
            this.gameplayStart();
            if (typeof onDismiss === "function") onDismiss();
          }
        });
        return;
      }

      // Standalone mode fallback: Show quick 5s house promo
      if (window.app && typeof window.app.showHousePromoRevive === "function") {
        window.app.showHousePromoRevive(onReward, onDismiss);
      } else {
        if (typeof onReward === "function") onReward();
      }
    }
  };

  window.PlatformSDK = PlatformSDK;
})(window);
