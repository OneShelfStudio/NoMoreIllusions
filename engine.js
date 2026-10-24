// ============================================================================
// NO MORE ILLUSIONS — CORE APP CONTROLLER, INPUT & RENDER LOOP
// ============================================================================

class NoMoreIllusionsApp {
  getRemixMode() {
    return TRACK_REMIX_MODES.find(m => m.id === this.prefs.selectedRemixModeId) || TRACK_REMIX_MODES[0];
  }

  setRemixMode(modeId) {
    this.unlockAudioOnFirstGesture();
    this.prefs.selectedRemixModeId = modeId;
    localStorage.setItem("nmi_remix_mode", modeId);
    const mode = this.getRemixMode();
    this.synthModeSwitchChime(mode);
    this.updateLobbyPlayback();
    this.syncUI();
  }

  constructor() {
    this.canvas = document.getElementById("game-canvas");
    this.ctx = this.canvas.getContext("2d");
    this.menuTurntableCanvas = document.getElementById("menu-turntable-canvas");
    this.menuTurntableCtx = this.menuTurntableCanvas ? this.menuTurntableCanvas.getContext("2d") : null;
    this.hudRevealCanvas = document.getElementById("hud-reveal-canvas");
    this.hudRevealCtx = this.hudRevealCanvas ? this.hudRevealCanvas.getContext("2d") : null;
    this.previewCanvas = document.getElementById("customizer-preview-canvas");
    this.previewCtx = this.previewCanvas ? this.previewCanvas.getContext("2d") : null;

    // Persistent Preferences (matching DodgePreferences.kt)
    this.prefs = {
      highScore: parseInt(localStorage.getItem("nmi_high_score") || "0", 10),
      points: parseInt(localStorage.getItem("nmi_points") || "350", 10),
      selectedSkin: localStorage.getItem("nmi_skin") || "skin_cyan",
      selectedTrail: localStorage.getItem("nmi_trail") || "trail_cyan",
      selectedTheme: localStorage.getItem("nmi_theme") || "theme_nebula",
      selectedRemixModeId: localStorage.getItem("nmi_remix_mode") || "ORIGINAL",
      unlockedItems: JSON.parse(localStorage.getItem("nmi_unlocked_items") || '["skin_cyan","trail_cyan","theme_nebula"]'),
      unlockedTracks: JSON.parse(localStorage.getItem("nmi_unlocked_tracks") || "[]"),
      trackBestProgress: JSON.parse(localStorage.getItem("nmi_track_progress") || '{"track_01":0,"track_02":0}'),
      trackMasteryMedals: JSON.parse(localStorage.getItem("nmi_track_medals") || '{"track_01":0,"track_02":0}'),
      lobbyThemeIndex: parseInt(localStorage.getItem("nmi_lobby_idx") || "0", 10),
      musicMuted: localStorage.getItem("nmi_music_muted") === "true",
      sfxMuted: localStorage.getItem("nmi_sfx_muted") === "true",
      seenHowToPlay: localStorage.getItem("nmi_seen_htp") === "true",
      vipUnlocked: localStorage.getItem("nmi_vip") === "true"
    };

    // Bitmaps Registry
    this.images = {};
    this.loadAllBitmaps();

    // Audio Registry
    this.lobbyAudio = new Audio();
    this.lobbyAudio.loop = true;
    this.lobbyAudio.volume = 0.55;
    this.trackAudio = new Audio();
    this.trackAudio.preload = "auto";
    this.demoAudio = new Audio();
    this.demoAudio.preload = "auto";
    this.audioUnlocked = false;
    this.sfxPool = {};
    this.initSfxPool();
    this.initH5Ads();

    // Screen State Machine
    this.gameState = "MENU"; // MENU, PLAYING, PAUSED, GAME_OVER, TRACK_COMPLETED, HOW_TO_PLAY, SETTINGS, DEMO_PLAYER, CUSTOMIZER
    this.selectedStageIndex = 0;
    this.customizerTab = 0; // 0: SKINS, 1: TRAILS, 2: THEMES

    // Demo Player State
    this.demoTrackIndex = 0;
    this.demoLoopMode = false;

    // Live Customizer Preview Drag State
    this.previewTouchPos = null;
    this.previewHeroPos = { x: 0, y: 0 };
    this.previewTrail = [];

    // Game Simulation State
    this.time = 0;
    this.lastFrameTime = performance.now();
    this.score = 0;
    this.level = 1;
    this.survivalSeconds = 0;
    this.combo = 1;
    this.maxComboRun = 1;
    this.comboTimer = 0;
    this.orbsCollectedRun = 0;
    this.screenShake = 0;
    this.beatPulse = 0;
    this.shieldTimer = 0;
    this.slowMoTimer = 0;
    this.overdriveTimer = 0;
    this.magnetTimer = 0;
    this.focusCharge = 1.0;
    this.isFocusHolding = false;
    this.reviveUsedThisRun = false;
    this.newlyUnlockedTrack = null;
    this.earnedMedalTier = 0;

    // Player & Entities
    this.playerX = window.innerWidth / 2;
    this.playerY = window.innerHeight * 0.65;
    this.targetX = this.playerX;
    this.targetY = this.playerY;
    this.playerRadius = 22;
    this.trailPoints = Array.from({ length: 28 }, () => ({ x: 0, y: 0 }));
    this.trailLength = 0;

    this.stars = [];
    this.motes = [];
    this.enemies = [];
    this.powerUps = [];
    this.shockwaves = [];
    this.floatingTexts = [];
    this.wormholes = [];
    this.activeWormholeSlide = null;
    this.orbitalPhenomenon = null;
    this.satellites = [];
    this.telegraphs = [];

    // Spawner & Chart Timers
    this.spawnTimer = 0;
    this.powerUpSpawnTimer = 0;
    this.wormholeSpawnTimer = 0;
    this.phenomenonCooldownTimer = 10;
    this.nextBeatCueIndex = 0;
    this.nextChoreoSectionIndex = 0;
    this.lastTriggeredSectionTime = -1;
    this.choreoBannerText = "";
    this.choreoBannerSubtext = "";
    this.choreoBannerTimer = 0;
    this.eclipseModeTimer = 0;
    this.constellationRushTimer = 0;
    this.activeTrackDurationSec = 179;
    this.trackElapsedSec = 0;
    this.multiBandState = new MultiBandState();

    this.resize();
    this.initStars();
    this.bindEvents();
    this.syncUI();
    requestAnimationFrame((ts) => this.loop(ts));
  }

