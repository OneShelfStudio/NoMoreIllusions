// ============================================================================
// NO MORE ILLUSIONS — FIRST-TIME CONTEXTUAL DISCOVERY & ARTIST SPECIALS VAULT
// 1:1 Port of FirstTimeTutorialModal.kt & ArtistVaultShowcase.kt
// ============================================================================

const FIRST_TIME_DISCOVERIES = {
  FAN_WELCOME: {
    key: "FAN_WELCOME",
    badgeText: "★ WELCOME TO THE INTERACTIVE ALBUM",
    title: "PLAY TO UNLOCK ARTIST EXCLUSIVES",
    subtitle: "What You Unlock By Playing The Music",
    bodyText: "This is more than an arcade game—it's Ethan's interactive visual album. Every stage you survive and beat streak you build unlocks personal Studio Liner Notes, Behind-the-Scenes Vocal Session Secrets, Slowed/Nightcore Master Reels, and Backstage Lyric Storyboards in the Artist Specials Vault.",
    tipText: "TIP: Tap '★ SPECIALS & ALBUM' on the main menu anytime to view your unlocked Artist Notes & Exclusives!",
    accentHex: "#FACC15",
    accentRgb: [250, 204, 21]
  },
  BEAT_CHAIN: {
    key: "BEAT_CHAIN",
    badgeText: "NEW MECHANIC DISCOVERED • 1 OF 4",
    title: "STATIONARY BEAT CHAINS (1 → 2 → 3 → 4)",
    subtitle: "Hit Sequenced Notes On Time To The Music",
    bodyText: "Numbered Beat Orbs stay anchored in place connected by glowing silk ribbons. Watch the outer ring shrink toward the orb—glide through each orb right as the ring locks in to build up to an 8x Combo! Missing a beat before it fades only resets your combo (it never hurts your ship).",
    tipText: "FOLLOW THE CURVED SILK STREAM FROM 1 → 2 → 3 → 4 IN RHYTHM!",
    accentHex: "#22D3EE",
    accentRgb: [34, 211, 238]
  },
  SUPERNOVA_ORB: {
    key: "SUPERNOVA_ORB",
    badgeText: "NEW MECHANIC DISCOVERED • 2 OF 4",
    title: "24K SUPERNOVA & POWER ORBS",
    subtitle: "Lyric Drop Triggers & Harmonic Buffs",
    bodyText: "Larger Golden Supernova Orbs and colored Aura Orbs spawn on key vocal moments in Ethan's tracks. Catching them triggers 2x Constellation Score Rush, Aegis Invincibility Shields, Tractor Reach Magnets, or Nova Blasts that vaporize nearby hazards!",
    tipText: "PRIORITIZE GOLDEN SUPERNOVA ORBS FOR MASSIVE SCORE & PROTECTION!",
    accentHex: "#FACC15",
    accentRgb: [250, 204, 21]
  },
  SLIDE_STREAM: {
    key: "SLIDE_STREAM",
    badgeText: "NEW MECHANIC DISCOVERED • 3 OF 4",
    title: "MUSICAL SLIDE-NOTE STREAM",
    subtitle: "Automatic Ribbon Surfing & Invincibility",
    bodyText: "Paired Cyan & Gold portals are connected by a flowing musical silk stream with 5 rhythm pearls. Touch either portal entrance and your ship automatically surfs the entire curved melody line—collecting 5 rapid beat hits while completely invincible!",
    tipText: "STEER INTO EITHER PORTAL RING TO SURF THE MELODY STREAM!",
    accentHex: "#38BDF8",
    accentRgb: [56, 189, 248]
  },
  HAZARD_SHIELD: {
    key: "HAZARD_SHIELD",
    badgeText: "NEW MECHANIC DISCOVERED • 4 OF 4",
    title: "EDGE WARNINGS & FOCUS SLOW-MO",
    subtitle: "Smooth Evasion & Time Dilation",
    bodyText: "Soft crimson ripples on the screen edge show where a hazard will enter before it launches—and hazards naturally curve away from Beat Orbs so your rhythm path stays fair. If space ever feels tight, hold the bottom FOCUS bar to slow time to 42%!",
    tipText: "HOLD 'FOCUS' AT THE BOTTOM ANYTIME TO SLOW DOWN TIME!",
    accentHex: "#FB7185",
    accentRgb: [251, 113, 133]
  }
};

