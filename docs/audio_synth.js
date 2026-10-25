// ============================================================================
// NO MORE ILLUSIONS — WEBAUDIO SYNTHESIZER, SFX POOL & H5 ADS
// ============================================================================

Object.assign(NoMoreIllusionsApp.prototype, {
  initH5Ads() {
    const H5_TEST_MODE = location.hostname === "localhost" || location.hostname === "127.0.0.1" || location.search.includes("adtest=1");
    const PUB_ID = "ca-pub-7651037308656903";
    this.h5AdsEnabled = /(^|\.)digital-dads-llc\.com$/i.test(location.hostname) || H5_TEST_MODE;
    this.pendingShowRewardAd = null;
    this.gameOverCount = 0;
    if (!this.h5AdsEnabled) return;
    try {
      let s = document.querySelector('script[src*="adsbygoogle.js"]');
      if (!s) {
        s = document.createElement("script");
        s.async = true;
        s.src = `https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=${PUB_ID}`;
        s.crossOrigin = "anonymous";
        s.setAttribute("data-ad-client", PUB_ID);
        s.setAttribute("data-ad-frequency-hint", "30s");
        document.head.appendChild(s);
      }
      if (H5_TEST_MODE) s.setAttribute("data-adbreak-test", "on");
      window.adsbygoogle = window.adsbygoogle || [];
      if (typeof window.adBreak !== "function") {
        window.adBreak = window.adConfig = function (o) { window.adsbygoogle.push(o); };
      }
      window.adConfig({
        preloadAdBreaks: "on",
        sound: (this.prefs && this.prefs.musicMuted) ? "off" : "on",
        onReady: () => { this.h5AdsReady = true; }
      });
    } catch (e) {
      this.h5AdsEnabled = false;
    }
  },

  syncH5SoundState() {
    if (!this.h5AdsEnabled || typeof window.adConfig !== "function") return;
    try {
      window.adConfig({ sound: (this.prefs && this.prefs.musicMuted) ? "off" : "on" });
    } catch (e) {}
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
    if (name === "whoosh_modal" || name === "whoosh_victory" || name === "whoosh_gameover") {
      this.synthWhooshChime(name, volume);
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
      const now = this.audioCtx.currentTime;
      // Soft air whoosh (95ms filtered noise sweep) -> crisp musical beep
      this._playAirWhoosh(now, 0.095, 320, 1180, vol * 0.22);
      const scale = [523.25, 587.33, 659.25, 783.99, 880.00, 1046.50];
      this._uiBeepIdx = ((this._uiBeepIdx || 0) + 1) % scale.length;
      const freq = scale[this._uiBeepIdx];
      const osc = this.audioCtx.createOscillator();
      const gain = this.audioCtx.createGain();
      osc.type = "sine";
      osc.frequency.setValueAtTime(freq, now + 0.035);
      osc.frequency.exponentialRampToValueAtTime(freq * 1.25, now + 0.145);
      gain.gain.setValueAtTime(vol * 0.36, now + 0.035);
      gain.gain.exponentialRampToValueAtTime(0.001, now + 0.165);
      osc.connect(gain);
      gain.connect(this.audioCtx.destination);
      osc.start(now + 0.035);
      osc.stop(now + 0.17);
    } catch (e) {}
  },

  _playAirWhoosh(startTime, durationSec, startFreq, endFreq, peakGain) {
    if (!this.audioCtx) return;
    const sampleRate = this.audioCtx.sampleRate;
    const frameCount = Math.max(1, Math.floor(sampleRate * durationSec));
    const buffer = this.audioCtx.createBuffer(1, frameCount, sampleRate);
    const data = buffer.getChannelData(0);
    for (let i = 0; i < frameCount; i++) {
      const env = Math.sin((i / frameCount) * Math.PI);
      data[i] = (Math.random() * 2 - 1) * env;
    }
    const noise = this.audioCtx.createBufferSource();
    noise.buffer = buffer;
    const filter = this.audioCtx.createBiquadFilter();
    filter.type = "bandpass";
    filter.Q.setValueAtTime(2.6, startTime);
    filter.frequency.setValueAtTime(startFreq, startTime);
    filter.frequency.exponentialRampToValueAtTime(endFreq, startTime + durationSec);
    const gain = this.audioCtx.createGain();
    gain.gain.setValueAtTime(peakGain, startTime);
    gain.gain.exponentialRampToValueAtTime(0.001, startTime + durationSec);
    noise.connect(filter);
    filter.connect(gain);
    gain.connect(this.audioCtx.destination);
    noise.start(startTime);
    noise.stop(startTime + durationSec + 0.01);
  },

  synthWhooshChime(kind = "whoosh_modal", vol = 0.65) {
    if (!this.audioCtx || this.prefs.sfxMuted) return;
    try {
      if (this.audioCtx.state === "suspended") this.audioCtx.resume();
      const now = this.audioCtx.currentTime;
      if (kind === "whoosh_victory") {
        // Lush upward stereo whoosh + soaring C5 -> E5 -> G5 -> B5 -> C6 major 9th crystal arpeggio
        this._playAirWhoosh(now, 0.24, 240, 1850, vol * 0.38);
        const notes = [523.25, 659.25, 783.99, 987.77, 1046.50];
        notes.forEach((f, idx) => {
          const t0 = now + 0.08 + idx * 0.068;
          const osc = this.audioCtx.createOscillator();
          const osc2 = this.audioCtx.createOscillator();
          const g = this.audioCtx.createGain();
          osc.type = "sine";
          osc.frequency.setValueAtTime(f, t0);
          osc2.type = "triangle";
          osc2.frequency.setValueAtTime(f * 2, t0);
          g.gain.setValueAtTime(vol * 0.28, t0);
          g.gain.exponentialRampToValueAtTime(0.001, t0 + 0.55);
          osc.connect(g);
          osc2.connect(g);
          g.connect(this.audioCtx.destination);
          osc.start(t0);
          osc2.start(t0);
          osc.stop(t0 + 0.58);
          osc2.stop(t0 + 0.58);
        });
      } else if (kind === "whoosh_gameover") {
        // Warm downward air whoosh + E5 -> B4 -> A4 gentle resolution beep
        this._playAirWhoosh(now, 0.22, 1350, 260, vol * 0.34);
        const notes = [659.25, 493.88, 440.00];
        notes.forEach((f, idx) => {
          const t0 = now + 0.07 + idx * 0.085;
          const osc = this.audioCtx.createOscillator();
          const g = this.audioCtx.createGain();
          osc.type = "triangle";
          osc.frequency.setValueAtTime(f, t0);
          g.gain.setValueAtTime(vol * 0.26, t0);
          g.gain.exponentialRampToValueAtTime(0.001, t0 + 0.38);
          osc.connect(g);
          g.connect(this.audioCtx.destination);
          osc.start(t0);
          osc.stop(t0 + 0.40);
        });
      } else {
        // "whoosh_modal": Velvety 150ms upward whoosh -> E5 -> B5 crystal fifth beep
        this._playAirWhoosh(now, 0.15, 280, 1480, vol * 0.32);
        const notes = [659.25, 987.77];
        notes.forEach((f, idx) => {
          const t0 = now + 0.065 + idx * 0.068;
          const osc = this.audioCtx.createOscillator();
          const g = this.audioCtx.createGain();
          osc.type = "sine";
          osc.frequency.setValueAtTime(f, t0);
          g.gain.setValueAtTime(vol * 0.30, t0);
          g.gain.exponentialRampToValueAtTime(0.001, t0 + 0.28);
          osc.connect(g);
          g.connect(this.audioCtx.destination);
          osc.start(t0);
          osc.stop(t0 + 0.30);
        });
      }
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
