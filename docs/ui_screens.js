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
      track_01: "Here is where you can put your personal studio story, production notes, or behind-the-scenes memory for your fans about 'Confide'!",
      track_02: "Here is where you can put your personal studio story, production notes, or behind-the-scenes memory for your fans about 'Number One'!"
    };

    const lyrics = {
      track_01: "Here is where you can put the official lyrics, chorus breakdown, or personal liner notes for 'Confide' for your fans!",
      track_02: "Here is where you can put the official lyrics, chorus breakdown, or personal liner notes for 'Number One' for your fans!"
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

  toggleRewardsPeek() {
    this.playSfx("click");
    this.showRewardsPeek = !this.showRewardsPeek;
    if (this.showRewardsPeek) {
      this.showTrackDropdown = false;
      this.showModeDropdown = false;
    }
    this.syncUI();
  },

  toggleTrackDropdown() {
    this.playSfx("click");
    this.showTrackDropdown = !this.showTrackDropdown;
    if (this.showTrackDropdown) {
      this.showModeDropdown = false;
      this.showRewardsPeek = false;
    }
    this.syncUI();
  },

  toggleModeDropdown() {
    this.playSfx("click");
    this.showModeDropdown = !this.showModeDropdown;
    if (this.showModeDropdown) {
      this.showTrackDropdown = false;
      this.showRewardsPeek = false;
    }
    this.syncUI();
  },

  toggleCustomizerCatDropdown() {
    this.playSfx("click");
    this.showCustomizerCatDropdown = !this.showCustomizerCatDropdown;
    this.syncCustomizerUI();
  },

  toggleVictoryExclusivePopup(show) {
    this.playSfx(show ? "powerup" : "click", 0.78);
    const popupEl = document.getElementById("tc-excl-popup");
    if (popupEl) {
      popupEl.style.display = show ? "block" : "none";
    }
  },

  selectStage(index) {
    this.unlockAudioOnFirstGesture();
    this.playSfx("click");
    this.selectedStageIndex = index;
    this.showTrackDropdown = false;
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
      exclPill.textContent = `${this.getUnlockedExclusivesCount()}/${ARTIST_EXCLUSIVES_CATALOG.length} UNLOCKED →`;
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
        : `BEST SCORE: <span id="menu-best-score">${this.prefs.highScore}</span>`;
    }

    const activeStage = OFFICIAL_TRACKS[this.selectedStageIndex] || OFFICIAL_TRACKS[0];
    const unlockedExclCount = typeof this.getUnlockedExclusivesCount === "function" ? this.getUnlockedExclusivesCount() : 0;
    const totalExclCount = typeof ARTIST_EXCLUSIVES_CATALOG !== "undefined" ? ARTIST_EXCLUSIVES_CATALOG.length : 7;

    // 0. Frameless Curiosity Peek Header (#menu-rewards-dropdown-wrap)
    const rewardsDropWrap = document.getElementById("menu-rewards-dropdown-wrap");
    if (rewardsDropWrap) {
      rewardsDropWrap.innerHTML = `
        <div style="padding:2px 6px;">
          <div onclick="window.app.toggleRewardsPeek()"
               style="padding:4px 0;display:flex;align-items:center;justify-content:center;gap:8px;cursor:pointer;">
            <span style="font-size:10px;font-weight:900;color:#EAB308;letter-spacing:1.1px;">✦ WHAT CAN I UNLOCK? (${unlockedExclCount}/${totalExclCount} SECRETS) ${this.showRewardsPeek ? "▴" : "▾"}</span>
          </div>
          <div style="height:1px;background:linear-gradient(90deg,transparent,rgba(234,179,8,0.6),transparent);"></div>
          ${this.showRewardsPeek ? `
            <div style="padding:8px 8px 6px;display:flex;flex-direction:column;gap:5px;text-align:left;">
              <div style="font-size:10px;font-weight:700;color:#4ADE80;">💿 Beat Any Song → Unlock Full Album Track &amp; Artist Fan Note</div>
              <div style="font-size:10px;font-weight:700;color:#38BDF8;">🔥 Hit 8x Beat Streak → Unlock Backstage Studio Secret</div>
              <div style="font-size:10px;font-weight:900;color:#F43F5E;">🏆 15,000+ on ∞ Endless Loop → Secret Personal Message from Artist!</div>
              <div onclick="window.app.openScreen('CUSTOMIZER', -1)"
                   style="margin-top:3px;padding:4px;text-align:center;font-size:9.5px;font-weight:900;color:#EAB308;letter-spacing:0.8px;cursor:pointer;">
                ★ TAP TO OPEN ALBUM VAULT &amp; FAN NOTES →
              </div>
            </div>
          ` : ""}
        </div>
      `;
    }

    // 1. Frameless Vinyl Stage Song Selector (#menu-track-deck-row)
    const trackDeckRow = document.getElementById("menu-track-deck-row");
    if (trackDeckRow) {
      const selStars = this.prefs.trackMasteryMedals[activeStage.id] || 0;
      const selStarsStr = [1, 2, 3].map(s => s <= selStars ? "★" : "☆").join("");
      const selUnlocked = this.prefs.unlockedTracks.includes(activeStage.id);
      trackDeckRow.innerHTML = `
        <div style="padding:2px 4px;">
          <div onclick="window.app.toggleTrackDropdown()"
               style="padding:6px 4px;display:flex;align-items:center;justify-content:space-between;cursor:pointer;">
            <div style="position:relative;width:44px;height:44px;border-radius:50%;overflow:hidden;border:1.8px solid ${selUnlocked ? "#4ADE80" : "#EAB308"};flex-shrink:0;margin-right:12px;">
              <img src="assets/nmi_logo.png" alt="Vinyl" style="width:100%;height:100%;object-fit:cover;filter:${selUnlocked ? "none" : "blur(3px) brightness(0.42)"};" />
              ${!selUnlocked ? `<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;font-size:14px;">🔒</div>` : ""}
            </div>
            <div style="text-align:left;min-width:0;flex:1;">
              <div style="font-size:8.5px;font-weight:800;color:#EAB308;letter-spacing:1.1px;">STAGE TRACK • TAP TO PREVIEW PRIZE &amp; SWITCH</div>
              <div style="font-size:15px;font-weight:900;color:#FAF8F5;margin-top:1px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
                ${selUnlocked ? `TRACK 0${this.selectedStageIndex + 1}: "${activeStage.title.toUpperCase()}"` : `TRACK 0${this.selectedStageIndex + 1} • 🔒 TAP TO SEE PRIZE`}
              </div>
            </div>
            <div style="display:flex;align-items:center;gap:6px;margin-left:8px;">
              <span style="font-size:11px;font-weight:900;color:#FACC15;">${selStarsStr}</span>
              <span style="font-size:12px;font-weight:900;color:#EAB308;">${this.showTrackDropdown ? "▴" : "▾"}</span>
            </div>
          </div>
          <div style="height:1.2px;background:linear-gradient(90deg,transparent,rgba(234,179,8,0.75),rgba(56,189,248,0.55),transparent);"></div>
          ${this.showTrackDropdown ? `
            <div style="padding:8px 2px 6px;display:flex;flex-direction:column;gap:8px;">
              ${OFFICIAL_TRACKS.map((track, idx) => {
                const isSel = idx === this.selectedStageIndex;
                const isTrkUnlocked = this.prefs.unlockedTracks.includes(track.id);
                const stars = this.prefs.trackMasteryMedals[track.id] || 0;
                const starsStr = [1, 2, 3].map(s => s <= stars ? "★" : "☆").join("");
                const effBpm = Math.round(track.bpm * (activeRemix.speed || 1.0));
                return `
                  <div onclick="window.app.selectStage(${idx})"
                       style="padding:6px 4px;display:flex;align-items:center;gap:12px;cursor:pointer;text-align:left;border-bottom:1px solid rgba(255,255,255,0.08);">
                    <div style="position:relative;width:50px;height:50px;border-radius:50%;overflow:hidden;border:2px solid ${isTrkUnlocked ? "#4ADE80" : "#EAB308"};flex-shrink:0;">
                      <img src="assets/nmi_logo.png" alt="Cover" style="width:100%;height:100%;object-fit:cover;filter:${isTrkUnlocked ? "none" : "blur(4px) brightness(0.38)"};" />
                      ${!isTrkUnlocked ? `<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;font-size:17px;">🔒</div>` : ""}
                    </div>
                    <div style="flex:1;min-width:0;">
                      <div style="display:flex;align-items:center;justify-content:space-between;">
                        <span style="font-size:12.5px;font-weight:900;color:${isSel ? "#EAB308" : "#FAF8F5"};">TRACK 0${idx + 1}: "${track.title.toUpperCase()}"</span>
                        <span style="font-size:10px;font-weight:900;color:#FACC15;">${starsStr}</span>
                      </div>
                      <div style="font-size:9.5px;font-weight:800;color:${isTrkUnlocked ? "#4ADE80" : "#38BDF8"};margin-top:2px;">
                        ${isTrkUnlocked ? "★ UNLOCKED IN YOUR ALBUM VAULT!" : "🎁 WIN: Full Song in Album + Cover Art + Fan Note!"}
                      </div>
                      <div style="font-size:8.5px;font-weight:700;color:#94A3B8;margin-top:1px;">
                        ${effBpm} BPM • ${isSel ? "✓ READY TO LAUNCH" : "TAP TO SELECT"}
                      </div>
                    </div>
                  </div>
                `;
              }).join("")}
            </div>
          ` : ""}
        </div>
      `;
    }

    // 2. Frameless Speed & Mode Selector (#menu-remix-pills)
    const remixPillsEl = document.getElementById("menu-remix-pills");
    if (remixPillsEl) {
      const modeHooks = {
        ORIGINAL: "1.0x Pts • Beat 1x to unlock Album Track & Artist Fan Note",
        OVERCLOCK: "1.5x Pts • Faster notes + unlocks ★3 Gold Medal",
        SLOWED: "0.5x Pts • Training speed (Play 2x to unlock Album Art)",
        ENDLESS: "🏆 Speeds up +8%/loop! Hit 15,000+ pts for Artist's Secret Message!"
      };
      remixPillsEl.innerHTML = `
        <div style="padding:2px 4px;">
          <div onclick="window.app.toggleModeDropdown()"
               style="padding:6px 4px;display:flex;align-items:center;justify-content:space-between;cursor:pointer;">
            <div style="text-align:left;min-width:0;flex:1;">
              <div style="font-size:8.5px;font-weight:800;color:#94A3B8;letter-spacing:1.1px;">⚡ SPEED &amp; DIFFICULTY • TAP TO SWITCH</div>
              <div style="font-size:13.5px;font-weight:900;color:${activeRemix.color};margin-top:1px;">
                ${activeRemix.shortTag} ${activeRemix.displayName} • ${activeRemix.subtitle}
              </div>
            </div>
            <span style="font-size:12px;font-weight:900;color:${activeRemix.color};margin-left:8px;">${this.showModeDropdown ? "▴" : "▾"}</span>
          </div>
          <div style="height:1.2px;background:linear-gradient(90deg,transparent,${activeRemix.color},transparent);"></div>
          ${this.showModeDropdown ? `
            <div style="padding:6px 2px 6px;display:flex;flex-direction:column;gap:6px;">
              ${TRACK_REMIX_MODES.map((mode) => {
                const isSel = mode.id === activeRemix.id;
                const hook = modeHooks[mode.id] || mode.subtitle;
                return `
                  <div onclick="window.app.setRemixMode('${mode.id}')"
                       style="padding:6px 4px;text-align:left;cursor:pointer;border-bottom:1px solid ${isSel ? mode.color : "rgba(255,255,255,0.08)"};">
                    <div style="display:flex;align-items:center;justify-content:space-between;">
                      <span style="font-size:11.5px;font-weight:900;color:${mode.color};">${mode.shortTag} ${mode.displayName}</span>
                      <span style="font-size:9px;font-weight:900;color:#FAF8F5;">${isSel ? "✓ ACTIVE" : "SELECT"}</span>
                    </div>
                    <div style="font-size:9.5px;font-weight:600;color:#CBD5E1;margin-top:2px;">${hook}</div>
                  </div>
                `;
              }).join("")}
            </div>
          ` : ""}
        </div>
      `;
    }

    // 3. Custom Vinyl-Groove Stage Launch Emblem (#menu-launch-main & #menu-launch-sub)
    const launchBtn = document.getElementById("btn-launch-stage");
    if (launchBtn) {
      launchBtn.style.borderTopColor = activeRemix.color;
      launchBtn.style.borderBottomColor = activeRemix.color;
    }

    const launchMain = document.getElementById("menu-launch-main");
    if (launchMain) {
      launchMain.innerHTML = `▶ &nbsp;PLAY TRACK ${this.selectedStageIndex + 1} &nbsp;◀`;
    }
    const launchSub = document.getElementById("menu-launch-sub");
    if (launchSub) {
      launchSub.textContent = `${activeRemix.displayName} • ${activeRemix.subtitle}`;
      launchSub.style.color = activeRemix.color;
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

    // Track Completed Screen (Visual-First Premiere Celebration)
    if (this.gameState === "TRACK_COMPLETED") {
      const tcTitle = document.getElementById("tc-track-title");
      if (tcTitle) tcTitle.textContent = `TRACK ${this.selectedStageIndex + 1}: "${activeStage.title.toUpperCase()}"`;
      const popupEl = document.getElementById("tc-excl-popup");
      if (popupEl && this._lastCompletedPopupInit !== this.survivalSeconds) {
        this._lastCompletedPopupInit = this.survivalSeconds;
        popupEl.style.display = "none";
      }
      const tcScore = document.getElementById("tc-score-val");
      if (tcScore) tcScore.textContent = String(this.score);
      const tcCombo = document.getElementById("tc-combo-val");
      if (tcCombo) tcCombo.textContent = `${this.maxComboRun}x`;
      const tcEarned = document.getElementById("tc-earned-val");
      if (tcEarned) {
        const totalEarned = this.lastRunEarnedNotes || (Math.max(50, Math.floor(this.score / 8)) + 500);
        tcEarned.textContent = `+♪ ${totalEarned}`;
      }
      if (typeof this.getPrimarySpotlightExclusive === "function") {
        const excl = this.getPrimarySpotlightExclusive(this.selectedStageIndex);
        if (excl) {
          const bEl = document.getElementById("tc-excl-badge");
          const tEl = document.getElementById("tc-excl-title");
          const bodyEl = document.getElementById("tc-excl-body");
          const qEl = document.getElementById("tc-excl-quote");
          if (bEl) bEl.textContent = `★ VAULT EXCLUSIVE UNLOCKED • ${excl.categoryBadge}`;
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
    this.showCustomizerCatDropdown = false;
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

    const catOptions = [
      { id: -1, label: "★ ARTIST VAULT & MYSTERY ENVELOPES", col: "#FACC15" },
      { id: 0, label: "🛸 HERO AVATAR CORES (5 SKINS)", col: "#38BDF8" },
      { id: 1, label: "✨ RHYTHM SILK TRAILS (5 WAKE FX)", col: "#4ADE80" },
      { id: 2, label: "🌌 STAGE NEBULA PALETTES (5 THEMES)", col: "#C084FC" }
    ];
    const activeCat = catOptions.find(c => c.id === this.customizerTab) || catOptions[0];
    const catWrap = document.getElementById("cust-category-dropdown-wrap");
    if (catWrap) {
      catWrap.innerHTML = `
        <div style="padding:2px 0;">
          <div onclick="window.app.toggleCustomizerCatDropdown()"
               style="padding:8px 4px;display:flex;align-items:center;justify-content:space-between;cursor:pointer;">
            <div style="text-align:left;min-width:0;flex:1;">
              <div style="font-size:8.5px;font-weight:800;color:#94A3B8;letter-spacing:1.1px;">🎨 SELECT CATEGORY • TAP TO SWITCH VIEW</div>
              <div style="font-size:13.5px;font-weight:900;color:${activeCat.col};margin-top:2px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
                ${activeCat.label}
              </div>
            </div>
            <span style="font-size:11px;font-weight:900;color:${activeCat.col};margin-left:8px;white-space:nowrap;">
              ${this.showCustomizerCatDropdown ? "CLOSE ▴" : "SWITCH ▾"}
            </span>
          </div>
          <div style="height:1.4px;background:linear-gradient(90deg,transparent,${activeCat.col},transparent);"></div>
          ${this.showCustomizerCatDropdown ? `
            <div style="padding:8px 2px 6px;display:flex;flex-direction:column;gap:6px;">
              ${catOptions.map(c => {
                const isSel = c.id === this.customizerTab;
                return `
                  <div onclick="window.app.setCustomizerTab(${c.id})"
                       style="padding:8px 4px;border-bottom:1px solid ${isSel ? c.col : "rgba(255,255,255,0.10)"};display:flex;align-items:center;justify-content:space-between;cursor:pointer;">
                    <span style="font-size:11.5px;font-weight:900;color:${isSel ? c.col : "#FAF8F5"};">${c.label}</span>
                    <span style="font-size:9.5px;font-weight:900;color:${isSel ? c.col : "#94A3B8"};">${isSel ? "✓ ACTIVE" : "OPEN →"}</span>
                  </div>
                `;
              }).join("")}
            </div>
          ` : ""}
        </div>
      `;
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
