// ============================================================================
// NO MORE ILLUSIONS — UI SCREENS, CUSTOMIZER, VAULT & DEMO PLAYER
// ============================================================================

Object.assign(NoMoreIllusionsApp.prototype, {
  openVipPasscode() {
    this.playSfx("click");
    const modal = document.getElementById("vip-modal");
    const input = document.getElementById("vip-input");
    const err = document.getElementById("vip-error");
    if (err) err.style.display = "none";
    if (input) input.value = "";
    if (modal) modal.style.display = "flex";
    setTimeout(() => { if (input) input.focus(); }, 60);
  },

  closeVipPasscode() {
    const modal = document.getElementById("vip-modal");
    if (modal) modal.style.display = "none";
  },

  submitVipPasscode() {
    const input = document.getElementById("vip-input");
    const err = document.getElementById("vip-error");
    const clean = (input ? input.value : "").trim().toUpperCase();
    if (["ETHAN", "CONFIDE", "ILLUSIONS2026", "NMI2026"].includes(clean)) {
      this.prefs.vipUnlocked = true;
      localStorage.setItem("nmi_vip", "true");
      this.prefs.points += 50000;
      const allItems = ["skin_cyan", "skin_gold", "skin_prism", "skin_emerald", "skin_blood",
        "trail_cyan", "trail_gold", "trail_emerald", "trail_prism", "trail_laser",
        "theme_nebula", "theme_sunset", "theme_matrix", "theme_blood", "theme_gold"];
      for (const it of allItems) if (!this.prefs.unlockedItems.includes(it)) this.prefs.unlockedItems.push(it);
      for (const t of OFFICIAL_TRACKS) if (!this.prefs.unlockedTracks.includes(t.id)) this.prefs.unlockedTracks.push(t.id);
      this.savePrefs();
      this.playSfx("levelup", 0.85);
      this.closeVipPasscode();
      this.syncUI();
      if (this.gameState === "CUSTOMIZER") this.syncCustomizerUI();
    } else if (err) {
      err.style.display = "block";
    }
  },


  renderCustomizerPreview(dt) {
    const ctx = this.previewCtx;
    const rect = this.previewCanvas.getBoundingClientRect();
    const w = rect.width || 320;
    const h = rect.height || 135;

    const palette = getThemePalette(this.prefs.selectedTheme);
    const bgGrad = ctx.createLinearGradient(0, 0, 0, h);
    bgGrad.addColorStop(0, rgb(palette.bgTop));
    bgGrad.addColorStop(0.5, rgb(palette.bgMid));
    bgGrad.addColorStop(1, rgb(palette.bgBot));
    ctx.fillStyle = bgGrad;
    ctx.fillRect(0, 0, w, h);

    // Organic rotating halo petal arcs inside preview box
    const maxDim = Math.max(w, h);
    for (let i = 0; i < 4; i++) {
      const rawPhase = ((i / 4) + this.time * 0.12) % 1;
      const ringRad = rawPhase * rawPhase * maxDim * 0.52;
      if (ringRad < 12) continue;
      const col = i % 2 === 0 ? palette.nebTint : palette.starTint;
      const fadeAlpha = Math.sin(rawPhase * Math.PI) * 0.24;
      const baseSpin = this.time * (i % 2 === 0 ? 0.45 : -0.38) + i * 0.8;
      for (let p = 0; p < 3; p++) {
        const startA = baseSpin + p * ((Math.PI * 2) / 3);
        ctx.beginPath();
        ctx.arc(w / 2, h / 2, ringRad, startA, startA + 1.35);
        ctx.strokeStyle = rgba(col, fadeAlpha);
        ctx.lineWidth = 1.6;
        ctx.lineCap = "round";
        ctx.stroke();
      }
    }

    // Target position: user drag or smooth figure-8 orbit
    let tx, ty;
    if (this.previewTouchPos) {
      tx = Math.max(28, Math.min(w - 28, this.previewTouchPos.x));
      ty = Math.max(28, Math.min(h - 28, this.previewTouchPos.y));
    } else {
      tx = w * 0.5 + Math.cos(this.time * 2.1) * (w * 0.28);
      ty = h * 0.54 + Math.sin(this.time * 4.2) * (h * 0.20);
    }

    if (this.previewHeroPos.x === 0 && this.previewHeroPos.y === 0) {
      this.previewHeroPos.x = tx;
      this.previewHeroPos.y = ty;
    } else {
      this.previewHeroPos.x += (tx - this.previewHeroPos.x) * 0.22;
      this.previewHeroPos.y += (ty - this.previewHeroPos.y) * 0.22;
    }

    this.previewTrail.unshift({ x: this.previewHeroPos.x, y: this.previewHeroPos.y });
    if (this.previewTrail.length > 22) this.previewTrail.pop();

    drawPlayerTrail(ctx, this.previewTrail, this.previewTrail.length, this.prefs.selectedTrail);

    let skinCol = [56, 189, 248];
    if (this.prefs.selectedSkin === "skin_gold") skinCol = [250, 204, 21];
    else if (this.prefs.selectedSkin === "skin_prism") skinCol = [236, 72, 153];

    const haloGrad = ctx.createRadialGradient(
      this.previewHeroPos.x, this.previewHeroPos.y, 2,
      this.previewHeroPos.x, this.previewHeroPos.y, 44
    );
    haloGrad.addColorStop(0, rgba(skinCol, 0.58));
    haloGrad.addColorStop(1, "rgba(0,0,0,0)");
    ctx.beginPath();
    ctx.arc(this.previewHeroPos.x, this.previewHeroPos.y, 44, 0, Math.PI * 2);
    ctx.fillStyle = haloGrad;
    ctx.fill();

    const bmp = this.getHeroBitmap();
    if (bmp && bmp.complete && bmp.naturalWidth > 0) {
      ctx.save();
      ctx.translate(this.previewHeroPos.x, this.previewHeroPos.y);
      ctx.rotate(((this.time * 45) % 360) * Math.PI / 180);
      ctx.globalCompositeOperation = "screen";
      ctx.drawImage(bmp, -28, -28, 56, 56);
      ctx.restore();
    }
  },

  updateLiveHUD() {
    const stage = OFFICIAL_TRACKS[this.selectedStageIndex] || OFFICIAL_TRACKS[0];
    const progress = Math.max(0, Math.min(1, this.trackElapsedSec / Math.max(1, this.activeTrackDurationSec)));

    const scoreEl = document.getElementById("hud-score-val");
    if (scoreEl) scoreEl.textContent = String(this.score);

    const phaseEl = document.getElementById("hud-phase-val");
    if (phaseEl) phaseEl.textContent = `PHASE ${this.level}`;

    const comboChip = document.getElementById("hud-combo-chip");
    const comboVal = document.getElementById("hud-combo-val");
    if (comboChip && comboVal) {
      if (this.combo > 1 || this.constellationRushTimer > 0) {
        comboChip.style.display = "inline-flex";
        const effMult = this.combo * (this.constellationRushTimer > 0 ? 2 : 1);
        comboVal.textContent = this.constellationRushTimer > 0 ? `★ RUSH x${effMult}` : `COMBO x${this.combo}`;
      } else {
        comboChip.style.display = "none";
      }
    }

    // Power-up status badges (1:1 with Android ActiveBuffChip)
    const badgesEl = document.getElementById("hud-powerup-badges");
    if (badgesEl) {
      let html = "";
      if (this.overdriveTimer > 0) html += `<div class="pu-pill" style="border-color:#F97316;color:#F97316;">💥 OVERDRIVE ${this.overdriveTimer.toFixed(1)}s</div>`;
      if (this.shieldTimer > 0) html += `<div class="pu-pill" style="border-color:#38BDF8;color:#38BDF8;">🛡️ SHIELD ${this.shieldTimer.toFixed(1)}s</div>`;
      if (this.slowMoTimer > 0) html += `<div class="pu-pill" style="border-color:#A855F7;color:#A855F7;">⏳ SLOW-MO ${this.slowMoTimer.toFixed(1)}s</div>`;
      if (this.magnetTimer > 0) html += `<div class="pu-pill" style="border-color:#10B981;color:#10B981;">🧲 MAGNET ${this.magnetTimer.toFixed(1)}s</div>`;
      if (this.endlessLoopCount > 0) html += `<div class="pu-pill" style="border-color:#FACC15;color:#FACC15;">∞ LOOP #${this.endlessLoopCount + 1} (${Math.min(1.65, 1 + this.endlessLoopCount * 0.08).toFixed(2)}x)</div>`;
      if (badgesEl.dataset.last !== html) {
        badgesEl.innerHTML = html;
        badgesEl.dataset.last = html;
      }
    }

    // Candy-Catcher-Style Top-Center Pickup Toast
    const toastEl = document.getElementById("hud-pickup-toast");
    if (toastEl) {
      const t = this.activeToast;
      if (t && t.timer > 0) {
        if (toastEl.dataset.key !== t.title) {
          toastEl.dataset.key = t.title;
          document.getElementById("hud-toast-icon").textContent = t.icon;
          document.getElementById("hud-toast-title").textContent = t.title;
          const sub = document.getElementById("hud-toast-sub");
          sub.textContent = t.subtitle;
          sub.style.color = t.accent;
          toastEl.style.borderColor = t.accent;
          toastEl.style.boxShadow = `0 0 24px ${t.accent}55`;
          document.getElementById("hud-toast-icon").style.borderColor = t.accent;
          document.getElementById("hud-toast-bar").style.background = t.accent;
        }
        toastEl.style.display = "flex";
        document.getElementById("hud-toast-bar").style.width = `${Math.round((t.timer / t.maxTimer) * 100)}%`;
      } else {
        toastEl.style.display = "none";
        toastEl.dataset.key = "";
      }
    }

    // Track Reveal Overlay Card
    if (this.hudRevealCtx) {
      drawTrackRevealCanvas(this.hudRevealCtx, this.images.logo, progress, this.beatPulse, this.constellationRushTimer > 0);
    }
    const pctBadge = document.getElementById("hud-reveal-pct");
    if (pctBadge) {
      pctBadge.textContent = `${Math.round(progress * 100)}%`;
      pctBadge.style.color = (this.constellationRushTimer > 0 || progress >= 0.99) ? "#FACC15" : "#38BDF8";
    }
    const trackTitleHud = document.getElementById("hud-track-title");
    if (trackTitleHud) trackTitleHud.textContent = stage.title.toUpperCase();

    const remSec = Math.max(0, Math.round(this.activeTrackDurationSec - this.trackElapsedSec));
    const remMin = Math.floor(remSec / 60);
    const remS = String(remSec % 60).padStart(2, "0");
    const remEl = document.getElementById("hud-track-rem");
    if (remEl) {
      remEl.textContent = progress >= 0.99 ? "UNLOCKED!" : `-${remMin}:${remS} LEFT`;
      remEl.style.color = progress >= 0.99 ? "#4ADE80" : "#94A3B8";
    }

    // Choreographed Section Banner
    const bannerEl = document.getElementById("hud-choreo-banner");
    if (bannerEl) {
      if (this.choreoBannerTimer > 0 && this.choreoBannerText) {
        bannerEl.style.display = "block";
        document.getElementById("hud-choreo-title").textContent = this.choreoBannerText;
        document.getElementById("hud-choreo-sub").textContent = this.choreoBannerSubtext;
      } else {
        bannerEl.style.display = "none";
      }
    }

    // Focus Charge Bar
    const focusFill = document.getElementById("hud-focus-fill");
    const focusBtn = document.getElementById("btn-focus-hold");
    if (focusFill) {
      focusFill.style.width = `${Math.round(this.focusCharge * 100)}%`;
      focusFill.style.background = this.isFocusHolding ? "#FACC15" : "#C084FC";
    }
    if (focusBtn) {
      focusBtn.style.borderColor = this.isFocusHolding ? "#FACC15" : "rgba(192,132,252,0.75)";
      focusBtn.style.color = this.isFocusHolding ? "#FACC15" : "#E9D5FF";
    }
  },

    openLinerNotes(trackIndex = this.selectedStageIndex) {
    this.playSfx("click");
    const track = OFFICIAL_TRACKS[trackIndex] || OFFICIAL_TRACKS[0];
    const notesModal = document.getElementById("screen-LINER_NOTES");
    const titleEl = document.getElementById("liner-track-title");
    const metaEl = document.getElementById("liner-track-meta");
    const storyEl = document.getElementById("liner-track-story");
    const lyricsEl = document.getElementById("liner-track-lyrics");

    if (titleEl) titleEl.textContent = track.title.toUpperCase();
    if (metaEl) metaEl.textContent = `${track.bpm} BPM • C MINOR • 432Hz ANALOG MASTER`;

    const stories = {
      track_01: "Written during late-night studio sessions surrounded by analog synthesizers and tape machines. 'Confide' explores the vulnerable threshold between holding up emotional armor and letting someone see what lies beneath. The composition deliberately contrasts intimate, zero-bass vocal passages with eruptive, volcanic multi-layered synthesizer drops.",
      track_02: "Built upon heavy, hypnotic 808 sub-bass foundations and warm vintage Rhodes piano chords. 'Number One' showcases Ethan's rhythmic rap flow and smooth vocal delivery. Crafted for night drives and high-retention acoustic immersion, the track ascends into an all-instruments-peaking grand finale."
    };

    const lyrics = {
      track_01: "\"Can I confide in you tonight?\nNo more illusions to hide behind...\nThrough the dark void and the frequency light,\nWe break the signal and come alive.\"",
      track_02: "\"Started from zero now we taking flight,\nNumber one frequency burning bright...\nBassline knocking through the midnight air,\nTell them No More Illusions is already there.\""
    };

    if (storyEl) storyEl.textContent = stories[track.id] || stories.track_01;
    if (lyricsEl) lyricsEl.textContent = lyrics[track.id] || lyrics.track_01;

    this.gameState = "LINER_NOTES";
    this.syncUI();
  },

  closeLinerNotes() {
    this.playSfx("whoosh_modal", 0.78);
    this.openScreen("CUSTOMIZER", -1);
  },

  openScreen(stateName, customTab = -1) {
    this.unlockAudioOnFirstGesture();
    this.playSfx("whoosh_modal", 0.80);
    if (this.gameState === "DEMO_PLAYER" && stateName !== "DEMO_PLAYER") {
      this.demoAudio.pause();
    }
    this.gameState = stateName;
    if (stateName === "CUSTOMIZER") {
      this.customizerTab = typeof customTab === "number" ? customTab : -1;
      setTimeout(() => this.resize(), 20);
    }
    if (stateName === "DEMO_PLAYER") {
      this.selectDemoTrack(this.demoTrackIndex, false);
    }
    this.updateLobbyPlayback();
    this.syncUI();
  },

  selectStage(index) {
    this.unlockAudioOnFirstGesture();
    this.playSfx("click");
    this.selectedStageIndex = index;
    this.syncUI();
  },

  syncUI() {
    // Show only active screen overlay
    const screens = ["MENU", "PLAYING", "PAUSED", "GAME_OVER", "TRACK_COMPLETED", "HOW_TO_PLAY", "SETTINGS", "DEMO_PLAYER", "CUSTOMIZER", "COUNTDOWN", "LINER_NOTES"];
    for (const s of screens) {
      const el = document.getElementById(`screen-${s}`);
      if (el) el.style.display = (this.gameState === s) ? "flex" : "none";
    }

    // Sync Menu Screen
    const allGold = OFFICIAL_TRACKS.every((t) => (this.prefs.trackMasteryMedals[t.id] || 0) >= 3);
    const ptsEl = document.getElementById("menu-points-val");
    if (ptsEl) ptsEl.textContent = String(this.prefs.points);

    const exclPill = document.getElementById("menu-exclusives-pill");
    if (exclPill && typeof this.getUnlockedExclusivesCount === "function") {
      exclPill.textContent = `${this.getUnlockedExclusivesCount()}/${ARTIST_EXCLUSIVES_CATALOG.length} VAULT →`;
    }

    const lobby = LOBBY_TRACKS[this.prefs.lobbyThemeIndex % LOBBY_TRACKS.length];
    const activeRemix = this.getRemixMode();
    const lobbyTextEl = document.getElementById("menu-lobby-text");
    if (lobbyTextEl) {
      lobbyTextEl.textContent = `LOBBY: ${lobby.title.toUpperCase()} (${activeRemix.shortTag}) ↻`;
    }

    const titleMain = document.getElementById("menu-title-main");
    if (titleMain) titleMain.style.color = allGold ? "#FEF08A" : "#F8FAFC";

    const subBanner = document.getElementById("menu-sub-banner");
    if (subBanner) {
      subBanner.style.color = allGold ? "#FACC15" : "#4ADE80";
      subBanner.innerHTML = allGold
        ? `★ 100% ALBUM MASTERY • BEST: <span id="menu-best-score">${this.prefs.highScore}</span>`
        : `INTERACTIVE VISUAL ALBUM • BEST SCORE: <span id="menu-best-score">${this.prefs.highScore}</span>`;
    }

    const activeStage = OFFICIAL_TRACKS[this.selectedStageIndex] || OFFICIAL_TRACKS[0];

    // 1. Track Vinyl Selector Cards (#menu-track-deck-row) — 1:1 with Android MenuScreen.kt
    const trackDeckRow = document.getElementById("menu-track-deck-row");
    if (trackDeckRow) {
      trackDeckRow.innerHTML = OFFICIAL_TRACKS.map((track, idx) => {
        const isSelected = idx === this.selectedStageIndex;
        const stars = this.prefs.trackMasteryMedals[track.id] || 0;
        const starsStr = [1, 2, 3].map(s => `<span style="color:${s <= stars ? "#FACC15" : "#334155"};">★</span>`).join("");
        return `
          <div class="track-card ${isSelected ? "selected" : ""}" onclick="window.app.selectStage(${idx})">
            <div class="track-card-header">
              <span class="track-card-bpm">SONG 0${idx + 1} • ${track.bpm} BPM</span>
              <span class="track-card-stars">${starsStr}</span>
            </div>
            <div class="track-card-title">${track.title.toUpperCase()}</div>
          </div>
        `;
      }).join("");
    }

    // 2. 4-Mode Replayability Selector (#menu-remix-pills & #menu-remix-subtitle) — 1:1 with Android MenuScreen.kt
    const remixSubEl = document.getElementById("menu-remix-subtitle");
    if (remixSubEl) {
      remixSubEl.textContent = activeRemix.subtitle;
      remixSubEl.style.color = activeRemix.color;
    }

    const remixPillsEl = document.getElementById("menu-remix-pills");
    if (remixPillsEl) {
      remixPillsEl.innerHTML = TRACK_REMIX_MODES.map((mode) => {
        const isSelected = mode.id === activeRemix.id;
        return `
          <div class="remix-pill ${isSelected ? "selected" : ""}"
               style="border-color:${isSelected ? mode.color : "rgba(255,255,255,0.12)"};background:${isSelected ? "rgba(255,255,255,0.09)" : "#050911"};"
               onclick="window.app.setRemixMode('${mode.id}')">
            <div class="remix-pill-tag" style="color:${isSelected ? mode.color : "#94A3B8"};">${mode.shortTag}</div>
            <div class="remix-pill-id" style="color:${isSelected ? "#FFFFFF" : "#64748B"};">${mode.displayName}</div>
          </div>
        `;
      }).join("");
    }

    // 3. Console Card Border & Launch Button (#menu-launch-main & #btn-launch-stage)
    const consoleCard = document.getElementById("menu-console-card");
    if (consoleCard) {
      consoleCard.style.borderColor = allGold ? "#FACC15" : activeRemix.color;
    }

    const launchBtn = document.getElementById("btn-launch-stage");
    if (launchBtn) {
      launchBtn.style.borderColor = activeRemix.color;
    }

    const launchMain = document.getElementById("menu-launch-main");
    if (launchMain) {
      launchMain.innerHTML = `▶ &nbsp;PLAY ${activeStage.title.toUpperCase()} (${activeRemix.displayName})`;
    }

    const vaultCountEl = document.getElementById("menu-vault-count");
    if (vaultCountEl) {
      vaultCountEl.textContent = `${this.prefs.unlockedTracks.length}/${OFFICIAL_TRACKS.length} UNLOCKED`;
    }

    // Settings toggles
    const musicToggle = document.getElementById("settings-music-btn");
    if (musicToggle) {
      musicToggle.textContent = this.prefs.musicMuted ? "MUTED" : "ACTIVE";
      musicToggle.className = `toggle-pill ${this.prefs.musicMuted ? "off" : "on"}`;
    }
    const sfxToggle = document.getElementById("settings-sfx-btn");
    if (sfxToggle) {
      sfxToggle.textContent = this.prefs.sfxMuted ? "MUTED" : "ACTIVE";
      sfxToggle.className = `toggle-pill ${this.prefs.sfxMuted ? "off" : "on"}`;
    }

    // Game Over Screen
    if (this.gameState === "GAME_OVER") {
      const goScore = document.getElementById("gameover-score-val");
      if (goScore) goScore.textContent = String(this.score);
      const goBest = document.getElementById("gameover-best-val");
      if (goBest) goBest.textContent = `BEST RECORD: ${this.prefs.highScore}`;
      const reviveBtn = document.getElementById("gameover-revive-btn");
      if (reviveBtn) reviveBtn.style.display = this.reviveUsedThisRun ? "none" : "flex";
      const notesBtn = document.getElementById("gameover-notes-btn");
      if (notesBtn) {
        const canAfford = this.prefs.points >= 250;
        notesBtn.innerHTML = canAfford
          ? `♪ USE 250 NOTES TO REVIVE <span style="opacity:0.75;font-weight:700;">&nbsp;(YOU HAVE ${this.prefs.points})</span>`
          : `NEED 250 ♪ NOTES <span style="opacity:0.75;font-weight:700;">&nbsp;(YOU HAVE ${this.prefs.points})</span>`;
        notesBtn.style.opacity = canAfford ? "1" : "0.6";
      }
    }

    // VIP badge on menu
    const vipBadge = document.getElementById("menu-vip-badge");
    if (vipBadge) {
      vipBadge.textContent = this.prefs.vipUnlocked ? "• ★ VIP UNLOCKED" : "";
    }

    // Track Completed Screen (Showstopping Premiere Celebration)
    if (this.gameState === "TRACK_COMPLETED") {
      const tcTitle = document.getElementById("tc-track-title");
      if (tcTitle) tcTitle.textContent = `"${activeStage.title.toUpperCase()}" MASTERED!`;
      const tcMedal = document.getElementById("tc-medal-badge");
      if (tcMedal) {
        const grade = this.maxComboRun >= 8 ? "S+ MASTER" : (this.maxComboRun >= 5 ? "S RANK" : "A RANK");
        const label = this.earnedMedalTier === 3 ? "★★★ 24K GOLD VINYL MASTER" : this.earnedMedalTier === 2 ? "★★☆ PLATINUM RHYTHM CUT" : "★☆☆ STUDIO STAGE CLEAR";
        tcMedal.textContent = `${label} • ${grade}`;
      }
      const tcScore = document.getElementById("tc-score-val");
      if (tcScore) tcScore.textContent = String(this.score);
      const tcCombo = document.getElementById("tc-combo-val");
      if (tcCombo) tcCombo.textContent = `${this.maxComboRun}x`;
      const tcEarned = document.getElementById("tc-earned-val");
      if (tcEarned) {
        const totalEarned = Math.max(50, Math.floor(this.score / 8)) + 500;
        tcEarned.textContent = `+♪ ${totalEarned}`;
      }
      if (typeof this.getPrimarySpotlightExclusive === "function") {
        const excl = this.getPrimarySpotlightExclusive(this.selectedStageIndex);
        if (excl) {
          const bEl = document.getElementById("tc-excl-badge");
          const tEl = document.getElementById("tc-excl-title");
          const bodyEl = document.getElementById("tc-excl-body");
          const qEl = document.getElementById("tc-excl-quote");
          if (bEl) bEl.textContent = `★ ARTIST EXCLUSIVE UNLOCKED • ${excl.categoryBadge}`;
          if (tEl) tEl.textContent = excl.title;
          if (bodyEl) bodyEl.textContent = excl.unlockedBody;
          if (qEl) qEl.textContent = excl.unlockedQuote;
        }
      }
    }

    if (this.gameState === "CUSTOMIZER") {
      this.syncCustomizerUI();
    }
    if (this.gameState === "DEMO_PLAYER") {
      this.syncDemoPlayerUI();
    }
  },

  toggleMusicMute() {
    this.prefs.musicMuted = !this.prefs.musicMuted;
    this.trackAudio.muted = this.prefs.musicMuted;
    this.demoAudio.muted = this.prefs.musicMuted;
    this.savePrefs();
    this.updateLobbyPlayback();
    this.syncUI();
  },

  toggleSfxMute() {
    this.prefs.sfxMuted = !this.prefs.sfxMuted;
    this.savePrefs();
    this.playSfx("click");
    this.syncUI();
  },

  // ==========================================================================
  // STUDIO CUSTOMIZER & ALBUM VAULT
  // ==========================================================================
  setCustomizerTab(tabIdx) {
    this.playSfx("whoosh_modal", 0.72);
    this.customizerTab = tabIdx;
    if (tabIdx >= 0) setTimeout(() => this.resize(), 20);
    this.syncCustomizerUI();
  },

  buyOrEquipItem(itemId, price, category) {
    const isUnlocked = this.prefs.unlockedItems.includes(itemId);
    if (isUnlocked) {
      this.playSfx("click");
      if (category === "skin") this.prefs.selectedSkin = itemId;
      else if (category === "trail") this.prefs.selectedTrail = itemId;
      else if (category === "theme") this.prefs.selectedTheme = itemId;
      this.savePrefs();
      this.syncCustomizerUI();
    } else if (this.prefs.points >= price) {
      this.playSfx("powerup");
      this.prefs.points -= price;
      this.prefs.unlockedItems.push(itemId);
      if (category === "skin") this.prefs.selectedSkin = itemId;
      else if (category === "trail") this.prefs.selectedTrail = itemId;
      else if (category === "theme") this.prefs.selectedTheme = itemId;
      this.savePrefs();
      this.syncCustomizerUI();
    }
  },

  syncCustomizerUI() {
    const ptsEl = document.getElementById("cust-points-val");
    if (ptsEl) ptsEl.textContent = `♪ ${this.prefs.points}`;

    // Tabs (-1 = ★ SPECIALS, 0 = CORES, 1 = TRAILS, 2 = THEMES)
    for (const i of [-1, 0, 1, 2]) {
      const tBtn = document.getElementById(`cust-tab-${i}`);
      if (tBtn) tBtn.className = `cust-tab ${this.customizerTab === i ? "active" : ""}`;
    }

    const specialsWrap = document.getElementById("cust-specials-wrap");
    const cosmeticsWrap = document.getElementById("cust-cosmetics-wrap");
    if (this.customizerTab === -1) {
      if (specialsWrap) specialsWrap.style.display = "block";
      if (cosmeticsWrap) cosmeticsWrap.style.display = "none";
      if (typeof this.renderArtistSpecialsVault === "function") {
        this.renderArtistSpecialsVault();
      }
    } else {
      if (specialsWrap) specialsWrap.style.display = "none";
      if (cosmeticsWrap) cosmeticsWrap.style.display = "block";
    }

    // Unlocked Track Vault Cards
    const vaultListEl = document.getElementById("cust-vault-tracks");
    if (vaultListEl) {
      vaultListEl.innerHTML = OFFICIAL_TRACKS.map((t, idx) => {
        const isUnlocked = this.prefs.unlockedTracks.includes(t.id);
        const bestProg = Math.max(isUnlocked ? 1 : 0, this.prefs.trackBestProgress[t.id] || 0);
        const pct = Math.round(bestProg * 100);
        return `
          <div class="vault-track-row ${isUnlocked ? "unlocked" : "locked"}" onclick="${isUnlocked ? `window.app.openTrackInDemo(${idx})` : ""}">
            <div class="vault-track-icon">${isUnlocked ? "▶" : "🔒"}</div>
            <div style="flex:1;min-width:0;">
              <div class="vault-track-name">${t.title}</div>
              <div class="vault-track-sub" style="color:${isUnlocked ? "#4ADE80" : "#94A3B8"};">
                ${isUnlocked ? "UNLOCKED • TAP TO LISTEN FULL TRACK" : `Complete Stage (${pct}% Revealed) to unlock`}
              </div>
            </div>
            <div class="vault-track-dur">${t.durationLabel}</div>
          </div>
        `;
      }).join("");
    }

    const itemsByTab = [
      [
        { id: "skin_cyan", cat: "skin", name: "Cyber Cyan Core", sub: "Standard Resonance Core", price: 0, col: "#38BDF8" },
        { id: "skin_gold", cat: "skin", name: "Solar Gold Core", sub: "24K Harmonic Sunburst", price: 200, col: "#FACC15" },
        { id: "skin_prism", cat: "skin", name: "Prism Nova Core", sub: "Magenta Crystal Matrix", price: 450, col: "#EC4899" },
        { id: "skin_emerald", cat: "skin", name: "Aurora Emerald Core", sub: "Harmonic Bio-Resonance", price: 350, col: "#10B981" },
        { id: "skin_blood", cat: "skin", name: "Eclipse Void Core", sub: "Dark Singularity Aura", price: 600, col: "#F43F5E" }
      ],
      [
        { id: "trail_cyan", cat: "trail", name: "Aether Ribbon", sub: "Cyan & Indigo Silk Trail", price: 0, col: "#38BDF8" },
        { id: "trail_gold", cat: "trail", name: "Solar Flare Wake", sub: "Liquid Gold Stardust", price: 250, col: "#FACC15" },
        { id: "trail_emerald", cat: "trail", name: "Aurora Emerald", sub: "Bioluminescent Synth Wave", price: 400, col: "#34D399" },
        { id: "trail_prism", cat: "trail", name: "Prismatic Mirage", sub: "Magenta & Violet Light Cascade", price: 450, col: "#C084FC" },
        { id: "trail_laser", cat: "trail", name: "Laser Stream", sub: "High-Energy Electric Beam", price: 550, col: "#22D3EE" }
      ],
      [
        { id: "theme_nebula", cat: "theme", name: "Deep Space Nebula", sub: "Signature No More Illusions Stage", price: 0, col: "#6366F1" },
        { id: "theme_sunset", cat: "theme", name: "Sunset Horizon", sub: "Crimson & Gold Synthwave", price: 300, col: "#F43F5E" },
        { id: "theme_matrix", cat: "theme", name: "Cyber Grid", sub: "Emerald Quantum Matrix", price: 500, col: "#10B981" },
        { id: "theme_blood", cat: "theme", name: "Blood Moon Eclipse", sub: "Crimson Stasis Chamber", price: 550, col: "#BE123C" },
        { id: "theme_gold", cat: "theme", name: "24K Gold Master Stage", sub: "All-Gold Concert Hall", price: 750, col: "#EAB308" }
      ]
    ];

    const activeItems = itemsByTab[this.customizerTab] || itemsByTab[0];
    const itemsContainer = document.getElementById("cust-items-list");
    if (itemsContainer) {
      itemsContainer.innerHTML = activeItems.map((it) => {
        const isUnlocked = this.prefs.unlockedItems.includes(it.id);
        const isSelected = (it.cat === "skin" && this.prefs.selectedSkin === it.id) ||
                           (it.cat === "trail" && this.prefs.selectedTrail === it.id) ||
                           (it.cat === "theme" && this.prefs.selectedTheme === it.id);
        const canAfford = this.prefs.points >= it.price;
        const btnLabel = isSelected ? "EQUIPPED" : isUnlocked ? "EQUIP" : `♪ ${it.price}`;
        const btnClass = isSelected ? "cust-btn equipped" : isUnlocked ? "cust-btn equip" : (canAfford ? "cust-btn buy" : "cust-btn locked");
        return `
          <div class="cust-item-row ${isSelected ? "selected" : ""}">
            <div class="cust-swatch" style="background:${it.col};box-shadow:0 0 12px ${it.col};"></div>
            <div style="flex:1;">
              <div class="cust-item-name">${it.name}</div>
              <div class="cust-item-sub">${it.sub}</div>
            </div>
            <button class="${btnClass}" onclick="window.app.buyOrEquipItem('${it.id}', ${it.price}, '${it.cat}')">${btnLabel}</button>
          </div>
        `;
      }).join("");
    }
  },

  // ==========================================================================
  // ALBUM VAULT & STUDIO PLAYER (MusicDemoScreen.kt 1:1 Port)
  // ==========================================================================
  openTrackInDemo(idx) {
    this.demoTrackIndex = idx;
    this.gameState = "DEMO_PLAYER";
    this.lobbyAudio.pause();
    this.selectDemoTrack(idx, true);
    this.syncUI();
  },

  selectDemoTrack(idx, autoPlay = true) {
    this.playSfx("click");
    this.demoTrackIndex = idx;
    const track = OFFICIAL_TRACKS[idx] || OFFICIAL_TRACKS[0];
    const isUnlocked = this.prefs.unlockedTracks.includes(track.id);
    this.demoAudio.pause();
    if (isUnlocked) {
      this.demoAudio.src = track.src;
      this.demoAudio.currentTime = 0;
      if (autoPlay && !this.prefs.musicMuted) {
        this.demoAudio.play().catch(() => {});
      }
    }
    this.syncDemoPlayerUI();
  },

  toggleDemoPlayPause() {
    const track = OFFICIAL_TRACKS[this.demoTrackIndex] || OFFICIAL_TRACKS[0];
    if (!this.prefs.unlockedTracks.includes(track.id)) {
      this.startStage(this.demoTrackIndex);
      return;
    }
    this.playSfx("click");
    if (this.demoAudio.paused) {
      if (!this.demoAudio.src.endsWith(track.src)) this.demoAudio.src = track.src;
      this.demoAudio.play().catch(() => {});
    } else {
      this.demoAudio.pause();
    }
    this.syncDemoPlayerUI();
  },

  seekDemoBy(deltaSec) {
    const track = OFFICIAL_TRACKS[this.demoTrackIndex] || OFFICIAL_TRACKS[0];
    if (!this.prefs.unlockedTracks.includes(track.id)) return;
    this.demoAudio.currentTime = Math.max(0, Math.min(this.demoAudio.duration || track.durationSec, this.demoAudio.currentTime + deltaSec));
    this.updateDemoPlayerProgress();
  },

  toggleDemoLoop() {
    this.playSfx("click");
    this.demoLoopMode = !this.demoLoopMode;
    this.syncDemoPlayerUI();
  },

  updateDemoPlayerProgress() {
    const track = OFFICIAL_TRACKS[this.demoTrackIndex] || OFFICIAL_TRACKS[0];
    const cur = this.demoAudio.currentTime || 0;
    const dur = this.demoAudio.duration || track.durationSec || 179;
    const pct = Math.max(0, Math.min(100, (cur / dur) * 100));
    const fill = document.getElementById("demo-seek-fill");
    if (fill) fill.style.width = `${pct}%`;

    const fmt = (s) => {
      const m = Math.floor(s / 60);
      const sec = String(Math.floor(s % 60)).padStart(2, "0");
      return `${m}:${sec}`;
    };
    const curEl = document.getElementById("demo-time-cur");
    const durEl = document.getElementById("demo-time-dur");
    if (curEl) curEl.textContent = fmt(cur);
    if (durEl) durEl.textContent = fmt(dur);
  },

  syncDemoPlayerUI() {
    const track = OFFICIAL_TRACKS[this.demoTrackIndex] || OFFICIAL_TRACKS[0];
    const isUnlocked = this.prefs.unlockedTracks.includes(track.id);
    const bestProg = Math.max(isUnlocked ? 1 : 0, this.prefs.trackBestProgress[track.id] || 0);

    const titleEl = document.getElementById("demo-active-title");
    if (titleEl) titleEl.textContent = track.title;

    const statusEl = document.getElementById("demo-active-status");
    if (statusEl) {
      statusEl.textContent = isUnlocked ? "★ UNLOCKED IN ALBUM VAULT" : `🔒 LOCKED (${Math.round(bestProg * 100)}% REVEALED)`;
      statusEl.style.color = isUnlocked ? "#4ADE80" : "#FACC15";
    }

    const playBtn = document.getElementById("demo-play-btn");
    if (playBtn) {
      if (!isUnlocked) {
        playBtn.textContent = "PLAY STAGE TO UNLOCK";
        playBtn.style.fontSize = "11px";
      } else {
        playBtn.textContent = this.demoAudio.paused ? "▶" : "❚❚";
        playBtn.style.fontSize = "22px";
      }
    }

    const loopBtn = document.getElementById("demo-loop-btn");
    if (loopBtn) {
      loopBtn.textContent = this.demoLoopMode ? "LOOP: ON" : "LOOP: OFF";
      loopBtn.style.color = this.demoLoopMode ? "#FACC15" : "#94A3B8";
    }

    const listEl = document.getElementById("demo-track-list");
    if (listEl) {
      listEl.innerHTML = OFFICIAL_TRACKS.map((t, idx) => {
        const u = this.prefs.unlockedTracks.includes(t.id);
        const sel = idx === this.demoTrackIndex;
        const p = Math.round(Math.max(u ? 1 : 0, this.prefs.trackBestProgress[t.id] || 0) * 100);
        return `
          <div class="vault-track-row ${sel ? "selected" : ""}" onclick="window.app.selectDemoTrack(${idx}, true)">
            <div class="vault-track-icon" style="color:${u ? "#4ADE80" : "#FACC15"};">${u ? "▶" : "🔒"}</div>
            <div style="flex:1;">
              <div class="vault-track-name">${t.title}</div>
              <div class="vault-track-sub">${u ? `${t.subtitle} • ${t.bpm} BPM` : `${p}% Revealed — Survive stage to unlock`}</div>
            </div>
            <div class="vault-track-dur">${t.durationLabel}</div>
          </div>
        `;
      }).join("");
    }
    this.updateDemoPlayerProgress();
  }
});