  loadAllBitmaps() {
    const assetMap = {
      logo: "assets/nmi_logo.png",
      hero_cyan: "assets/mandala_hero_cyber.png",
      hero_gold: "assets/mandala_hero_solar.png",
      hero_prism: "assets/mandala_hero_prism.png",
      enemy_pulsar: "assets/mandala_enemy_pulsar.png",
      enemy_seeker: "assets/mandala_enemy_seeker.png",
      enemy_wanderer: "assets/mandala_enemy_wanderer.png"
    };
    for (const [k, src] of Object.entries(assetMap)) {
      const img = new Image();
      img.src = src;
      this.images[k] = img;
    }
  }

  // ==========================================================================
  // GOOGLE ADSENSE H5 GAMES ADS (Ad Placement API) — web/iOS equivalent of Android AdMob
  // Only loads on the AdSense-approved domain (www.digital-dads-llc.com/nomoreillusions/).
  // H5_TEST_MODE serves Google test ads until H5 Games access is approved; flip to false afterwards.
  // ==========================================================================
  savePrefs() {
    localStorage.setItem("nmi_high_score", String(this.prefs.highScore));
    localStorage.setItem("nmi_points", String(this.prefs.points));
    localStorage.setItem("nmi_skin", this.prefs.selectedSkin);
    localStorage.setItem("nmi_trail", this.prefs.selectedTrail);
    localStorage.setItem("nmi_theme", this.prefs.selectedTheme);
    localStorage.setItem("nmi_unlocked_items", JSON.stringify(this.prefs.unlockedItems));
    localStorage.setItem("nmi_unlocked_tracks", JSON.stringify(this.prefs.unlockedTracks));
    localStorage.setItem("nmi_track_progress", JSON.stringify(this.prefs.trackBestProgress));
    localStorage.setItem("nmi_track_medals", JSON.stringify(this.prefs.trackMasteryMedals));
    localStorage.setItem("nmi_lobby_idx", String(this.prefs.lobbyThemeIndex));
    localStorage.setItem("nmi_music_muted", String(this.prefs.musicMuted));
    localStorage.setItem("nmi_sfx_muted", String(this.prefs.sfxMuted));
  }

