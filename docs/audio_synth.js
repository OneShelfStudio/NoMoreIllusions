// ============================================================================
// NO MORE ILLUSIONS — WEBAUDIO SYNTHESIZER, SFX POOL & H5 ADS
// ============================================================================

Object.assign(NoMoreIllusionsApp.prototype, {
  initH5Ads() {
    const H5_TEST_MODE = true;
    const PUB_ID = "ca-pub-7651037308656903";
    this.h5AdsEnabled = /(^|\.)digital-dads-llc\.com$/i.test(location.hostname);
    this.pendingShowRewardAd = null;
    this.gameOverCount = 0;
    if (!this.h5AdsEnabled) return;
    try {
      const s = document.createElement("script");
      s.async = true;
      s.src = `https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=${PUB_ID}`;
      s.crossOrigin = "anonymous";
      s.setAttribute("data-ad-client", PUB_ID);
      s.setAttribute("data-ad-frequency-hint", "30s");
      if (H5_TEST_MODE) s.setAttribute("data-adbreak-test", "on");
      document.head.appendChild(s);
      window.adsbygoogle = window.adsbygoogle || [];
      window.adBreak = window.adConfig = function (o) { window.adsbygoogle.push(o); };
      window.adConfig({ preloadAdBreaks: "on", sound: "on" });
    } catch (e) {
      this.h5AdsEnabled = false;
    }
  },

  pauseAllAudioForAd() {
    this.audioPausedForAd = true;
    try { this.trackAudio.pause(); } catch (e) {}
    try { this.lobbyAudio.pause(); } catch (e) {}
    try { this.demoAudio.pause(); } catch (e) {}
  },

  resumeAudioAfterAd() {
    this.audioPausedForAd = false;
    if (this.gameState === "PLAYING" && !this.prefs.musicMuted) this.trackAudio.play().catch(() => {});
    else this.updateLobbyPlayback();
  },

  // Ask Google for a rewarded placement as soon as the Game Over screen appears.
  // beforeReward only fires when an ad is actually ready; we hold showAdFn until the player taps the AD button.
  prepareRewardedRevive() {
    this.pendingShowRewardAd = null;
    if (!this.h5AdsEnabled || this.reviveUsedThisRun || typeof window.adBreak !== "function") return;
    let granted = false;
    try {
      window.adBreak({
        type: "reward",
        name: "revive_player",
        beforeAd: () => this.pauseAllAudioForAd(),
        afterAd: () => this.resumeAudioAfterAd(),
        beforeReward: (showAdFn) => { this.pendingShowRewardAd = showAdFn; },
        adDismissed: () => { granted = false; },
        adViewed: () => { granted = true; },
        adBreakDone: () => {
          this.pendingShowRewardAd = null;
          if (granted && this.gameState === "GAME_OVER") {
            this.reviveUsedThisRun = true;
            this.performRevive();
          }
        }
      });
    } catch (e) {}
  },

  // Interstitial between runs (every 3rd replay after a Game Over — matches Android cadence)
  showInterstitialThen(next) {
    if (!this.h5AdsEnabled || typeof window.adBreak !== "function") { next(); return; }
    let done = false;
    const finish = () => { if (!done) { done = true; next(); } };
    try {
      window.adBreak({
        type: "next",
        name: "replay_track",
        beforeAd: () => this.pauseAllAudioForAd(),
        afterAd: () => { this.audioPausedForAd = false; },
        adBreakDone: finish
      });
      setTimeout(finish, 6000); // never block gameplay if the ad API stalls
    } catch (e) { finish(); }
  },

  initSfxPool() {
    // Real acoustic and synthetic assets on disk
    const sfxFiles = {
      graze: "assets/near_miss_woosh.wav",
      focus: "assets/focus_activate.wav",
      hit: "assets/pulse_hit.wav",
      victory: "assets/victory_fanfare.wav",
      gameover: "assets/death.wav",
      levelup: "assets/level_up.wav",
      powerup: "assets/powerup.wav"
    };
    for (const [k, src] of Object.entries(sfxFiles)) {
      const a = new Audio(src);
      a.preload = "auto";
      this.sfxPool[k] = a;
    }

    // WebAudio Synthesizer for warm, juicy harmonic chords and chimes (zero 404s!)
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (AudioCtx) this.audioCtx = new AudioCtx();
    } catch (e) {}
  },

  playSfx(name, volume = 0.65) {
    if (this.prefs.sfxMuted) return;

    // Check for synthetic chimes
    if (name === "click") {
      this.synthWarmClick(volume);
      return;
    }
    if (name === "orb") {
      this.synthHarmonicOrbChime(volume);
      return;
    }
    if (name === "shield") {
      const base = this.sfxPool["hit"];
      if (base) {
        try {
          const clone = base.cloneNode();
          clone.volume = volume;
          clone.play().catch(() => {});
        } catch (e) {}
      }
      return;
    }

    const base = this.sfxPool[name];
    if (base) {
      try {
        const clone = base.cloneNode();
        clone.volume = volume;
        clone.play().catch(() => {});
      } catch (e) {}
    }
  },

  triggerHaptic(pattern = [20]) {
    try {
      if (typeof navigator !== "undefined" && navigator.vibrate) {
        navigator.vibrate(pattern);
      }
    } catch (e) {}
  },

  synthWarmClick(vol = 0.35) {
    if (!this.audioCtx || this.prefs.sfxMuted) return;
    try {
      if (this.audioCtx.state === "suspended") this.audioCtx.resume();
      const osc = this.audioCtx.createOscillator();
      const gain = this.audioCtx.createGain();
      osc.type = "sine";
      osc.frequency.setValueAtTime(880, this.audioCtx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(320, this.audioCtx.currentTime + 0.045);
      gain.gain.setValueAtTime(vol * 0.45, this.audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.audioCtx.currentTime + 0.045);
      osc.connect(gain);
      gain.connect(this.audioCtx.destination);
      osc.start();
      osc.stop(this.audioCtx.currentTime + 0.05);
    } catch (e) {}
  },

  synthHarmonicOrbChime(vol = 0.55) {
    if (!this.audioCtx || this.prefs.sfxMuted) return;
    try {
      if (this.audioCtx.state === "suspended") this.audioCtx.resume();
      // Musical pentatonic scale matching C minor / Eb major: C5, Eb5, F5, G5, Bb5, C6
      const scale = [523.25, 622.25, 698.46, 783.99, 932.33, 1046.50];
      const freq = scale[(this.combo || 1) % scale.length];

      const osc = this.audioCtx.createOscillator();
      const osc2 = this.audioCtx.createOscillator();
      const gain = this.audioCtx.createGain();

      osc.type = "sine";
      osc.frequency.setValueAtTime(freq, this.audioCtx.currentTime);
      osc2.type = "triangle";
      osc2.frequency.setValueAtTime(freq * 2, this.audioCtx.currentTime);

      gain.gain.setValueAtTime(vol * 0.35, this.audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.audioCtx.currentTime + 0.38);

      osc.connect(gain);
      osc2.connect(gain);
      gain.connect(this.audioCtx.destination);

      osc.start();
      osc2.start();
      osc.stop(this.audioCtx.currentTime + 0.40);
      osc2.stop(this.audioCtx.currentTime + 0.40);
    } catch (e) {}
  },

  synthComboChordStinger(comboLevel) {
    if (!this.audioCtx || this.prefs.sfxMuted) return;
    try {
      if (this.audioCtx.state === "suspended") this.audioCtx.resume();
      // Lush velvet minor 9th chord: C4, Eb4, G4, Bb4, D5
      const chord = [261.63, 311.13, 392.00, 466.16, 587.33];
      chord.forEach((freq, idx) => {
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.type = "sine";
        osc.frequency.setValueAtTime(freq, this.audioCtx.currentTime + idx * 0.035);
        gain.gain.setValueAtTime(0.12, this.audioCtx.currentTime + idx * 0.035);
        gain.gain.exponentialRampToValueAtTime(0.001, this.audioCtx.currentTime + 0.85);
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);
        osc.start(this.audioCtx.currentTime + idx * 0.035);
        osc.stop(this.audioCtx.currentTime + 0.90);
      });
    } catch (e) {}
  },

  synthModeSwitchChime(mode) {
    if (!this.audioCtx || this.prefs.sfxMuted) return;
    try {
      if (this.audioCtx.state === "suspended") this.audioCtx.resume();
      const pitchMult = mode.pitch || mode.speed || 1.0;
      const baseNotes = mode.id === "OVERCLOCK"
        ? [523.25, 659.25, 783.99, 1046.50] // Bright ascending C5-E5-G5-C6
        : mode.id === "SLOWED"
        ? [392.00, 311.13, 261.63] // Deep lush descending G4-Eb4-C4
        : mode.id === "ENDLESS"
        ? [440.00, 554.37, 659.25, 880.00] // Golden A-major shimmer
        : [523.25, 622.25, 783.99]; // Balanced C-minor triad

      baseNotes.forEach((freq, idx) => {
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.type = mode.id === "SLOWED" ? "triangle" : "sine";
        const startT = this.audioCtx.currentTime + idx * 0.045;
        osc.frequency.setValueAtTime(freq * pitchMult, startT);
        gain.gain.setValueAtTime(0.18, startT);
        gain.gain.exponentialRampToValueAtTime(0.001, startT + 0.36);
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);
        osc.start(startT);
        osc.stop(startT + 0.38);
      });
    } catch (e) {}
  },

  unlockAudioOnFirstGesture() {
    if (this.audioUnlocked) return;
    this.audioUnlocked = true;
    this.updateLobbyPlayback();
  },

  updateLobbyPlayback() {
    if (!this.audioUnlocked) return;
    if (this.gameState === "PLAYING" || this.gameState === "DEMO_PLAYER" || this.prefs.musicMuted) {
      this.lobbyAudio.pause();
      return;
    }
    const lobby = (LOBBY_TRACKS && LOBBY_TRACKS[this.prefs.lobbyThemeIndex % LOBBY_TRACKS.length]) || LOBBY_TRACKS[0];
    if (lobby && (!this.lobbyAudio.src || !this.lobbyAudio.src.endsWith(lobby.src))) {
      this.lobbyAudio.src = lobby.src;
    }
    const mode = this.getRemixMode();
    try {
      this.lobbyAudio.preservesPitch = false;
      this.lobbyAudio.webkitPreservesPitch = false;
      this.lobbyAudio.mozPreservesPitch = false;
      this.lobbyAudio.playbackRate = mode.speed || 1.0;
    } catch (e) {}
    this.lobbyAudio.play().catch(() => {});
  },

  cycleLobbyTheme() {
    this.playSfx("click");
    this.prefs.lobbyThemeIndex = (this.prefs.lobbyThemeIndex + 1) % LOBBY_TRACKS.length;
    this.savePrefs();
    const lobby = LOBBY_TRACKS[this.prefs.lobbyThemeIndex];
    if (this.audioUnlocked && !this.prefs.musicMuted && this.gameState === "MENU") {
      this.lobbyAudio.src = lobby.src;
      this.lobbyAudio.play().catch(() => {});
    }
    this.syncUI();
  },

});
