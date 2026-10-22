import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

# Read engine.js
with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'r', encoding='utf-8') as f:
    js = f.read()

# 1. Update initSfxPool to use the REAL sound assets on disk + WebAudio synthesizer
old_sfx_pool = '''initSfxPool() {
    const sfxFiles = {
      click: "assets/sfx_button_click.wav",
      orb: "assets/sfx_score_orb.wav",
      powerup: "assets/sfx_powerup.wav",
      shield: "assets/sfx_shield_break.wav",
      gameover: "assets/sfx_game_over.wav",
      levelup: "assets/sfx_level_up.wav"
    };
    for (const [k, src] of Object.entries(sfxFiles)) {
      const a = new Audio(src);
      a.preload = "auto";
      this.sfxPool[k] = a;
    }
  }

  playSfx(name, volume = 0.65) {
    if (this.prefs.sfxMuted) return;
    const base = this.sfxPool[name];
    if (base) {
      try {
        const clone = base.cloneNode();
        clone.volume = volume;
        clone.play().catch(() => {});
      } catch (e) {}
    }
  }'''

new_sfx_pool = '''initSfxPool() {
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
  }

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
  }

  triggerHaptic(pattern = [20]) {
    try {
      if (typeof navigator !== "undefined" && navigator.vibrate) {
        navigator.vibrate(pattern);
      }
    } catch (e) {}
  }

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
  }

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
  }

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
  }'''

js = js.replace(old_sfx_pool, new_sfx_pool)
print('Updated sound synthesis & pool!')

# 2. Add Near-Miss Graze Mechanic into stepPhysics
old_collision = '''// Collision with Player
      const dist = Math.hypot(this.playerX - e.x, this.playerY - e.y);
      if (dist < this.playerRadius * 0.82 + e.radius * 0.82) {'''

new_collision = '''// Near-Miss Adrenaline Graze Check (passing within a hair's breadth of an enemy!)
      const dist = Math.hypot(this.playerX - e.x, this.playerY - e.y);
      const grazeThreshold = this.playerRadius + e.radius + 32;

      if (!e.grazed && dist < grazeThreshold && dist >= this.playerRadius * 0.82 + e.radius * 0.82) {
        e.grazed = true;
        this.playSfx("graze", 0.85);
        this.triggerHaptic([25]);
        this.screenShake = 6;
        this.adrenalineGlitch = 0.28; // Triggers chromatic camera flash
        const grazePts = 60 * this.combo;
        this.score += grazePts;
        this.spawnBurstMotes((this.playerX + e.x) / 2, (this.playerY + e.y) / 2, [254, 240, 138], 10);
        this.floatingTexts.push({
          x: (this.playerX + e.x) / 2,
          y: (this.playerY + e.y) / 2 - 20,
          text: `GRAZE! +${grazePts}`,
          color: [254, 240, 138],
          alpha: 1,
          age: 0
        });
      }

      // Collision with Player
      if (dist < this.playerRadius * 0.82 + e.radius * 0.82) {'''

js = js.replace(old_collision, new_collision)
print('Added Near-Miss Graze mechanic!')

# 3. Add Haptics to Orb Collection & Combo Chord Stingers
old_orb_collect = '''if (pu.type === "SCORE_ORB") {
      this.playSfx("orb", 0.72);
      this.orbsCollectedRun++;
      this.combo = Math.min(8, this.combo + 1);
      if (this.combo > this.maxComboRun) this.maxComboRun = this.combo;'''

new_orb_collect = '''if (pu.type === "SCORE_ORB") {
      this.playSfx("orb", 0.72);
      this.triggerHaptic([18]);
      this.orbsCollectedRun++;
      const prevCombo = this.combo;
      this.combo = Math.min(8, this.combo + 1);
      if (this.combo > this.maxComboRun) this.maxComboRun = this.combo;
      if (this.combo >= 4 && this.combo > prevCombo) {
        this.synthComboChordStinger(this.combo);
      }'''

js = js.replace(old_orb_collect, new_orb_collect)
print('Added haptics and combo chord stingers!')

# 4. Interactive Vinyl Turntable Scratch / Spin on Menu
# Add turntable touch handlers into bindEvents
old_bind_events = '''// Pointer / Touch Controls on Game Canvas
    const updatePointer = (clientX, clientY) => {'''

new_bind_events = '''// Interactive Main Menu Turntable Scratch / Flick Controls
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
    const updatePointer = (clientX, clientY) => {'''