const ARTIST_EXCLUSIVES_CATALOG = [
  {
    id: "excl_confide_note",
    categoryBadge: "PERSONAL ARTIST NOTE • TRACK 01",
    title: "Artist Fan Note: 'Confide'",
    unlockRequirementText: "Reach 50% in 'Confide' or unlock the track",
    previewTeaser: "Unlock the personal artist note & behind-the-scenes fan message for 'Confide'...",
    unlockedHeadline: "ARTIST FAN NOTE — 'CONFIDE'",
    unlockedBody: "Here is where you can put your personal story, lyrics, or behind-the-scenes studio note for your fans about 'Confide'!",
    unlockedQuote: "— Here is where you can put a custom sign-off or dedication for your fans",
    bonusGamePerk: "+15% Score Resonance Aura unlocked on Confide",
    accentHex: "#38BDF8",
    isUnlockedFn: (app) =>
      app.prefs.vipUnlocked ||
      app.prefs.unlockedTracks.includes("track_01") ||
      (app.prefs.trackBestProgress["track_01"] || 0) >= 0.50
  },
  {
    id: "excl_confide_master",
    categoryBadge: "UNLOCKABLE AUDIO REEL • TRACK 01",
    title: "'Confide' — Full Album Master & Remix Speeds",
    unlockRequirementText: "Complete 'Confide' (100% Stage Clear)",
    previewTeaser: "Survive to the finale of 'Confide' to unlock unlimited on-demand playback in your Album Vault...",
    unlockedHeadline: "FULL TRACK UNLOCKED IN ALBUM DECK",
    unlockedBody: "Here is where you can put a custom celebration note for fans who unlock the full 'Confide' track in their Album Vault!",
    unlockedQuote: "UNLOCKED: Full 'Confide' Playback + Slowed & Fast Remix Modes",
    bonusGamePerk: "Unlocks 'Confide' Full Playback + ★1 Gold Vinyl Star",
    accentHex: "#4ADE80",
    isUnlockedFn: (app) =>
      app.prefs.vipUnlocked ||
      app.prefs.unlockedTracks.includes("track_01")
  },
  {
    id: "excl_number_one_note",
    categoryBadge: "PERSONAL ARTIST NOTE • TRACK 02",
    title: "Artist Fan Note: 'Number One'",
    unlockRequirementText: "Reach 50% in 'Number One' or unlock the track",
    previewTeaser: "Unlock the personal artist note & behind-the-scenes fan message for 'Number One'...",
    unlockedHeadline: "ARTIST FAN NOTE — 'NUMBER ONE'",
    unlockedBody: "Here is where you can put your personal story, lyrics, or behind-the-scenes studio note for your fans about 'Number One'!",
    unlockedQuote: "— Here is where you can put a custom sign-off or dedication for your fans",
    bonusGamePerk: "+15% Combo Rush duration on Number One",
    accentHex: "#FACC15",
    isUnlockedFn: (app) =>
      app.prefs.vipUnlocked ||
      app.prefs.unlockedTracks.includes("track_02") ||
      (app.prefs.trackBestProgress["track_02"] || 0) >= 0.50
  },
  {
    id: "excl_number_one_master",
    categoryBadge: "UNLOCKABLE AUDIO REEL • TRACK 02",
    title: "'Number One' — Full Album Master & Remix Speeds",
    unlockRequirementText: "Complete 'Number One' (100% Stage Clear)",
    previewTeaser: "Survive to the finale of 'Number One' to unlock the complete track in your Album Vault...",
    unlockedHeadline: "FULL TRACK UNLOCKED IN ALBUM DECK",
    unlockedBody: "Here is where you can put a custom celebration note for fans who unlock the full 'Number One' track in their Album Vault!",
    unlockedQuote: "UNLOCKED: Full 'Number One' Playback + Slowed & Fast Remix Modes",
    bonusGamePerk: "Unlocks 'Number One' Full Playback + ★1 Gold Vinyl Star",
    accentHex: "#C084FC",
    isUnlockedFn: (app) =>
      app.prefs.vipUnlocked ||
      app.prefs.unlockedTracks.includes("track_02")
  },
  {
    id: "excl_vocal_secrets",
    categoryBadge: "BACKSTAGE SESSION SECRET • COMBO REWARD",
    title: "Backstage Studio Secret (8x Streak Reward)",
    unlockRequirementText: "Reach an 8x Beat Combo on any song (★2 Star)",
    previewTeaser: "Hit 8 Beat Pegs in a row on time to unlock this backstage artist secret for your fans...",
    unlockedHeadline: "BACKSTAGE STUDIO SECRET (8x STREAK REWARD)",
    unlockedBody: "Here is where you can put a special backstage studio secret, vocal session story, or exclusive message for fans who hit an 8x Beat Streak!",
    unlockedQuote: "UNLOCKED BY MASTERING AN 8x RHYTHM STREAK",
    bonusGamePerk: "Unlocks ★2 Mastery Star + Pentatonic Chord Stingers",
    accentHex: "#22D3EE",
    isUnlockedFn: (app) =>
      app.prefs.vipUnlocked ||
      Object.values(app.prefs.trackMasteryMedals || {}).some((m) => m >= 2)
  },
  {
    id: "excl_vip_thank_you",
    categoryBadge: "COLLECTOR'S INNER CIRCLE • MASTER VAULT",
    title: "Inner-Circle Fan Letter & Gold Vinyl Cert",
    unlockRequirementText: "Unlock both songs or earn a ★3 Star in Fast/Chill Mode",
    previewTeaser: "Master the interactive album to unlock a personal letter to day-one listeners and the 24K Gold Turntable certification...",
    unlockedHeadline: "DAY-ONE INNER CIRCLE — PERSONAL FAN LETTER",
    unlockedBody: "Here is where you can put your personal thank-you letter, social media tag invite, or VIP perk for fans who unlock both tracks!",
    unlockedQuote: "★ CERTIFIED DAY-ONE INNER CIRCLE LISTENER — NO MORE ILLUSIONS ★",
    bonusGamePerk: "Unlocks 24K Gold Turntable Halo & Inner-Circle Badge",
    accentHex: "#FACC15",
    isUnlockedFn: (app) =>
      app.prefs.vipUnlocked ||
      (app.prefs.unlockedTracks.includes("track_01") && app.prefs.unlockedTracks.includes("track_02")) ||
      Object.values(app.prefs.trackMasteryMedals || {}).some((m) => m >= 3)
  },
  {
    id: "excl_endless_champion",
    categoryBadge: "🏆 ENDLESS LOOP CHAMPION SECRET",
    title: "Artist's Secret Message to Endless Champions",
    unlockRequirementText: "Score 15,000+ pts or reach Loop #2 in ∞ Endless Mode",
    previewTeaser: "Survive the accelerating ∞ Endless Loop to unlock a personal artist message & secret fan reward...",
    unlockedHeadline: "SECRET ARTIST MESSAGE — ∞ ENDLESS CHAMPION",
    unlockedBody: "Here is where you can put your special reward message, secret DM code, or unreleased demo link for fans who conquer the accelerating ∞ Endless Loop!",
    unlockedQuote: "★ TOP ENDLESS SCORE REWARD FOR YOUR BIGGEST FANS ★",
    bonusGamePerk: "Unlocks Champion Crown Flair & Fan Reward Claim",
    accentHex: "#F43F5E",
    isUnlockedFn: (app) =>
      app.prefs.vipUnlocked ||
      app.prefs.highScore >= 15000 ||
      (app.prefs.endlessBestLoop || 0) >= 2
  }
];

