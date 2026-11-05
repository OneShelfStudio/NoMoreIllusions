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
    id: "excl_confide_master",
    categoryBadge: "STUDIO MASTER • TRACK 01",
    title: "'Confide' — Studio Master & Vocal Stems",
    unlockRequirementText: "Reach 50% in 'Confide' or complete stage",
    previewTeaser: "Explore Ethan's late-night vocal sessions, analog Rhodes chords, and full studio mix...",
    unlockedHeadline: "STUDIO MASTER: CONFIDE (88 BPM)",
    unlockedBody: "Written during late-night studio sessions exploring vulnerability, 'Confide' is built around warm analog Rhodes chords and a tight 88 BPM vocal pocket. Every beat orb in the highway was aligned to the natural breath and cadence of this vocal take.",
    unlockedQuote: "— Ethan Correa, Composer & Vocalist",
    bonusGamePerk: "Unlocks 'Confide' Full Master Playback + Fast/Chill Modes",
    accentHex: "#4ADE80",
    isUnlockedFn: (app) =>
      app.prefs.vipUnlocked ||
      app.prefs.unlockedTracks.includes("track_01") ||
      (app.prefs.trackBestProgress["track_01"] || 0) >= 0.50
  },
  {
    id: "excl_number_one_master",
    categoryBadge: "STUDIO MASTER • TRACK 02",
    title: "'Number One' — Hi-Hat Cadence & Master Reel",
    unlockRequirementText: "Reach 50% in 'Number One' or complete stage",
    previewTeaser: "Unlock the rhythmic vocal chops, open hi-hat breakdown, and high-energy electronic drop...",
    unlockedHeadline: "STUDIO MASTER: NUMBER ONE",
    unlockedBody: "'Number One' fuses modern hip-hop syncopation with progressive electronic energy. The open hi-hat swing and layered vocal harmonies were designed to create a hypnotic, forward-driving pocket.",
    unlockedQuote: "— Ethan Correa, Composer & Vocalist",
    bonusGamePerk: "Unlocks 'Number One' Full Master Playback + ★1 Gold Vinyl Star",
    accentHex: "#C084FC",
    isUnlockedFn: (app) =>
      app.prefs.vipUnlocked ||
      app.prefs.unlockedTracks.includes("track_02") ||
      (app.prefs.trackBestProgress["track_02"] || 0) >= 0.50
  },
  {
    id: "excl_upcoming_teaser",
    categoryBadge: "UPCOMING STUDIO RELEASES",
    title: "More Artist Exclusives Coming Soon",
    unlockRequirementText: "Master current tracks to qualify for upcoming releases",
    previewTeaser: "Ethan Correa is producing upcoming singles and exclusive stem packs. Level up your album mastery to unlock new tracks upon release...",
    unlockedHeadline: "NEW ARTIST EXCLUSIVES IN PRODUCTION",
    unlockedBody: "Ethan Correa is currently in the studio recording upcoming singles, acoustic stripped sessions, and stems. Keep honing your rhythm score to instantly unlock VIP rewards and community drops when new tracks go live.",
    unlockedQuote: "Tag @EthanCorrea #NoMoreIllusions on TikTok & Instagram",
    bonusGamePerk: "VIP Status: Automatic Access to Upcoming Singles",
    accentHex: "#FACC15",
    isUnlockedFn: (app) => app.prefs.vipUnlocked || false
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
    document.getElementById("disc-card").style.borderColor = disc.accentHex;

    const bodyEl = document.getElementById("disc-body");
    const tipEl = document.getElementById("disc-tip");
    const canvasEl = document.getElementById("disc-spotlight-canvas");

    if (disc.key === "FAN_WELCOME") {
      if (canvasEl) canvasEl.style.display = "none";
      if (tipEl) tipEl.style.display = "none";
      if (bodyEl) {
        bodyEl.style.background = "transparent";
        bodyEl.style.border = "none";
        bodyEl.style.padding = "0";
        bodyEl.innerHTML = `
          <div style="display:flex;gap:8px;width:100%;margin:10px 0;">
            <div style="flex:1;background:#070E1D;border:1.2px solid rgba(56,189,248,0.55);border-radius:12px;padding:10px 4px;text-align:center;">
              <div style="font-size:26px;">👆</div>
              <div style="font-size:11px;font-weight:900;color:#38BDF8;margin-top:4px;">DRAG</div>
              <div style="font-size:9px;font-weight:700;color:#94A3B8;">To Glide</div>
            </div>
            <div style="flex:1;background:#070E1D;border:1.2px solid rgba(250,204,21,0.55);border-radius:12px;padding:10px 4px;text-align:center;">
              <div style="font-size:26px;">🎵</div>
              <div style="font-size:11px;font-weight:900;color:#FACC15;margin-top:4px;">CATCH</div>
              <div style="font-size:9px;font-weight:700;color:#94A3B8;">Beat Notes</div>
            </div>
            <div style="flex:1;background:#070E1D;border:1.2px solid rgba(244,63,94,0.55);border-radius:12px;padding:10px 4px;text-align:center;">
              <div style="font-size:26px;">⚠️</div>
              <div style="font-size:11px;font-weight:900;color:#F43F5E;margin-top:4px;">AVOID</div>
              <div style="font-size:9px;font-weight:700;color:#94A3B8;">Red Hazards</div>
            </div>
          </div>
          <div style="font-size:10px;font-weight:700;color:#CBD5E1;text-align:center;margin-bottom:8px;">Unlock tracks &amp; artist vault secrets as you play!</div>
        `;
      }
    } else {
      if (canvasEl) canvasEl.style.display = "block";
      if (tipEl) tipEl.style.display = "block";
      if (bodyEl) {
        bodyEl.style.background = "rgba(6,10,22,0.85)";
        bodyEl.style.border = "1px solid rgba(255,255,255,0.1)";
        bodyEl.style.padding = "10px 12px";
        bodyEl.textContent = disc.bodyText;
      }
      if (tipEl) tipEl.textContent = disc.tipText;
    }

    const btn = document.getElementById("disc-btn");
    if (btn) {
      btn.textContent = disc.key === "FAN_WELCOME"
        ? "LET'S PLAY ♪"
        : "▶  GOT IT — RESUME STAGE";
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
      <div style="padding:4px 2px 8px;margin-bottom:8px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
          <div style="font-size:11px;font-weight:900;color:#EAB308;letter-spacing:1.1px;">★ ARTIST VAULT &amp; SECRETS</div>
          <div style="font-size:10px;font-weight:900;color:#4ADE80;letter-spacing:0.8px;">
            ${unlockedCount} / ${totalCount} UNLOCKED
          </div>
        </div>
        <div style="width:100%;height:3px;background:rgba(255,255,255,0.12);overflow:hidden;">
          <div style="width:${pct}%;height:100%;background:linear-gradient(90deg,#EAB308,#4ADE80,#38BDF8);"></div>
        </div>
      </div>
      <div style="font-size:9.5px;font-weight:900;color:#38BDF8;letter-spacing:0.9px;margin:4px 0 6px;">
        💌 TAP ANY MYSTERY ENVELOPE TO PEEK INSIDE
      </div>
    `;

    html += ARTIST_EXCLUSIVES_CATALOG.map((item) => {
      const unlocked = item.isUnlockedFn(this);
      const isExpanded = this.expandedExclusiveId === item.id;
      const badgeLabel = unlocked ? (isExpanded ? "✓ OPEN ▴" : "✓ READ ▾") : (isExpanded ? "🔒 HOW ▴" : "🔒 PEEK ▾");
      const badgeCol = unlocked ? "#4ADE80" : "#EAB308";

      return `
        <div onclick="window.app.toggleExclusiveEnvelope('${item.id}')"
             style="padding:10px 4px;border-bottom:1px solid ${unlocked ? item.accentHex : "rgba(234,179,8,0.28)"};margin-bottom:4px;cursor:pointer;">
          <div style="display:flex;justify-content:space-between;align-items:center;gap:8px;">
            <div style="min-width:0;flex:1;">
              <div style="font-size:8.5px;font-weight:900;color:${item.accentHex};letter-spacing:0.8px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${item.categoryBadge}</div>
              <div style="font-size:12.5px;font-weight:900;color:#FAF8F5;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;margin-top:2px;">${item.title}</div>
            </div>
            <div style="font-size:9.5px;font-weight:900;color:${badgeCol};white-space:nowrap;">
              ${badgeLabel}
            </div>
          </div>
          ${isExpanded ? (unlocked ? `
            <div style="padding:8px 4px 4px;margin-top:6px;border-left:2px solid ${item.accentHex};padding-left:10px;">
              <div style="font-size:9.5px;font-weight:900;color:${item.accentHex};margin-bottom:4px;">${item.unlockedHeadline}</div>
              <div style="font-size:11px;color:#F1F5F9;line-height:1.5;">${item.unlockedBody}</div>
              <div style="font-size:10px;font-weight:800;font-style:italic;color:#EAB308;margin-top:6px;">${item.unlockedQuote}</div>
              <div style="font-size:9.5px;font-weight:800;color:#4ADE80;margin-top:4px;">✦ PERK: ${item.bonusGamePerk}</div>
            </div>
          ` : `
            <div style="padding:8px 4px 4px;margin-top:6px;border-left:2px solid #EAB308;padding-left:10px;">
              <div style="font-size:9px;font-weight:900;color:#EAB308;">HOW TO UNLOCK THIS SECRET:</div>
              <div style="font-size:10.5px;font-weight:700;color:#FAF8F5;margin-top:2px;">${item.unlockRequirementText}</div>
            </div>
          `) : ""}
        </div>
      `;
    }).join("");

    container.innerHTML = html;
  }
});