js = js.replace(old_bind_events, new_bind_events)
print('Added interactive turntable scratch and spin!')

# 5. Add turntable momentum damping and angle update in loop
old_loop_turntable = '''if (this.gameState === "MENU" && this.menuTurntableCtx) {
      const allGold = OFFICIAL_TRACKS.every((t) => (this.prefs.trackMasteryMedals[t.id] || 0) >= 3);
      drawMenuTurntable(this.menuTurntableCtx, this.images.logo, this.time, allGold);
    }'''

new_loop_turntable = '''if (this.turntableSpinVel) {
      this.turntableAngle = (this.turntableAngle || 0) + this.turntableSpinVel * dt;
      this.turntableSpinVel *= Math.pow(0.08, dt); // smooth vinyl damping
      if (Math.abs(this.turntableSpinVel) < 0.05) this.turntableSpinVel = 0;
    }

    if (this.gameState === "MENU" && this.menuTurntableCtx) {
      const allGold = OFFICIAL_TRACKS.every((t) => (this.prefs.trackMasteryMedals[t.id] || 0) >= 3);
      const effectiveTime = this.time + (this.turntableAngle || 0) * 0.45;
      drawMenuTurntable(this.menuTurntableCtx, this.images.logo, effectiveTime, allGold);
    }'''

js = js.replace(old_loop_turntable, new_loop_turntable)
print('Updated loop turntable inertia!')

# 6. Add Chromatic Aberration & Adrenaline Camera Glitch in renderGameCanvas
old_render_canvas_start = '''renderGameCanvas() {
    const ctx = this.ctx;
    ctx.save();

    if (this.screenShake > 0.2 && this.gameState === "PLAYING") {
      const sx = (Math.random() - 0.5) * this.screenShake;
      const sy = (Math.random() - 0.5) * this.screenShake;
      ctx.translate(sx, sy);
    }'''

new_render_canvas_start = '''renderGameCanvas() {
    const ctx = this.ctx;
    ctx.save();

    if (this.adrenalineGlitch > 0) {
      this.adrenalineGlitch = Math.max(0, this.adrenalineGlitch - 0.016 * 2.5);
    }

    if (this.screenShake > 0.2 && this.gameState === "PLAYING") {
      const sx = (Math.random() - 0.5) * this.screenShake;
      const sy = (Math.random() - 0.5) * this.screenShake;
      ctx.translate(sx, sy);
    }'''

js = js.replace(old_render_canvas_start, new_render_canvas_start)

# 7. Add Expanded Skins, Trails, Themes in syncCustomizerUI
old_customizer_items = '''const itemsByTab = [
      [
        { id: "skin_cyan", cat: "skin", name: "Cyber Cyan Core", sub: "Standard Resonance Core", price: 0, col: "#38BDF8" },
        { id: "skin_gold", cat: "skin", name: "Solar Gold Core", sub: "24K Harmonic Sunburst", price: 200, col: "#FACC15" },
        { id: "skin_prism", cat: "skin", name: "Prism Nova Core", sub: "Magenta Crystal Matrix", price: 450, col: "#EC4899" }
      ],
      [
        { id: "trail_cyan", cat: "trail", name: "Aether Ribbon", sub: "Cyan & Indigo Silk Trail", price: 0, col: "#38BDF8" },
        { id: "trail_gold", cat: "trail", name: "Solar Flare Wake", sub: "Liquid Gold Stardust", price: 250, col: "#FACC15" },
        { id: "trail_emerald", cat: "trail", name: "Aurora Emerald", sub: "Bioluminescent Synth Wave", price: 400, col: "#34D399" }
      ],
      [
        { id: "theme_nebula", cat: "theme", name: "Deep Space Nebula", sub: "Signature No More Illusions Stage", price: 0, col: "#6366F1" },
        { id: "theme_sunset", cat: "theme", name: "Sunset Horizon", sub: "Crimson & Gold Synthwave", price: 300, col: "#F43F5E" },
        { id: "theme_matrix", cat: "theme", name: "Cyber Grid", sub: "Emerald Quantum Matrix", price: 500, col: "#10B981" }
      ]
    ];'''

new_customizer_items = '''const itemsByTab = [
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
    ];'''

js = js.replace(old_customizer_items, new_customizer_items)
print('Added expanded skins, trails, and themes!')