Object.assign(NoMoreIllusionsApp.prototype, {
  hasSeenDiscovery(key) {
    return localStorage.getItem(`nmi_seen_disc_${key}`) === "true";
  },

  markDiscoverySeen(key) {
    localStorage.setItem(`nmi_seen_disc_${key}`, "true");
  },

  triggerDiscoveryIfUnseen(key) {
    if (this.activeDiscovery) return false;
    if (this.hasSeenDiscovery(key)) return false;
    const disc = FIRST_TIME_DISCOVERIES[key];
    if (!disc) return false;

    this.markDiscoverySeen(key);
    this.activeDiscovery = disc;
    if (this.gameState === "PLAYING" && this.trackAudio && !this.trackAudio.paused) {
      this.trackAudio.pause();
      this._discoveryPausedTrack = true;
    } else {
      this._discoveryPausedTrack = false;
    }
    this.playSfx("whoosh_modal", 0.85);
    this.showDiscoveryModalUI(disc);
    return true;
  },

  showDiscoveryModalUI(disc) {
    const modal = document.getElementById("discovery-modal");
    if (!modal) return;
    document.getElementById("disc-badge").textContent = disc.badgeText;
    document.getElementById("disc-badge").style.color = disc.accentHex;
    document.getElementById("disc-badge").style.borderColor = disc.accentHex + "88";
    document.getElementById("disc-title").textContent = disc.title;
    document.getElementById("disc-sub").textContent = disc.subtitle;
    document.getElementById("disc-sub").style.color = disc.accentHex;
    document.getElementById("disc-body").textContent = disc.bodyText;
    document.getElementById("disc-tip").textContent = disc.tipText;
    document.getElementById("disc-card").style.borderColor = disc.accentHex;
    const btn = document.getElementById("disc-btn");
    if (btn) {
      btn.textContent = disc.key === "FAN_WELCOME"
        ? "★  AWESOME — LET'S EXPERIENCE THE ALBUM"
        : "▶  GOT IT — RESUME STAGE (NEVER SHOW AGAIN)";
    }
    modal.style.display = "flex";
  },

  dismissDiscoveryModal() {
    this.playSfx("whoosh_modal", 0.78);
    const modal = document.getElementById("discovery-modal");
    if (modal) modal.style.display = "none";
    this.activeDiscovery = null;
    if (this._discoveryPausedTrack && this.gameState === "PLAYING" && !this.prefs.musicMuted) {
      this.trackAudio.play().catch(() => {});
    }
    this._discoveryPausedTrack = false;
  },

  drawDiscoverySpotlightCanvas() {
    const canvas = document.getElementById("disc-spotlight-canvas");
    if (!canvas || !this.activeDiscovery) return;
    const ctx = canvas.getContext("2d");
    const w = canvas.width;
    const h = canvas.height;
    const cx = w * 0.5;
    const cy = h * 0.5;
    const pulse = 0.5 + 0.5 * Math.sin(this.time * 3.8);
    const disc = this.activeDiscovery;
    const col = disc.accentRgb || [56, 189, 248];

    ctx.clearRect(0, 0, w, h);
    const bgGrad = ctx.createRadialGradient(cx, cy, 4, cx, cy, w * 0.55);
    bgGrad.addColorStop(0, rgba(col, 0.24));
    bgGrad.addColorStop(1, "rgba(6, 9, 20, 0.95)");
    ctx.fillStyle = bgGrad;
    ctx.fillRect(0, 0, w, h);

    if (disc.key === "BEAT_CHAIN") {
      const pts = [
        { x: w * 0.20, y: h * 0.66 },
        { x: w * 0.42, y: h * 0.32 },
        { x: w * 0.64, y: h * 0.64 },
        { x: w * 0.84, y: h * 0.36 }
      ];
      for (let i = 0; i < pts.length - 1; i++) {
        const a = pts[i];
        const b = pts[i + 1];
        const mx = (a.x + b.x) * 0.5;
        const my = (a.y + b.y) * 0.5 - 16;
        ctx.beginPath();
        ctx.moveTo(a.x, a.y);
        ctx.quadraticCurveTo(mx, my, b.x, b.y);
        ctx.strokeStyle = "rgba(34, 211, 238, 0.55)";
        ctx.lineWidth = 4;
        ctx.lineCap = "round";
        ctx.stroke();
      }
      pts.forEach((pt, idx) => {
        if (idx === 0) {
          const shrinkR = 16 + (1 - ((this.time * 0.9) % 1)) * 18;
          ctx.beginPath();
          ctx.arc(pt.x, pt.y, shrinkR, 0, Math.PI * 2);
          ctx.strokeStyle = "rgba(250, 204, 21, 0.88)";
          ctx.lineWidth = 2.2;
          ctx.stroke();
        }
        ctx.beginPath();
        ctx.arc(pt.x, pt.y, 13, 0, Math.PI * 2);
        ctx.fillStyle = idx === 0 ? "#FACC15" : "#22D3EE";
        ctx.fill();
        ctx.fillStyle = "#040814";
        ctx.font = "900 12px system-ui, sans-serif";
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillText(String(idx + 1), pt.x, pt.y + 1);
      });
    } else if (disc.key === "SLIDE_STREAM") {
      const ax = w * 0.20, ay = h * 0.64;
      const bx = w * 0.80, by = h * 0.36;
      const ctrlX = w * 0.50, ctrlY = h * 0.14;
      ctx.beginPath();
      ctx.moveTo(ax, ay);
      ctx.quadraticCurveTo(ctrlX, ctrlY, bx, by);
      ctx.strokeStyle = "rgba(56, 189, 248, 0.42)";
      ctx.lineWidth = 10;
      ctx.lineCap = "round";
      ctx.stroke();
      for (let i = 1; i <= 5; i++) {
        const t = i / 6;
        const inv = 1 - t;
        const px = inv * inv * ax + 2 * inv * t * ctrlX + t * t * bx;
        const py = inv * inv * ay + 2 * inv * t * ctrlY + t * t * by;
        ctx.beginPath();
        ctx.arc(px, py, 5.5, 0, Math.PI * 2);
        ctx.fillStyle = "#FACC15";
        ctx.fill();
      }
      ctx.beginPath();
      ctx.arc(ax, ay, 16, 0, Math.PI * 2);
      ctx.strokeStyle = "#22D3EE";
      ctx.lineWidth = 3;
      ctx.stroke();
      ctx.beginPath();
      ctx.arc(bx, by, 16, 0, Math.PI * 2);
      ctx.strokeStyle = "#FACC15";
      ctx.lineWidth = 3;
      ctx.stroke();
    } else {
      for (let k = 0; k < 3; k++) {
        const startA = this.time * 1.4 + k * ((Math.PI * 2) / 3);
        ctx.beginPath();
        ctx.arc(cx, cy, 28 + k * 7, startA, startA + 1.25);
        ctx.strokeStyle = rgba(col, 0.78);
        ctx.lineWidth = 3;
        ctx.lineCap = "round";
        ctx.stroke();
      }
      ctx.beginPath();
      ctx.arc(cx, cy, 20 + pulse * 3, 0, Math.PI * 2);
      ctx.fillStyle = disc.accentHex;
      ctx.fill();
      ctx.beginPath();
      ctx.arc(cx, cy, 10, 0, Math.PI * 2);
      ctx.fillStyle = "#FFFFFF";
      ctx.fill();
    }
  },

  getUnlockedExclusivesCount() {
    return ARTIST_EXCLUSIVES_CATALOG.filter((item) => item.isUnlockedFn(this)).length;
  },

  getPrimarySpotlightExclusive(stageIndex = this.selectedStageIndex) {
    const targetId = stageIndex === 0 ? "excl_confide_note" : "excl_number_one_note";
    return ARTIST_EXCLUSIVES_CATALOG.find((e) => e.id === targetId) || ARTIST_EXCLUSIVES_CATALOG[0];
  },

  toggleExclusiveEnvelope(id) {
    this.playSfx("click");
    this.expandedExclusiveId = (this.expandedExclusiveId === id) ? null : id;
    this.renderArtistSpecialsVault();
  },

  renderArtistSpecialsVault() {
    const container = document.getElementById("cust-specials-list");
    if (!container) return;
    const unlockedCount = this.getUnlockedExclusivesCount();
    const totalCount = ARTIST_EXCLUSIVES_CATALOG.length;
    const pct = Math.max(8, Math.round((unlockedCount / totalCount) * 100));

    let html = `
      <div style="background:linear-gradient(90deg,rgba(31,20,56,0.95),rgba(13,25,43,0.95));border:1.5px solid rgba(250,204,21,0.75);border-radius:14px;padding:10px 12px;margin-bottom:10px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
          <div style="font-size:11px;font-weight:900;color:#FACC15;letter-spacing:0.8px;">★ ARTIST VAULT &amp; SECRETS</div>
          <div style="background:rgba(74,222,128,0.18);border:1px solid #4ADE80;border-radius:50px;padding:2px 9px;font-size:9.5px;font-weight:900;color:#4ADE80;">
            ${unlockedCount} / ${totalCount} UNLOCKED
          </div>
        </div>
        <div style="width:100%;height:6px;background:#040812;border-radius:6px;overflow:hidden;border:1px solid rgba(255,255,255,0.12);">
          <div style="width:${pct}%;height:100%;background:linear-gradient(90deg,#FACC15,#4ADE80,#38BDF8);"></div>
        </div>
      </div>
      <div style="font-size:9.5px;font-weight:900;color:#38BDF8;letter-spacing:0.8px;margin:4px 0 6px;">
        💌 TAP ANY MYSTERY ENVELOPE TO PEEK INSIDE
      </div>
    `;

    html += ARTIST_EXCLUSIVES_CATALOG.map((item) => {
      const unlocked = item.isUnlockedFn(this);
      const isExpanded = this.expandedExclusiveId === item.id;
      const badgeLabel = unlocked ? (isExpanded ? "✓ OPEN ▴" : "✓ READ ▾") : (isExpanded ? "🔒 HOW ▴" : "🔒 PEEK ▾");
      const badgeCol = unlocked ? "#4ADE80" : "#FACC15";
      const borderCol = unlocked ? (isExpanded ? item.accentHex : `${item.accentHex}88`) : (isExpanded ? "rgba(250,204,21,0.55)" : "rgba(148,163,184,0.25)");

      return `
        <div onclick="window.app.toggleExclusiveEnvelope('${item.id}')"
             style="background:rgba(11,16,30,0.94);border:${isExpanded ? "1.5px" : "1px"} solid ${borderCol};border-radius:12px;padding:10px 12px;margin-bottom:8px;cursor:pointer;">
          <div style="display:flex;justify-content:space-between;align-items:center;gap:8px;">
            <div style="min-width:0;flex:1;">
              <div style="font-size:8.5px;font-weight:900;color:${item.accentHex};letter-spacing:0.6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${item.categoryBadge}</div>
              <div style="font-size:12px;font-weight:900;color:#F8FAFC;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;margin-top:2px;">${item.title}</div>
            </div>
            <div style="background:${badgeCol}22;border:1px solid ${badgeCol};border-radius:50px;padding:3px 8px;font-size:8.5px;font-weight:900;color:${badgeCol};white-space:nowrap;">
              ${badgeLabel}
            </div>
          </div>
          ${isExpanded ? (unlocked ? `
            <div style="background:rgba(6,9,19,0.94);border:1px solid ${item.accentHex}55;border-radius:10px;padding:10px;margin-top:8px;">
              <div style="font-size:9px;font-weight:900;color:${item.accentHex};margin-bottom:4px;">${item.unlockedHeadline}</div>
              <div style="font-size:11px;color:#F1F5F9;line-height:1.48;">${item.unlockedBody}</div>
              <div style="font-size:9.5px;font-weight:800;font-style:italic;color:#FACC15;margin-top:6px;">${item.unlockedQuote}</div>
              <div style="font-size:9.5px;font-weight:800;color:#4ADE80;margin-top:4px;">✦ PERK: ${item.bonusGamePerk}</div>
            </div>
          ` : `
            <div style="background:rgba(8,13,26,0.92);border:1px solid rgba(250,204,21,0.38);border-radius:10px;padding:9px 10px;margin-top:8px;">
              <div style="font-size:9px;font-weight:900;color:#FACC15;">HOW TO UNLOCK THIS SECRET:</div>
              <div style="font-size:10.5px;font-weight:700;color:#F8FAFC;margin-top:2px;">${item.unlockRequirementText}</div>
            </div>
          `) : ""}
        </div>
      `;
    }).join("");

    container.innerHTML = html;
  }
});
