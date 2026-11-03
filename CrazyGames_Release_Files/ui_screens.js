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
    const totalDur = Math.max(1, this.activeTrackDurationSec || 179);
    const elapsed = this.survivalSeconds || this.trackElapsedSec || 0;
    const progress = Math.max(0, Math.min(1, elapsed / totalDur));

    const progFill = document.getElementById("hud-top-progress-fill");
    if (progFill) progFill.style.width = `${(progress * 100).toFixed(1)}%`;

    // Milestone Stars & Crowns (Magic Tiles 3 Style — Check both standard and ms IDs)
    const s1 = document.getElementById("hud-star-1") || document.getElementById("hud-ms-star-1");
    const s2 = document.getElementById("hud-star-2") || document.getElementById("hud-ms-star-2");
    const s3 = document.getElementById("hud-star-3") || document.getElementById("hud-ms-star-3");
    const c1 = document.getElementById("hud-crown-1") || document.getElementById("hud-ms-crown-1");
    const c2 = document.getElementById("hud-crown-2") || document.getElementById("hud-ms-crown-2");
    const c3 = document.getElementById("hud-crown-3") || document.getElementById("hud-ms-crown-3");
    if (s1) s1.classList.toggle("unlocked", progress >= 0.166);
    if (s2) s2.classList.toggle("unlocked", progress >= 0.333);
    if (s3) s3.classList.toggle("unlocked", progress >= 0.500);
    if (c1) c1.classList.toggle("unlocked", progress >= 0.666);
    if (c2) c2.classList.toggle("unlocked", progress >= 0.833);
    if (c3) c3.classList.toggle("unlocked", progress >= 0.980);

    // Endless Mode Loop Counter Pill (Magic Tiles Accelerating Style)
    const endlessPill = document.getElementById("hud-endless-crown-pill");
    const endlessVal = document.getElementById("hud-endless-crown-val") || document.getElementById("hud-endless-loop-val");
    if (endlessPill) {
      const isEndless = this.getRemixMode().id === "ENDLESS" || this.isEndlessMode;
      const loopNum = (this.endlessLoopCount || 0) + 1;
      const curRate = (this.trackAudio && this.trackAudio.playbackRate) ? this.trackAudio.playbackRate : 1.0;
      const speedPct = Math.round((curRate - 1.0) * 100);
      if (isEndless) {
        endlessPill.style.display = "inline-flex";
        if (endlessVal) endlessVal.textContent = `LOOP ${loopNum} • +${speedPct}% SPEED`;
      } else {
        endlessPill.style.display = "none";
      }
    }

    // Band Hero 5-Star Overdrive Meter & 24K Overdrive Trigger Button
    const odProg = this.overdriveProgress || 0;
    const odFilledCount = Math.floor(odProg * 5);
    for (let st = 1; st <= 5; st++) {
      const starEl = document.getElementById(`od-star-${st}`);
      if (starEl) {
        starEl.classList.toggle("charged", st <= odFilledCount || this.is24KOverdrive);
      }
    }
    const odFillBar = document.getElementById("hud-od-fill-bar");
    if (odFillBar) {
      odFillBar.style.width = `${(odProg * 100).toFixed(1)}%`;
    }
    const odBtn = document.getElementById("btn-24k-overdrive");
    const odBtnLabel = document.getElementById("od-btn-label");
    if (odBtn) {
      if (this.is24KOverdrive) {
        odBtn.style.display = "inline-flex";
        odBtn.classList.add("active-rush");
        if (odBtnLabel) odBtnLabel.textContent = `24K ACTIVE • ${(this.overdriveTimer || 0).toFixed(1)}s`;
      } else if (odProg >= 0.60) {
        odBtn.style.display = "inline-flex";
        odBtn.classList.remove("active-rush");
        if (odBtnLabel) odBtnLabel.textContent = "24K OVERDRIVE";
      } else {
        odBtn.style.display = "none";
        odBtn.classList.remove("active-rush");
      }
    }

    // Center Big Score Display
    const scoreEl = document.getElementById("hud-score-val");
    if (scoreEl) scoreEl.textContent = Number(this.score || 0).toLocaleString();

    // Multiplier Row (× 1 ... × 8)
    const multVal = document.getElementById("hud-combo-mult-val");
    const multRow = document.getElementById("hud-combo-mult-row");
    const curCombo = this.combo || 1;
    if (multVal) {
      const baseMult = Math.max(1, Math.min(8, Math.floor(curCombo / 4) + 1));
      const mult = this.is24KOverdrive ? Math.min(8, baseMult * 4) : baseMult;
      multVal.textContent = String(mult);
      if (multRow) {
        multRow.style.opacity = curCombo > 1 || this.is24KOverdrive ? "1" : "0.45";
      }
    }

    const badgesEl = document.getElementById("hud-powerup-badges");
    if (badgesEl) {
      let html = "";
      if (this.shieldTimer > 0) html += `<span class="pu-badge" style="background:rgba(56,189,248,0.2);border:1px solid #38BDF8;color:#38BDF8;padding:2px 6px;border-radius:6px;font-size:9px;font-weight:900;">SHIELD</span>`;
      if (this.is24KOverdrive) html += `<span class="pu-badge" style="background:rgba(250,204,21,0.25);border:1px solid #FACC15;color:#FACC15;padding:2px 6px;border-radius:6px;font-size:9px;font-weight:900;">24K OVERDRIVE</span>`;
      else if (this.electricSurgeTimer > 0 || this.overdriveTimer > 0) html += `<span class="pu-badge" style="background:rgba(250,204,21,0.2);border:1px solid #FACC15;color:#FACC15;padding:2px 6px;border-radius:6px;font-size:9px;font-weight:900;">4X MULT</span>`;
      if (this.prismBloomTimer > 0) html += `<span class="pu-badge" style="background:rgba(74,222,128,0.2);border:1px solid #4ADE80;color:#4ADE80;padding:2px 6px;border-radius:6px;font-size:9px;font-weight:900;">PRISM</span>`;
      if (this.slowMoTimer > 0) html += `<span class="pu-badge" style="background:rgba(192,132,252,0.2);border:1px solid #C084FC;color:#C084FC;padding:2px 6px;border-radius:6px;font-size:9px;font-weight:900;">SLOW-MO</span>`;
      const lastHtml = badgesEl.dataset ? badgesEl.dataset.last : badgesEl._lastHtml;
      if (lastHtml !== html) {
        badgesEl.innerHTML = html;
        if (badgesEl.dataset) badgesEl.dataset.last = html;
        else badgesEl._lastHtml = html;
      }
    }

    const unlockCapsule = document.getElementById("hud-unlock-capsule");
    if (unlockCapsule) {
      unlockCapsule.style.display = "none";
    }
  },

  triggerRatingPopup(ratingText, combo = 1) {
    const pill = document.getElementById("hud-rating-pill");
    const textEl = document.getElementById("hud-rating-text");
    const multVal = document.getElementById("hud-combo-mult-val");
    const multRow = document.getElementById("hud-combo-mult-row");
    if (!pill || !textEl) return;
    textEl.textContent = ratingText;
    textEl.className = "hud-rating-text";
    if (ratingText.includes("24K")) {
      pill.style.borderColor = "rgba(250,204,21,1)";
      textEl.style.color = "#FACC15";
    } else if (ratingText === "PERFECT") {
      pill.style.borderColor = "rgba(236,72,153,0.85)";
      textEl.style.color = "#F472B6";
    } else if (ratingText === "SHIELD") {
      pill.style.borderColor = "rgba(245,158,11,0.85)";
      textEl.style.color = "#F59E0B";
      textEl.classList.add("shield");
    } else {
      pill.style.borderColor = "rgba(56,189,248,0.85)";
      textEl.style.color = "#38BDF8";
      textEl.classList.add("great");
    }
    if (multVal) {
      const baseMult = Math.max(1, Math.min(8, Math.floor(combo / 4) + 1));
      const mult = this.is24KOverdrive ? Math.min(8, baseMult * 4) : baseMult;
      multVal.textContent = String(mult);
      if (multRow) multRow.style.opacity = combo > 1 || this.is24KOverdrive ? "1" : "0.45";
    }
    pill.classList.remove("rating-pop");
    void pill.offsetWidth;
    pill.classList.add("rating-pop");
  },

  triggerComboBreak() {
    const pill = document.getElementById("hud-rating-pill");
    const textEl = document.getElementById("hud-rating-text");
    const multVal = document.getElementById("hud-combo-mult-val");
    const multRow = document.getElementById("hud-combo-mult-row");
    if (multVal) multVal.textContent = "1";
    if (multRow) multRow.style.opacity = "0.3";
    if (pill && textEl) {
      textEl.textContent = "MISS";
      textEl.style.color = "#F43F5E";
      pill.style.borderColor = "rgba(244,63,94,0.6)";
      pill.classList.remove("rating-pop");
      void pill.offsetWidth;
      pill.classList.add("rating-pop");
    }
  },

  toggleAltCover() {
    this.unlockAudioOnFirstGesture();
    this.playSfx("click");
    this.prefs.track02AltCover = !this.prefs.track02AltCover;
    this.savePrefs();
    this.syncUI();
  },

  shareRunToSocials() {
    this.playSfx("click");
    const track = OFFICIAL_TRACKS[this.selectedStageIndex] || OFFICIAL_TRACKS[0];
    const scoreFmt = Number(this.score || 0).toLocaleString();
    const shareText = `Just scored ${scoreFmt} on "${track.title}" in No More Illusions! Tagging @EthanKStark #NoMoreIllusions Check out the gamified album:`;
    const shareUrl = "https://oneshelfstudio.github.io/NoMoreIllusions/";
    if (navigator.share) {
      navigator.share({
        title: "No More Illusions — Beat Run",
        text: shareText,
        url: shareUrl
      }).catch(() => {});
    } else if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(`${shareText} ${shareUrl}`).then(() => {
        alert("Copied run details & tags to clipboard! Tag @EthanKStark on TikTok / Instagram #NoMoreIllusions");
      }).catch(() => {});
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
    if (metaEl) metaEl.textContent = `C MINOR • STUDIO MASTER`;

    const stories = {
      track_01: "Written and produced during late-night studio sessions exploring connection, vulnerability, and navigating illusions in modern life. The track fuses warm analog synth pads with crisp 808 percussion.",
      track_02: "An upbeat, hypnotic anthem celebrating artistic independence, relentless drive, and staying true to personal vision against all odds."
    };

    const lyrics = {
      track_01: `"Hold on through the twilight haze...\nBreaking down the walls we raise.\nConfide in me when shadows fall,\nNo more illusions through it all."`,
      track_02: `"Step into the light, make the moment count...\nEvery single obstacle we're climbing the mount.\nNumber one in the arena, sound ringing clear,\nEchoes of the rhythm taking over the year."`
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

  openScreen(stateName, customTab = -1, pushHistory = true) {
    this.unlockAudioOnFirstGesture();
    this.playSfx("whoosh_modal", 0.80);
    if (this.gameState === "DEMO_PLAYER" && stateName !== "DEMO_PLAYER") {
      this.demoAudio.pause();
    }
    this.gameState = stateName;
    if (pushHistory && window.history && typeof window.history.pushState === "function") {
      try {
        window.history.pushState({ screen: stateName, tab: customTab }, "");
      } catch (err) {}
    }
    if (stateName === "CUSTOMIZER") {
      this.customizerTab = (typeof customTab === "number" && customTab >= 0) ? customTab : 0;
      if (this.customizerTab === 0) setTimeout(() => this.resize(), 20);
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

  togglePrizePeek(trackIdx, ev) {
    if (ev && typeof ev.stopPropagation === "function") ev.stopPropagation();
    this.playSfx("click");
    this.peekPrizeTrackIdx = (this.peekPrizeTrackIdx === trackIdx) ? null : trackIdx;
    this.syncUI();
  },

  selectStage(index) {
    this.unlockAudioOnFirstGesture();
    this.playSfx("click");
    this.selectedStageIndex = index;
    this.showTrackDropdown = false;
    this.syncUI();
  },

  nextStage(ev) {
    if (ev && typeof ev.stopPropagation === "function") ev.stopPropagation();
    this.unlockAudioOnFirstGesture();
    this.playSfx("click");
    this.selectedStageIndex = (this.selectedStageIndex + 1) % OFFICIAL_TRACKS.length;
    this.syncUI();
  },

  prevStage(ev) {
    if (ev && typeof ev.stopPropagation === "function") ev.stopPropagation();
    this.unlockAudioOnFirstGesture();
    this.playSfx("click");
    this.selectedStageIndex = (this.selectedStageIndex - 1 + OFFICIAL_TRACKS.length) % OFFICIAL_TRACKS.length;
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

    // 0. Hide legacy text curiosity dropdown (#menu-rewards-dropdown-wrap)
    const rewardsDropWrap = document.getElementById("menu-rewards-dropdown-wrap");
    if (rewardsDropWrap) {
      rewardsDropWrap.innerHTML = "";
    }

    // 1. CAROUSEL SHOWCASE & PAGINATION DOTS
    const trackDotsEl = document.getElementById("menu-track-dots");
    if (trackDotsEl) {
      trackDotsEl.innerHTML = OFFICIAL_TRACKS.map((t, idx) => `
        <div class="track-dot ${idx === this.selectedStageIndex ? "active" : ""}"
             onclick="window.app.selectStage(${idx})"
             title="Track 0${idx + 1}: ${t.title}"></div>
      `).join("");
    }

    const titleMainEl = document.getElementById("menu-track-title-main");
    const metaRowEl = document.getElementById("menu-track-meta-row");
    const isCurUnlocked = this.prefs.unlockedTracks.includes(activeStage.id) || this.prefs.isVip || this.selectedStageIndex === 0;
    const stars = this.prefs.trackMasteryMedals[activeStage.id] || 0;
    const starsStr = [1, 2, 3].map(s => s <= stars ? "★" : "☆").join("");

    if (titleMainEl) {
      titleMainEl.textContent = `TRACK 0${this.selectedStageIndex + 1}: ${activeStage.title.toUpperCase()}`;
      titleMainEl.style.color = isCurUnlocked ? "#FAF8F5" : "#94A3B8";
    }
    if (metaRowEl) {
      metaRowEl.textContent = isCurUnlocked
        ? `${activeStage.key || "STUDIO MASTER"} • ${starsStr} READY`
        : `LOCKED • BEAT TRACK 01 OR UNLOCK (1,200 NOTES)`;
      metaRowEl.style.color = isCurUnlocked ? "#FACC15" : "#F87171";
    }

    const altCoverBtn = document.getElementById("menu-alt-cover-btn");
    const altCoverLabel = document.getElementById("menu-alt-cover-label");
    if (altCoverBtn) {
      if (this.selectedStageIndex === 1) {
        altCoverBtn.style.display = "inline-flex";
        if (altCoverLabel) {
          altCoverLabel.textContent = this.prefs.track02AltCover ? "✓ ALT LOTUS COVER" : "ALT COVER";
        }
        altCoverBtn.style.borderColor = this.prefs.track02AltCover ? "#FACC15" : "rgba(255,255,255,0.25)";
        altCoverBtn.style.background = this.prefs.track02AltCover ? "rgba(250,204,21,0.22)" : "rgba(255,255,255,0.08)";
      } else {
        altCoverBtn.style.display = "none";
      }
    }

    // 2. 4 SCULPTED CHAMFERED NEON MODE GEMS (#menu-remix-pills: Chill, Normal, Fast, Endless!)
    const remixPillsEl = document.getElementById("menu-remix-pills");
    if (remixPillsEl) {
      const gems = [
        { id: "SLOWED", label: "CHILL", sub: "0.5x • 2 Runs", col: "#38BDF8" },
        { id: "ORIGINAL", label: "NORMAL", sub: "1.0x • 1 Run", col: "#4ADE80" },
        { id: "OVERCLOCK", label: "FAST", sub: "1.5x Score", col: "#F43F5E" },
        { id: "ENDLESS", label: "ENDLESS", sub: "+6% Spd/Loop", col: "#A855F7" }
      ];
      const gemsHtml = gems.map(g => {
        const isAct = !this.isBandHeroMode && activeRemix.id === g.id;
        return `
          <div onclick="window.app.isBandHeroMode=false;window.app.setRemixMode('${g.id}')"
               style="flex:1;min-width:0;padding:8px 4px;cursor:pointer;text-align:center;
                      clip-path:polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px);
                      background:${isAct ? `${g.col}33` : "#070C18"};
                      border:${isAct ? `2px solid ${g.col}` : "1px solid rgba(255,255,255,0.16)"};">
            <div style="font-size:10px;font-weight:900;color:${isAct ? g.col : "#FFFFFF"};white-space:nowrap;">${g.label}</div>
            <div style="font-size:7.5px;font-weight:800;color:#CBD5E1;margin-top:2px;white-space:nowrap;">${g.sub}</div>
          </div>
        `;
      }).join("");

      const bhActive = Boolean(this.isBandHeroMode);
      const bhGemHtml = `
        <div onclick="window.app.toggleBandHeroMode()"
             style="flex:1.15;min-width:0;padding:8px 4px;cursor:pointer;text-align:center;
                    clip-path:polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px);
                    background:${bhActive ? "rgba(250,204,21,0.26)" : "#160B24"};
                    border:${bhActive ? "2px solid #FACC15" : "1px solid rgba(236,72,153,0.48)"};">
          <div style="font-size:9.5px;font-weight:900;color:${bhActive ? "#FACC15" : "#F472B6"};white-space:nowrap;">CLASSIC</div>
          <div style="font-size:7px;font-weight:800;color:#CBD5E1;margin-top:2px;white-space:nowrap;">${bhActive ? "Highway Mode" : "4-Lane Highway"}</div>
        </div>
      `;

      remixPillsEl.innerHTML = `
        <div style="display:flex;gap:6px;width:100%;margin-top:4px;">
          ${gemsHtml}
          ${bhGemHtml}
        </div>
      `;
    }

    // 3. Grand Pulsing Jewel Stage Launch Emblem (#btn-launch-stage)
    const launchBtn = document.getElementById("btn-launch-stage");
    const isStageUnlocked = this.prefs.unlockedTracks.includes(activeStage.id) || this.prefs.isVip || this.selectedStageIndex === 0;
    const launchAccent = !isStageUnlocked ? "#F59E0B" : (this.isBandHeroMode ? "#FACC15" : activeRemix.color);

    const launchMain = document.getElementById("menu-launch-main");
    if (launchMain) {
      launchMain.innerHTML = !isStageUnlocked
        ? `UNLOCK TRACK 0${this.selectedStageIndex + 1} (1,200 NOTES)`
        : (this.isBandHeroMode
          ? `PLAY CLASSIC HIGHWAY`
          : `▶ &nbsp;PLAY ${activeStage.title.toUpperCase()}`);
    }
    const launchSub = document.getElementById("menu-launch-sub");
    if (launchSub) {
      launchSub.textContent = !isStageUnlocked
        ? "TAP TO UNLOCK WITH COLLECTED NOTES"
        : (this.isBandHeroMode
          ? "4-LANE GUITAR HERO RHYTHM RUN"
          : `${activeRemix.displayName} • STUDIO MASTER`);
      launchSub.style.color = isStageUnlocked ? "#FAF8F5" : "#FACC15";
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
          ? `USE 250 NOTES TO REVIVE <span style="opacity:0.75;font-weight:700;">&nbsp;(YOU HAVE ${this.prefs.points})</span>`
          : `NEED 250 NOTES <span style="opacity:0.75;font-weight:700;">&nbsp;(YOU HAVE ${this.prefs.points})</span>`;
        notesBtn.style.opacity = canAfford ? "1" : "0.6";
      }
    }

    // VIP badge on menu
    const vipBadge = document.getElementById("menu-vip-badge");
    if (vipBadge) {
      vipBadge.textContent = this.prefs.vipUnlocked ? "• VIP UNLOCKED" : "";
    }

    // Track Completed Screen (Visual-First Premiere Celebration)
    if (this.gameState === "TRACK_COMPLETED") {
      const tcArt = document.getElementById("tc-album-art-img");
      if (tcArt) {
        let artSrc = activeStage.coverArt || "assets/nmi_logo.png";
        if (this.selectedStageIndex === 1 && this.prefs.track02AltCover && activeStage.coverArtAlt) {
          artSrc = activeStage.coverArtAlt;
        }
        tcArt.src = artSrc;
      }
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
        tcEarned.textContent = `+${totalEarned} NOTES`;
      }
      if (typeof this.getPrimarySpotlightExclusive === "function") {
        const excl = this.getPrimarySpotlightExclusive(this.selectedStageIndex);
        if (excl) {
          const bEl = document.getElementById("tc-excl-badge");
          const tEl = document.getElementById("tc-excl-title");
          const bodyEl = document.getElementById("tc-excl-body");
          const qEl = document.getElementById("tc-excl-quote");
          if (bEl) bEl.textContent = `VAULT EXCLUSIVE UNLOCKED • ${excl.categoryBadge}`;
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
    if (tabIdx === 0) setTimeout(() => this.resize(), 20);
    this.syncCustomizerUI();
  },

  setSkinsSubCategory(subIdx) {
    this.playSfx("click");
    this.skinsSubCategory = subIdx;
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
    if (ptsEl) ptsEl.textContent = `${this.prefs.points} NOTES`;

    const tab = (typeof this.customizerTab === "number" && this.customizerTab >= 0) ? this.customizerTab : 0;
    const sub = (typeof this.skinsSubCategory === "number") ? this.skinsSubCategory : 0;

    // 1. Sync Tab Buttons
    [0, 1, 2].forEach(i => {
      const btn = document.getElementById(`cust-tab-${i}`);
      if (btn) btn.classList.toggle("active", i === tab);
    });

    // 2. Toggle Tab Views
    const skinsView = document.getElementById("cust-view-skins");
    const vaultView = document.getElementById("cust-view-vault");
    const exclView = document.getElementById("cust-view-exclusives");
    if (skinsView) skinsView.style.display = tab === 0 ? "block" : "none";
    if (vaultView) vaultView.style.display = tab === 1 ? "block" : "none";
    if (exclView) exclView.style.display = tab === 2 ? "block" : "none";

    const titleEl = document.getElementById("cust-modal-title");
    const subTitleEl = document.getElementById("cust-modal-sub");
    if (titleEl) {
      titleEl.textContent = tab === 0 ? "HERO SKINS & FX" : tab === 1 ? "ARTIST ALBUM VAULT" : "ARTIST EXCLUSIVES";
    }
    if (subTitleEl) {
      subTitleEl.textContent = tab === 0 ? "CUSTOM CORES, SILK TRAILS & NEBULA PALETTES" : tab === 1 ? "OFFICIAL STUDIO MASTERS & BONUS ART" : "LINER NOTES, STUDIO DIARIES & VIP PASSES";
    }

    // 3. Tab 0: Skins & FX (No Enemies!)
    if (tab === 0) {
      [0, 1, 2].forEach(s => {
        const subBtn = document.getElementById(`cust-sub-${s}`);
        if (subBtn) {
          const isAct = s === sub;
          subBtn.style.color = isAct ? (s === 0 ? "#38BDF8" : s === 1 ? "#4ADE80" : "#C084FC") : "#94A3B8";
          subBtn.style.background = isAct ? "rgba(255,255,255,0.12)" : "rgba(255,255,255,0.03)";
          subBtn.style.borderColor = isAct ? (s === 0 ? "#38BDF8" : s === 1 ? "#4ADE80" : "#C084FC") : "rgba(255,255,255,0.18)";
        }
      });

      const itemsBySub = [
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

      const activeItems = itemsBySub[sub] || itemsBySub[0];
      const itemsContainer = document.getElementById("cust-items-list");
      if (itemsContainer) {
        itemsContainer.innerHTML = activeItems.map((it) => {
          const isUnlocked = this.prefs.unlockedItems.includes(it.id);
          const isSelected = (it.cat === "skin" && this.prefs.selectedSkin === it.id) ||
                             (it.cat === "trail" && this.prefs.selectedTrail === it.id) ||
                             (it.cat === "theme" && this.prefs.selectedTheme === it.id);
          const canAfford = this.prefs.points >= it.price;
          const btnLabel = isSelected ? "EQUIPPED" : isUnlocked ? "EQUIP" : `${it.price} NOTES`;
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
    }

    // 4. Tab 1: Artist Vault (2 Tracks, Alternate Lotus Cover for Track 2)
    if (tab === 1) {
      const vaultListEl = document.getElementById("cust-vault-tracks");
      if (vaultListEl) {
        vaultListEl.innerHTML = OFFICIAL_TRACKS.map((t, idx) => {
          const isUnlocked = this.prefs.unlockedTracks.includes(t.id);
          const bestProg = Math.max(isUnlocked ? 1 : 0, this.prefs.trackBestProgress[t.id] || 0);
          const pct = Math.round(bestProg * 100);
          const hasAltCover = t.id === "track_02";
          return `
            <div class="vault-track-row ${isUnlocked ? "unlocked" : "locked"}" style="flex-direction:column;align-items:stretch;gap:6px;">
              <div style="display:flex;align-items:center;gap:10px;cursor:pointer;" onclick="${isUnlocked ? `window.app.openTrackInDemo(${idx})` : ""}">
                <div class="vault-track-icon">${isUnlocked ? "▶" : "•"}</div>
                <div style="flex:1;min-width:0;">
                  <div class="vault-track-name">${t.title}</div>
                  <div class="vault-track-sub" style="color:${isUnlocked ? "#4ADE80" : "#94A3B8"};">
                    ${isUnlocked ? "UNLOCKED • TAP TO LISTEN FULL TRACK" : `Complete Stage (${pct}% Revealed) to unlock`}
                  </div>
                </div>
                <div class="vault-track-dur">${t.durationLabel}</div>
              </div>
              ${hasAltCover ? `
                <div style="display:flex;justify-content:flex-end;margin-top:2px;">
                  <button class="btn-close-pill" style="font-size:9.5px;color:${this.prefs.track02AltCover ? "#FACC15" : "#38BDF8"};border-color:${this.prefs.track02AltCover ? "#FACC15" : "rgba(56,189,248,0.5)"};background:${this.prefs.track02AltCover ? "rgba(250,204,21,0.18)" : "transparent"};"
                          onclick="window.app.toggleAltCover()">
                    ${this.prefs.track02AltCover ? "✓ ALT LOTUS COVER ACTIVE" : "USE ALTERNATE LOTUS ART"}
                  </button>
                </div>
              ` : ""}
            </div>
          `;
        }).join("");
      }
    }

    // 5. Tab 2: Artist Exclusives
    if (tab === 2) {
      if (typeof this.renderArtistSpecialsVault === "function") {
        this.renderArtistSpecialsVault();
      }
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
      statusEl.textContent = isUnlocked ? "UNLOCKED IN ALBUM VAULT" : `LOCKED (${Math.round(bestProg * 100)}% REVEALED)`;
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
            <div class="vault-track-icon" style="color:${u ? "#4ADE80" : "#FACC15"};">${u ? "▶" : "•"}</div>
            <div style="flex:1;">
              <div class="vault-track-name">${t.title}</div>
              <div class="vault-track-sub">${u ? `${t.subtitle}` : `${p}% Revealed — Survive stage to unlock`}</div>
            </div>
            <div class="vault-track-dur">${t.durationLabel}</div>
          </div>
        `;
      }).join("");
    }
    this.updateDemoPlayerProgress();
  }
});