# 8. Update getTrailPalette to support new trails
old_trail_pal = '''function getTrailPalette(trailId) {
  switch (trailId) {
    case "trail_gold": return [[250, 204, 21], [245, 158, 11]];
    case "trail_emerald": return [[52, 211, 153], [16, 185, 129]];
    default: return [[56, 189, 248], [129, 140, 248]];
  }
}'''

new_trail_pal = '''function getTrailPalette(trailId) {
  switch (trailId) {
    case "trail_gold": return [[250, 204, 21], [245, 158, 11]];
    case "trail_emerald": return [[52, 211, 153], [16, 185, 129]];
    case "trail_prism": return [[232, 121, 249], [192, 132, 252]];
    case "trail_laser": return [[34, 211, 238], [250, 204, 21]];
    default: return [[56, 189, 248], [129, 140, 248]];
  }
}'''

js = js.replace(old_trail_pal, new_trail_pal)

# 9. Update getHeroBitmap to support new skins
old_hero_bmp = '''getHeroBitmap(skinId = this.prefs.selectedSkin) {
    if (skinId === "skin_gold") return this.images.hero_gold;
    if (skinId === "skin_prism") return this.images.hero_prism;
    return this.images.hero_cyan;
  }'''

new_hero_bmp = '''getHeroBitmap(skinId = this.prefs.selectedSkin) {
    if (skinId === "skin_gold") return this.images.hero_gold;
    if (skinId === "skin_prism") return this.images.hero_prism;
    if (skinId === "skin_emerald") return this.images.hero_cyan;
    if (skinId === "skin_blood") return this.images.hero_gold;
    return this.images.hero_cyan;
  }'''

js = js.replace(old_hero_bmp, new_hero_bmp)

# 10. Update getThemePalette to support new themes
old_theme_pal = '''case "GOLD_VINYL":
      return {
        c1: isFocus ? "#080501" : "#050300",
        c2: isFocus ? "#1A1103" : "#120B02",
        c3: isFocus ? "#0E0902" : "#080501",
        bgTop: [8, 5, 1], bgMid: [26, 17, 3], bgBot: [14, 9, 2],
        nebTint: [250, 204, 21], starTint: [56, 189, 248]
      };
    default: // OBSIDIAN / theme_nebula'''

new_theme_pal = '''case "GOLD_VINYL":
    case "theme_gold":
      return {
        c1: isFocus ? "#0A0601" : "#060401",
        c2: isFocus ? "#241703" : "#181002",
        c3: isFocus ? "#140D02" : "#0B0701",
        bgTop: [10, 6, 1], bgMid: [36, 23, 3], bgBot: [20, 13, 2],
        nebTint: [250, 204, 21], starTint: [253, 224, 71]
      };
    case "BLOOD_MOON":
    case "theme_blood":
      return {
        c1: isFocus ? "#090103" : "#050002",
        c2: isFocus ? "#1C040A" : "#120206",
        c3: isFocus ? "#0E0208" : "#070104",
        bgTop: [9, 1, 3], bgMid: [28, 4, 10], bgBot: [14, 2, 8],
        nebTint: [244, 63, 94], starTint: [250, 204, 21]
      };
    default: // OBSIDIAN / theme_nebula'''

js = js.replace(old_theme_pal, new_theme_pal)

# 11. Add Liner Notes Modal methods
liner_methods = '''  openLinerNotes(trackIndex = this.selectedStageIndex) {
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
      track_01: "\\"Can I confide in you tonight?\\nNo more illusions to hide behind...\\nThrough the dark void and the frequency light,\\nWe break the signal and come alive.\\"",
      track_02: "\\"Started from zero now we taking flight,\\nNumber one frequency burning bright...\\nBassline knocking through the midnight air,\\nTell them No More Illusions is already there.\\""
    };

    if (storyEl) storyEl.textContent = stories[track.id] || stories.track_01;
    if (lyricsEl) lyricsEl.textContent = lyrics[track.id] || lyrics.track_01;

    this.gameState = "LINER_NOTES";
    this.syncUI();
  }

  closeLinerNotes() {
    this.playSfx("click");
    this.openScreen("CUSTOMIZER");
  }'''

js = js.replace('closeLinerNotes()', '') # in case
js = js.replace('openScreen(stateName) {', liner_methods + '\n\n  openScreen(stateName) {')

# Add LINER_NOTES to screens list
js = js.replace('"CUSTOMIZER", "COUNTDOWN"', '"CUSTOMIZER", "COUNTDOWN", "LINER_NOTES"')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'w', encoding='utf-8') as f:
    f.write(js)

print('Updated engine.js with all deep customization & alive feeling features!')