  resize() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    this.W = window.innerWidth;
    this.H = window.innerHeight;
    this.canvas.width = Math.round(this.W * dpr);
    this.canvas.height = Math.round(this.H * dpr);
    this.ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

    if (this.menuTurntableCanvas && this.menuTurntableCtx) {
      this.menuTurntableCanvas.width = Math.round(138 * dpr);
      this.menuTurntableCanvas.height = Math.round(138 * dpr);
      this.menuTurntableCtx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }
    if (this.hudRevealCanvas && this.hudRevealCtx) {
      this.hudRevealCanvas.width = Math.round(76 * dpr);
      this.hudRevealCanvas.height = Math.round(76 * dpr);
      this.hudRevealCtx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }
    if (this.previewCanvas && this.previewCtx) {
      const rect = this.previewCanvas.getBoundingClientRect();
      const pw = rect.width || 320;
      const ph = rect.height || 135;
      this.previewCanvas.width = Math.round(pw * dpr);
      this.previewCanvas.height = Math.round(ph * dpr);
      this.previewCtx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }
  }

  initStars() {
    this.stars = [];
    for (let i = 0; i < 85; i++) {
      const layer = i % 3;
      this.stars.push({
        x: Math.random() * this.W,
        y: Math.random() * this.H,
        speed: layer === 0 ? 18 : layer === 1 ? 42 : 85,
        size: layer === 0 ? 1.4 : layer === 1 ? 2.3 : 3.5,
        alpha: layer === 0 ? 0.35 : layer === 1 ? 0.65 : 0.92,
        twinkleOffset: Math.random() * 6.28
      });
    }
  }

  getHeroBitmap(skinId = this.prefs.selectedSkin) {
    if (skinId === "skin_gold") return this.images.hero_gold;
    if (skinId === "skin_prism") return this.images.hero_prism;
    if (skinId === "skin_emerald") return this.images.hero_cyan;
    if (skinId === "skin_blood") return this.images.hero_gold;
    return this.images.hero_cyan;
  }

  getEnemyBitmaps() {
    return {
      PULSAR: this.images.enemy_pulsar,
      SEEKER: this.images.enemy_seeker,
      WANDERER: this.images.enemy_wanderer
    };
  }

  bindEvents() {
    window.addEventListener("resize", () => this.resize());

    // Interactive Main Menu Turntable Scratch / Flick Controls
    if (this.menuTurntableCanvas) {
      let isDraggingTurntable = false;
      let lastAngle = 0;
      let lastTime = 0;

      const getAngle = (e) => {
        const rect = this.menuTurntableCanvas.getBoundingClientRect();
        const cx = rect.left + rect.width / 2;
        const cy = rect.top + rect.height / 2;
        return Math.atan2(e.clientY - cy, e.clientX - cx);
      };

      this.menuTurntableCanvas.addEventListener("pointerdown", (e) => {
        this.unlockAudioOnFirstGesture();
        isDraggingTurntable = true;
        lastAngle = getAngle(e);
        lastTime = performance.now();
        this.turntableSpinVel = 0;
      });

      window.addEventListener("pointermove", (e) => {
        if (!isDraggingTurntable) return;
        const curAngle = getAngle(e);
        const curTime = performance.now();
        const dt = Math.max(1, curTime - lastTime) / 1000;
        let delta = curAngle - lastAngle;
        while (delta > Math.PI) delta -= Math.PI * 2;
        while (delta < -Math.PI) delta += Math.PI * 2;

        this.turntableSpinVel = delta / dt;
        lastAngle = curAngle;
        lastTime = curTime;

        // Modulate lobby audio playback rate based on scratch speed for real vinyl feel!
        if (this.lobbyAudio && !this.lobbyAudio.paused) {
          try {
            const rate = Math.max(0.4, Math.min(2.2, 1.0 + (delta * 4.5)));
            this.lobbyAudio.playbackRate = rate;
          } catch (err) {}
        }
      });

      const endScratch = () => {
        if (isDraggingTurntable) {
          isDraggingTurntable = false;
          if (this.lobbyAudio) this.lobbyAudio.playbackRate = 1.0;
        }
      };
      window.addEventListener("pointerup", endScratch);
      window.addEventListener("pointercancel", endScratch);
    }

    // Pointer / Touch Controls on Game Canvas
    const updatePointer = (clientX, clientY) => {
      this.unlockAudioOnFirstGesture();
      if (this.gameState === "PLAYING") {
        this.targetX = Math.max(this.playerRadius, Math.min(this.W - this.playerRadius, clientX));
        // Slight vertical offset on touch so finger doesn't cover core
        const offsetY = clientY - 36;
        this.targetY = Math.max(this.playerRadius, Math.min(this.H - this.playerRadius, offsetY));
      }
    };

    this.canvas.addEventListener("pointerdown", (e) => {
      updatePointer(e.clientX, e.clientY);
    });
    this.canvas.addEventListener("pointermove", (e) => {
      if (e.buttons > 0 || e.pointerType === "touch") {
        updatePointer(e.clientX, e.clientY);
      }
    });

    // Live Customizer Preview Drag
    if (this.previewCanvas) {
      const onPrevMove = (e) => {
        const r = this.previewCanvas.getBoundingClientRect();
        this.previewTouchPos = {
          x: e.clientX - r.left,
          y: e.clientY - r.top
        };
      };
      this.previewCanvas.addEventListener("pointerdown", onPrevMove);
      this.previewCanvas.addEventListener("pointermove", (e) => {
        if (e.buttons > 0 || e.pointerType === "touch") onPrevMove(e);
      });
      this.previewCanvas.addEventListener("pointerup", () => { this.previewTouchPos = null; });
      this.previewCanvas.addEventListener("pointercancel", () => { this.previewTouchPos = null; });
    }

    // Focus Button Hold
    const focusBtn = document.getElementById("btn-focus-hold");
    if (focusBtn) {
      focusBtn.addEventListener("pointerdown", (e) => {
        e.stopPropagation();
        if (this.focusCharge > 0.12) this.isFocusHolding = true;
      });
      window.addEventListener("pointerup", () => { this.isFocusHolding = false; });
      window.addEventListener("pointercancel", () => { this.isFocusHolding = false; });
    }

    // Track Audio Ended -> Track Completed (or Endless Loop Escalation)!
    this.trackAudio.addEventListener("ended", () => {
      if (this.gameState === "PLAYING") {
        if (this.getRemixMode().id === "ENDLESS") this.advanceEndlessLoop();
        else this.completeStageTrack();
      }
    });

    // Demo Audio Ended
    this.demoAudio.addEventListener("ended", () => {
      if (this.demoLoopMode) {
        this.demoAudio.currentTime = 0;
        this.demoAudio.play().catch(() => {});
      } else {
        this.syncDemoPlayerUI();
      }
    });
  }

  // ==========================================================================
  // STAGE LAUNCH, PAUSE, REVIVE & COMPLETION
  // ==========================================================================
  dismissHowToPlay(startImmediately = false, dontShowAgain = false) {
    this.playSfx("click");
    if (dontShowAgain) {
      this.prefs.seenHowToPlay = true;
      localStorage.setItem("nmi_seen_htp", "true");
    }
    const shouldLaunch = startImmediately || this.pendingStageLaunch;
    this.pendingStageLaunch = false;

    const card = document.getElementById("htp-modal-card");
    if (card) {
      card.classList.add("bounce-out");
      setTimeout(() => {
        card.classList.remove("bounce-out");
        if (shouldLaunch) {
          this.launchStageCountdown();
        } else {
          this.openScreen("MENU");
        }
      }, 250);
    } else {
      if (shouldLaunch) {
        this.launchStageCountdown();
      } else {
        this.openScreen("MENU");
      }
    }
  }

  startStage(stageIndex = this.selectedStageIndex) {
    this.unlockAudioOnFirstGesture();
    this.playSfx("click");
    this.selectedStageIndex = stageIndex;

    // Show centered zoom-bounce How-To-Play pop-up before countdown starts (unless user clicked "Don't Show Again")
    if (!this.prefs.seenHowToPlay) {
      this.pendingStageLaunch = true;
      this.openScreen("HOW_TO_PLAY");
      return;
    }

    this.launchStageCountdown();
  }

  // Replay from Game Over: every 3rd Game Over shows an interstitial first (1:1 with Android AdManager cadence)
  replayFromGameOver() {
    const go = () => this.startStage();
    if (this.gameOverCount > 0 && this.gameOverCount % 3 === 0) this.showInterstitialThen(go);
    else go();
  }

  launchStageCountdown() {
    this.gameState = "COUNTDOWN";
    this.lobbyAudio.pause();
    this.trackAudio.pause();
    this.syncUI();

    const countdownEl = document.getElementById("screen-COUNTDOWN");
    const countValEl = document.getElementById("countdown-val");
    if (countdownEl) countdownEl.style.display = "flex";

    let count = 3;
    if (countValEl) countValEl.textContent = String(count);
    this.playSfx("click", 0.7);

    if (this.countdownTimerId) clearInterval(this.countdownTimerId);

    this.countdownTimerId = setInterval(() => {
      count--;
      if (count > 0) {
        if (countValEl) countValEl.textContent = String(count);
        this.playSfx("click", 0.7);
      } else if (count === 0) {
        if (countValEl) countValEl.textContent = "DROP!";
        this.playSfx("orb", 0.85);
      } else {
        clearInterval(this.countdownTimerId);
        if (countdownEl) countdownEl.style.display = "none";
        this.executeStageRun();
      }
    }, 850);
  }

  executeStageRun() {
    const stage = OFFICIAL_TRACKS[this.selectedStageIndex] || OFFICIAL_TRACKS[0];

    this.score = 0;
    this.level = 1;
    this.survivalSeconds = 0;
    this.combo = 1;
    this.maxComboRun = 1;
    this.comboTimer = 0;
    this.orbsCollectedRun = 0;
    this.screenShake = 0;
    this.shieldTimer = 0;
    this.slowMoTimer = 0;
    this.overdriveTimer = 0;
    this.magnetTimer = 0;
    this.focusCharge = 1.0;
    this.isFocusHolding = false;
    this.reviveUsedThisRun = false;
    this.newlyUnlockedTrack = null;
    this.earnedMedalTier = 0;
    this.endlessLoopCount = 0;
    this.activeToast = null;

    this.playerX = this.W / 2;
    this.playerY = this.H * 0.65;
    this.targetX = this.playerX;
    this.targetY = this.playerY;
    this.trailLength = 0;

    this.enemies = [];
    this.powerUps = [];
    this.motes = [];
    this.shockwaves = [];
    this.floatingTexts = [];
    this.wormholes = [];
    this.activeWormholeSlide = null;
    this.orbitalPhenomenon = null;
    this.satellites = [];
    this.telegraphs = [];

    this.spawnTimer = 0;
    this.powerUpSpawnTimer = 0;
    this.wormholeSpawnTimer = 0;
    this.phenomenonCooldownTimer = 10;
    this.nextBeatCueIndex = 0;
    this.nextChoreoSectionIndex = 0;
    this.lastTriggeredSectionTime = -1;
    this.choreoBannerText = "";
    this.choreoBannerSubtext = "";
    this.choreoBannerTimer = 0;
    this.eclipseModeTimer = 0;
    this.constellationRushTimer = 0;
    this.activeTrackDurationSec = stage.durationSec;
    this.trackElapsedSec = 0;

    this.lobbyAudio.pause();
    this.demoAudio.pause();
    this.trackAudio.src = stage.src;
    this.trackAudio.currentTime = 0;
    this.trackAudio.muted = this.prefs.musicMuted;
    const mode = this.getRemixMode();
    try {
      this.trackAudio.preservesPitch = false;
      this.trackAudio.webkitPreservesPitch = false;
      this.trackAudio.mozPreservesPitch = false;
      this.trackAudio.playbackRate = mode.speed || 1.0;
    } catch (e) {}
    this.trackAudio.play().catch(() => {});

    this.gameState = "PLAYING";
    this.syncUI();
  }

  pauseGame() {
    if (this.gameState !== "PLAYING") return;
    this.playSfx("click");
    this.trackAudio.pause();
    this.gameState = "PAUSED";
    this.syncUI();
  }

  resumeGame() {
    if (this.gameState !== "PAUSED") return;
    this.playSfx("click");
    if (!this.prefs.musicMuted) this.trackAudio.play().catch(() => {});
    this.gameState = "PLAYING";
    this.syncUI();
  }

  advanceEndlessLoop() {
    this.endlessLoopCount = (this.endlessLoopCount || 0) + 1;
    const newSpeed = Math.min(1.65, 1.0 + this.endlessLoopCount * 0.08);
    this.nextBeatCueIndex = 0;
    this.lastTriggeredSectionTime = -1;
    this.trackElapsedSec = 0;
    const bonus = 500 * this.endlessLoopCount;
    this.score += bonus;
    this.prefs.points += 25;
    try {
      this.trackAudio.currentTime = 0;
      this.trackAudio.preservesPitch = false;
      this.trackAudio.webkitPreservesPitch = false;
      this.trackAudio.playbackRate = newSpeed;
      if (!this.prefs.musicMuted) this.trackAudio.play().catch(() => {});
    } catch (e) {}
    this.screenShake = Math.max(this.screenShake, 12);
    this.shockwaves.push({ x: this.W / 2, y: this.H * 0.45, radius: 12, maxRadius: Math.max(this.W, this.H) * 0.85, color: [250, 204, 21], alpha: 1 });
    this.showToast("∞", `ENDLESS LOOP #${this.endlessLoopCount + 1}!`, `SONG SPEED ESCALATED TO ${newSpeed.toFixed(2)}x • +${bonus} PTS`, "#FACC15", 3.2);
  }

  // Revive using 250 ♪ Notes (1:1 with Android GameOverUI)
  reviveWithNotes() {
    if (this.prefs.points < 250) {
      this.playSfx("click");
      this.openScreen("CUSTOMIZER");
      return;
    }
    this.prefs.points -= 250;
    this.savePrefs();
    this.performRevive();
  }

  // Rewarded "Ad" revive. AdMob only serves inside the native Android app, so on Web/iOS this shows a
  // 5-second house promo for No More Illusions' streaming links instead of a third-party ad.
  reviveWithAd() {
    if (this.reviveUsedThisRun) return;
    this.playSfx("click");
    // Real Google rewarded ad (H5 Games Ads) when one is ready on the approved domain
    if (typeof this.pendingShowRewardAd === "function") {
      const show = this.pendingShowRewardAd;
      this.pendingShowRewardAd = null;
      try { show(); return; } catch (e) {}
    }
    // Fallback: 5-second No More Illusions house promo (no third-party ad available)
    const overlay = document.getElementById("house-ad-overlay");
    const countEl = document.getElementById("house-ad-count");
    if (!overlay) { this.reviveUsedThisRun = true; this.performRevive(); return; }
    overlay.style.display = "flex";
    let remaining = 5;
    if (countEl) countEl.textContent = String(remaining);
    if (this.houseAdTimer) clearInterval(this.houseAdTimer);
    this.houseAdTimer = setInterval(() => {
      remaining--;
      if (countEl) countEl.textContent = String(Math.max(0, remaining));
      if (remaining <= 0) {
        clearInterval(this.houseAdTimer);
        overlay.style.display = "none";
        this.reviveUsedThisRun = true;
        this.performRevive();
      }
    }, 1000);
  }

  revivePlayer() { this.reviveWithAd(); }

  performRevive() {
    this.playSfx("powerup");
    this.enemies = [];
    this.telegraphs = [];
    this.satellites = [];
    this.orbitalPhenomenon = null;
    this.shieldTimer = Math.max(this.shieldTimer, 3.0);
    this.overdriveTimer = Math.max(this.overdriveTimer, 2.5);
    this.shockwaves.push({
      x: this.playerX,
      y: this.playerY,
      radius: 10,
      maxRadius: Math.max(this.W, this.H) * 0.65,
      color: [74, 222, 128],
      alpha: 1
    });
    this.showToast("🛡️", "REVIVED!", "3.0s INVINCIBILITY SHIELD ACTIVE", "#4ADE80", 2.6);
    if (!this.prefs.musicMuted) this.trackAudio.play().catch(() => {});
    this.gameState = "PLAYING";
    this.syncUI();
  }

  // Artist VIP Master Passcode (ETHAN / CONFIDE / ILLUSIONS2026 / NMI2026) — 1:1 with Android
  completeStageTrack() {
    if (this.gameState !== "PLAYING") return;
    const stage = OFFICIAL_TRACKS[this.selectedStageIndex];
    this.trackAudio.pause();
    this.playSfx("levelup", 0.85);

    const completionBonus = 500;
    const totalEarned = Math.max(50, Math.floor(this.score / 8)) + completionBonus;
    this.prefs.points += totalEarned;
    if (this.score > this.prefs.highScore) this.prefs.highScore = this.score;

    this.prefs.trackBestProgress[stage.id] = 1.0;
    const wasUnlocked = this.prefs.unlockedTracks.includes(stage.id);
    if (!wasUnlocked) {
      this.prefs.unlockedTracks.push(stage.id);
      this.newlyUnlockedTrack = stage;
    } else {
      this.newlyUnlockedTrack = null;
    }

    const mode = this.getRemixMode();
    let stars = 1; // ★1: Clear song
    if (this.maxComboRun >= 8) stars = 2; // ★2: 8x Beat streak
    if (mode.id === "OVERCLOCK" || mode.id === "SLOWED") stars = 3; // ★3: Beat Nightcore/Slowed
    this.earnedMedalTier = stars;
    if (stars > (this.prefs.trackMasteryMedals[stage.id] || 0)) {
      this.prefs.trackMasteryMedals[stage.id] = stars;
    }

    this.savePrefs();
    this.gameState = "TRACK_COMPLETED";
    this.syncUI();
  }

  triggerGameOver() {
    const stage = OFFICIAL_TRACKS[this.selectedStageIndex];
    this.trackAudio.pause();
    this.playSfx("gameover", 0.8);

    const progress = Math.max(0, Math.min(1, this.trackElapsedSec / Math.max(1, this.activeTrackDurationSec)));
    if (progress > (this.prefs.trackBestProgress[stage.id] || 0)) {
      this.prefs.trackBestProgress[stage.id] = progress;
    }

    if (this.score > this.prefs.highScore) this.prefs.highScore = this.score;
    const earned = Math.max(5, Math.floor(this.score / 10));
    this.prefs.points += earned;
    this.savePrefs();

    this.gameState = "GAME_OVER";
    this.gameOverCount = (this.gameOverCount || 0) + 1;
    this.syncUI();
    this.prepareRewardedRevive();
  }

  // ==========================================================================
  // PHYSICS STEP (1:1 Port of GamePhysics.kt)
  // ==========================================================================
  loop(timestamp) {
    const dt = Math.min(0.05, Math.max(0.001, (timestamp - this.lastFrameTime) / 1000));
    this.lastFrameTime = timestamp;
    this.time += dt;

    // Update Starfield
    for (let i = 0; i < this.stars.length; i++) {
      const s = this.stars[i];
      s.y += s.speed * dt;
      if (s.y > this.H) {
        s.y = -4;
        s.x = Math.random() * this.W;
      }
    }

    if (this.gameState === "PLAYING") {
      this.stepPhysics(dt);
    } else {
      // Idle gentle beat pulse for menu & background
      this.beatPulse = 0.5 + 0.5 * Math.sin(this.time * 2.1 * Math.PI * 2);
      this.multiBandState.bass = 0.32 + 0.25 * Math.sin(this.time * 1.9);
      this.multiBandState.vocal = 0.35 + 0.30 * Math.cos(this.time * 1.3);
      this.multiBandState.high = 0.28 + 0.22 * Math.sin(this.time * 3.1);
      this.multiBandState.bassDropOut = false;
      this.multiBandState.isClimax = false;
    }

    this.renderGameCanvas();

    if (this.turntableSpinVel) {
      this.turntableAngle = (this.turntableAngle || 0) + this.turntableSpinVel * dt;
      this.turntableSpinVel *= Math.pow(0.08, dt); // smooth vinyl damping
      if (Math.abs(this.turntableSpinVel) < 0.05) this.turntableSpinVel = 0;
    }

    if (this.gameState === "MENU" && this.menuTurntableCtx) {
      const allGold = OFFICIAL_TRACKS.every((t) => (this.prefs.trackMasteryMedals[t.id] || 0) >= 3);
      const effectiveTime = this.time + (this.turntableAngle || 0) * 0.45;
      drawMenuTurntable(this.menuTurntableCtx, this.images.logo, effectiveTime, allGold);
    }

    if (this.gameState === "CUSTOMIZER" && this.previewCtx) {
      this.renderCustomizerPreview(dt);
    }

    if (this.gameState === "DEMO_PLAYER") {
      this.updateDemoPlayerProgress();
    }

    requestAnimationFrame((ts) => this.loop(ts));
  }

  renderGameCanvas() {
    const ctx = this.ctx;
    ctx.save();

    if (this.adrenalineGlitch > 0) {
      this.adrenalineGlitch = Math.max(0, this.adrenalineGlitch - 0.016 * 2.5);
    }

    if (this.screenShake > 0.2 && this.gameState === "PLAYING") {
      const sx = (Math.random() - 0.5) * this.screenShake;
      const sy = (Math.random() - 0.5) * this.screenShake;
      ctx.translate(sx, sy);
    }

    const palette = getThemePalette(this.prefs.selectedTheme);
    const trackProgress = this.gameState === "PLAYING"
      ? Math.max(0, Math.min(1, this.trackElapsedSec / Math.max(1, this.activeTrackDurationSec)))
      : 0;

    drawNebulaBackground(
      ctx,
      this.W,
      this.H,
      palette,
      this.stars,
      this.time,
      this.beatPulse,
      this.multiBandState,
      this.eclipseModeTimer > 0,
      this.constellationRushTimer > 0,
      this.images.logo,
      trackProgress
    );

    if (this.gameState === "PLAYING" || this.gameState === "PAUSED" || this.gameState === "GAME_OVER" || this.gameState === "TRACK_COMPLETED") {
      drawWormholes(ctx, this.wormholes, this.time, this.beatPulse);
      drawOrbitalPhenomenonAndSatellites(ctx, this.orbitalPhenomenon, this.satellites, this.time, this.beatPulse, this.multiBandState.vocal);
      drawMotes(ctx, this.motes);
      drawPlayerTrail(ctx, this.trailPoints, this.trailLength, this.prefs.selectedTrail);
      drawShockwaves(ctx, this.shockwaves);
      drawPowerUps(ctx, this.powerUps, this.playerX, this.playerY, this.time, this.trackElapsedSec, this.beatPulse, this.constellationRushTimer > 0, this.multiBandState);
      drawEnemyTelegraphs(ctx, this.telegraphs, this.time, this.beatPulse);

      const enemyBmps = this.getEnemyBitmaps();
      for (let i = 0; i < this.enemies.length; i++) {
        drawEnemySprite(ctx, this.enemies[i], enemyBmps, this.time, this.beatPulse);
      }

      drawPlayerSprite(
        ctx,
        this.playerX,
        this.playerY,
        this.playerRadius,
        this.getHeroBitmap(),
        this.prefs.selectedSkin,
        this.shieldTimer,
        this.overdriveTimer,
        this.magnetTimer,
        this.isFocusHolding,
        this.focusCharge,
        this.beatPulse,
        this.time
      );

      drawFloatingTexts(ctx, this.floatingTexts);
    }

    ctx.restore();
  }

}

window.addEventListener("DOMContentLoaded", () => {
  window.app = new NoMoreIllusionsApp();
});

