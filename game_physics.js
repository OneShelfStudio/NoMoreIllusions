// ============================================================================
// NO MORE ILLUSIONS — RHYTHM PHYSICS, STATIONARY BEAT NODES & WORMHOLE SURF
// ============================================================================

Object.assign(NoMoreIllusionsApp.prototype, {
  stepPhysics(dt) {
    const stage = OFFICIAL_TRACKS[this.selectedStageIndex] || OFFICIAL_TRACKS[0];

    // Sync elapsed time from audio element if playing
    if (!this.trackAudio.paused && this.trackAudio.currentTime > 0) {
      this.trackElapsedSec = this.trackAudio.currentTime;
    } else {
      this.trackElapsedSec += dt;
    }

    if (this.trackElapsedSec >= this.activeTrackDurationSec - 0.15) {
      if (this.getRemixMode().id === "ENDLESS") {
        this.advanceEndlessLoop();
      } else {
        this.completeStageTrack();
        return;
      }
    }
    if (this.activeToast) {
      this.activeToast.timer -= dt;
      if (this.activeToast.timer <= 0) this.activeToast = null;
    }

    // Update Multi-Band State at exact timestamp
    this.multiBandState = getMultiBandStateAt(stage.id, this.trackElapsedSec);

    // Beat pulse @ 126 BPM
    const beatPeriod = 60 / stage.bpm;
    const beatPhase = (this.trackElapsedSec % beatPeriod) / beatPeriod;
    this.beatPulse = Math.pow(1 - beatPhase, 3);

    // Focus Mechanic
    const focusActive = this.isFocusHolding && this.focusCharge > 0.02;
    if (focusActive) {
      this.focusCharge = Math.max(0, this.focusCharge - dt * 0.42);
      if (this.focusCharge <= 0.02) this.isFocusHolding = false;
    } else {
      this.focusCharge = Math.min(1, this.focusCharge + dt * 0.18);
    }

    // Time Dilation
    const timeScale = focusActive ? 0.42 : (this.slowMoTimer > 0 ? 0.50 : 1.0);
    const worldDt = dt * timeScale;

    this.survivalSeconds += dt;
    if (this.shieldTimer > 0) this.shieldTimer = Math.max(0, this.shieldTimer - dt);
    if (this.slowMoTimer > 0) this.slowMoTimer = Math.max(0, this.slowMoTimer - dt);
    if (this.overdriveTimer > 0) this.overdriveTimer = Math.max(0, this.overdriveTimer - dt);
    if (this.magnetTimer > 0) this.magnetTimer = Math.max(0, this.magnetTimer - dt);
    if (this.eclipseModeTimer > 0) this.eclipseModeTimer = Math.max(0, this.eclipseModeTimer - dt);
    if (this.constellationRushTimer > 0) this.constellationRushTimer = Math.max(0, this.constellationRushTimer - dt);
    if (this.choreoBannerTimer > 0) this.choreoBannerTimer = Math.max(0, this.choreoBannerTimer - dt);
    if (this.screenShake > 0) this.screenShake = Math.max(0, this.screenShake - dt * 32);

    if (this.comboTimer > 0) {
      this.comboTimer -= dt;
      if (this.comboTimer <= 0) this.combo = 1;
    }

    // Score & Level Progression
    const mult = this.combo * (this.constellationRushTimer > 0 ? 2 : 1);
    this.score += Math.max(1, Math.round(dt * 18 * mult));
    const newLevel = Math.min(12, 1 + Math.floor(this.survivalSeconds / 15));
    if (newLevel > this.level) {
      this.level = newLevel;
      this.playSfx("levelup", 0.7);
      this.shockwaves.push({
        x: this.playerX,
        y: this.playerY,
        radius: 10,
        maxRadius: Math.max(this.W, this.H) * 0.55,
        color: [250, 204, 21],
        alpha: 1
      });
      this.floatingTexts.push({
        x: this.W / 2,
        y: this.H * 0.30,
        text: `PHASE ${this.level}`,
        color: [250, 204, 21],
        alpha: 1,
        age: 0
      });
    }

    // Player Spring-Damper Tracking (or Musical Slide-Note Wormhole Stream Surfing!)
    if (this.activeWormholeSlide) {
      const slide = this.activeWormholeSlide;
      slide.progress = Math.min(1.0, slide.progress + dt / slide.durationSec);
      const t = slide.progress;
      const inv = 1.0 - t;
      this.playerX = inv * inv * slide.startX + 2 * inv * t * slide.ctrlX + t * t * slide.endX;
      this.playerY = inv * inv * slide.startY + 2 * inv * t * slide.ctrlY + t * t * slide.endY;
      this.targetX = this.playerX;
      this.targetY = this.playerY;
      this.shieldTimer = Math.max(this.shieldTimer, 0.5);

      const expectedTicks = Math.min(5, Math.floor(t * 5));
      while (slide.ticksCollected < expectedTicks) {
        slide.ticksCollected++;
        const tickPts = 65 * this.combo;
        this.score += tickPts;
        this.orbsCollectedRun++;
        this.combo = Math.min(8, this.combo + 1);
        if (this.combo > this.maxComboRun) this.maxComboRun = this.combo;
        this.comboTimer = 4.5;
        this.playSfx("orb", 0.72);
        this.triggerHaptic([14]);
        this.spawnBurstMotes(this.playerX, this.playerY, [34, 211, 238], 8);
        this.floatingTexts.push({
          x: this.playerX,
          y: this.playerY - 18,
          text: `SLIDE ♪ +${tickPts}`,
          color: [34, 211, 238],
          alpha: 1,
          age: 0
        });
      }

      if (slide.progress >= 1.0) {
        const finPts = 150 * this.combo;
        this.score += finPts;
        this.shieldTimer = Math.max(this.shieldTimer, 1.8);
        this.screenShake = Math.max(this.screenShake, 8);
        this.playSfx("powerup", 0.80);
        this.shockwaves.push({
          x: this.playerX,
          y: this.playerY,
          radius: 10,
          maxRadius: 220,
          color: [250, 204, 21],
          alpha: 1
        });
        this.floatingTexts.push({
          x: this.playerX,
          y: this.playerY - 26,
          text: `SLIDE DROP! +${finPts}`,
          color: [250, 204, 21],
          alpha: 1,
          age: 0
        });
        this.activeWormholeSlide = null;
      }
    } else {
      const followFactor = 1 - Math.pow(0.0001, dt);
      this.playerX += (this.targetX - this.playerX) * followFactor;
      this.playerY += (this.targetY - this.playerY) * followFactor;
    }

    // Update Player Trail
    for (let i = this.trailPoints.length - 1; i > 0; i--) {
      this.trailPoints[i].x = this.trailPoints[i - 1].x;
      this.trailPoints[i].y = this.trailPoints[i - 1].y;
    }
    this.trailPoints[0].x = this.playerX;
    this.trailPoints[0].y = this.playerY;
    if (this.trailLength < this.trailPoints.length) this.trailLength++;

    // 1. Pre-Mapped Beat-Orb Chart Spawner (Stationary 1 -> 2 -> 3 -> 4 Phrase Chains with Shrinking Approach Ring!)
    // Spawn up to 4 upcoming beat nodes ~2.5 beats ahead of their target beat so players see the sequence in advance.
    const chart = stage.orbChart;
    const spawnLeadTime = beatPeriod * 2.5;
    while (
      this.nextBeatCueIndex < chart.length &&
      this.powerUps.length < 4 &&
      (chart[this.nextBeatCueIndex].timestampSec || chart[this.nextBeatCueIndex].timeSec) <= this.trackElapsedSec + spawnLeadTime
    ) {
      const cue = chart[this.nextBeatCueIndex];
      this.nextBeatCueIndex++;
      this.spawnChartedOrb(cue, beatPeriod);
    }

    // 2. Choreographed Section Director (1:1 with Android TrackSection table)
    const allSections = stage.sections || stage.choreography;
    let activeSec = allSections[0];
    for (let s = 0; s < allSections.length; s++) {
      if ((allSections[s].timeSec ?? allSections[s].t ?? 0) <= this.trackElapsedSec) {
        activeSec = allSections[s];
      }
    }
    const activeSecTime = activeSec.timeSec ?? activeSec.t ?? 0;
    if (activeSecTime !== this.lastTriggeredSectionTime) {
      this.lastTriggeredSectionTime = activeSecTime;
      this.choreoBannerText = activeSec.title;
      this.choreoBannerSubtext = activeSec.subtitle;
      this.choreoBannerTimer = 2.8;

      // If entering a Hook Vocal Float, Pre-Drop Hush, or Victory Outro, dissolve all active enemies into stardust!
      if (activeSec.clearEnemiesOnEnter) {
        this.telegraphs = [];
        this.wormholes = [];
        this.orbitalPhenomenon = null;
        this.satellites = [];
        for (let eIdx = 0; eIdx < this.enemies.length; eIdx++) {
          const en = this.enemies[eIdx];
          this.spawnBurstMotes(en.x, en.y, [56, 189, 248], 10);
          this.shockwaves.push({
            x: en.x,
            y: en.y,
            radius: 6,
            maxRadius: 90,
            color: [56, 189, 248],
            alpha: 1
          });
        }
        this.enemies = [];
      }

      if (activeSec.effectType === "CONSTELLATION_RUSH") {
        this.constellationRushTimer = 14.0;
      } else if (activeSec.effectType === "ECLIPSE_VORTEX") {
        this.eclipseModeTimer = 8.0;
      } else if (activeSec.effectType === "SUPERNOVA_BURST") {
        this.constellationRushTimer = 15.0;
      } else if (activeSec.effectType === "OVERDRIVE_DROP") {
        this.overdriveTimer = Math.max(this.overdriveTimer, 4.5);
      }

      if (activeSec.juiceTier === "EXTRA_JUICE") {
        this.screenShake = Math.max(this.screenShake, 10);
        this.shockwaves.push({
          x: this.W / 2,
          y: this.H / 2,
          radius: 15,
          maxRadius: Math.max(this.W, this.H) * 0.78,
          color: [250, 204, 21],
          alpha: 1
        });
      } else if (activeSecTime > 0) {
        this.shockwaves.push({
          x: this.W / 2,
          y: this.H / 2,
          radius: 12,
          maxRadius: Math.max(this.W, this.H) * 0.45,
          color: [56, 189, 248],
          alpha: 1
        });
      }
    }

    // 808 Sub-Bass Heartbeat Pulse during Confide's 3:21–3:27.5 Breakdown
    if (this.selectedStageIndex === 0 && this.trackElapsedSec >= 201.0 && this.trackElapsedSec <= 207.5 && beatPhase <= 0.14 && this.shockwaves.length < 6) {
      this.screenShake = Math.max(this.screenShake, 5);
      this.shockwaves.push({
        x: this.W / 2,
        y: this.H * 0.46,
        radius: 14,
        maxRadius: Math.max(this.W, this.H) * 0.62,
        color: [250, 204, 21],
        alpha: 1
      });
    }

    // 3. Support Power-Ups (Nova Blast, Shield, Slow-Mo, Magnet) arrive via the beat chart (1:1 with Android MusicLibrary.kt)

    // 4. Musical Slide-Note Wormhole Spawner (Paired Entry -> Curved Bezier Ribbon -> Exit Stream)
    if (activeSec.allowAnomalies) {
      this.wormholeSpawnTimer += dt;
      if (this.wormholeSpawnTimer >= 12.5 && this.wormholes.length === 0) {
        this.wormholeSpawnTimer = 0;
        const leftToRight = Math.random() < 0.5;
        const ax = this.W * (leftToRight ? (0.20 + Math.random() * 0.12) : (0.68 + Math.random() * 0.12));
        const ay = this.H * (0.28 + Math.random() * 0.16);
        const bx = this.W * (leftToRight ? (0.68 + Math.random() * 0.12) : (0.20 + Math.random() * 0.12));
        const by = this.H * (0.56 + Math.random() * 0.16);
        const midX = (ax + bx) * 0.5;
        const midY = (ay + by) * 0.5;
        const ctrlX = Math.max(this.W * 0.18, Math.min(this.W * 0.82, midX + (leftToRight ? 1 : -1) * (this.W * 0.12)));
        const ctrlY = Math.max(this.H * 0.22, Math.min(this.H * 0.78, midY - this.H * 0.12));

        this.wormholes.push({
          ax, ay, bx, by, ctrlX, ctrlY,
          radius: 36,
          age: 0,
          maxLifetime: 10.5,
          teleportCooldown: 0
        });
      }
    }

    // 5. Clear legacy hazardous satellites so rhythm stays front-and-center
    this.orbitalPhenomenon = null;
    this.satellites = [];

    // 6. 1:1 Telegraphed Enemy Spawner (Tamed in Normal mode so rhythm stays primary!)
    const sectionMax = activeSec.maxEnemies ?? 2;
    if (sectionMax <= 0 || this.multiBandState.bassDropOut) {
      this.telegraphs = [];
    } else {
      // Step active telegraphs
      for (let i = this.telegraphs.length - 1; i >= 0; i--) {
        const tg = this.telegraphs[i];
        tg.timer -= worldDt;
        if (tg.timer <= 0) {
          this.enemies.push({
            x: tg.edgeX,
            y: tg.edgeY,
            vx: Math.cos(tg.targetAngle) * tg.speed,
            vy: Math.sin(tg.targetAngle) * tg.speed,
            radius: tg.type === "PULSAR" ? 21 : 17,
            type: tg.type,
            phase: Math.random() * 6.28
          });
          this.shockwaves.push({
            x: tg.edgeX,
            y: tg.edgeY,
            radius: 4,
            maxRadius: 85,
            color: [255, 30, 66],
            alpha: 1
          });
          this.telegraphs.splice(i, 1);
        }
      }

      const mode = this.getRemixMode();
      const maxEnemies = Math.min(6, Math.max(1, Math.round(sectionMax * (mode.speed >= 1.15 ? 1.25 : 1.0))));
      const sectionUrgency = sectionMax >= 3 ? 0.92 : 0.65;
      this.spawnTimer += worldDt * sectionUrgency * (mode.speed || 1.0);
      const spawnCooldown = Math.max(1.25, 2.10 - (sectionMax * 0.18));
      const canSpawn = (this.enemies.length + this.telegraphs.length < maxEnemies) &&
                       (this.beatPulse > 0.42 || this.spawnTimer >= 1.85);

      if (canSpawn && this.spawnTimer >= spawnCooldown) {
        this.spawnTimer = 0;
        this.initiateEnemyTelegraph(activeSec.enemySpeedMult || 0.80);
      }
    }

    // 7. Update Musical Slide-Note Wormholes & Trigger Auto-Surfing Slide Stream
    for (let i = this.wormholes.length - 1; i >= 0; i--) {
      const wh = this.wormholes[i];
      wh.age += dt;
      if (wh.teleportCooldown > 0) wh.teleportCooldown = Math.max(0, wh.teleportCooldown - dt);
      if (wh.age >= wh.maxLifetime) {
        this.wormholes.splice(i, 1);
        continue;
      }
      if (!this.activeWormholeSlide && wh.teleportCooldown <= 0 && wh.age > 0.4 && wh.age < wh.maxLifetime - 0.5) {
        const distA = Math.hypot(this.playerX - wh.ax, this.playerY - wh.ay);
        const distB = Math.hypot(this.playerX - wh.bx, this.playerY - wh.by);
        const triggerR = wh.radius + this.playerRadius * 0.85;
        if (distA < triggerR) {
          this.startWormholeSlide(wh, wh.ax, wh.ay, wh.bx, wh.by);
        } else if (distB < triggerR) {
          this.startWormholeSlide(wh, wh.bx, wh.by, wh.ax, wh.ay);
        }
      }
    }

    // 8. Update Stationary Sequential Beat Nodes (with Time-To-Live Expiration -> Combo Reset Only!)
    for (let i = this.powerUps.length - 1; i >= 0; i--) {
      const pu = this.powerUps[i];
      pu.age += worldDt;

      // Check Time-To-Live (TTL) expiration
      const isExpiredByTtl = pu.expireBeatSec > 0 && this.trackElapsedSec > pu.expireBeatSec;
      if (isExpiredByTtl || pu.age >= pu.maxLifetime) {
        if (isExpiredByTtl) {
          // Missed beatbefore TTL expired: ONLY reset combo (never damages player or ends run!)
          if (this.combo > 1) this.combo = 1;
          this.comboTimer = 0;
          this.shockwaves.push({
            x: pu.x,
            y: pu.y,
            radius: 8,
            maxRadius: 68,
            color: [251, 113, 133],
            alpha: 1
          });
          this.floatingTexts.push({
            x: pu.x,
            y: pu.y - 12,
            text: "MISS • COMBO RESET",
            color: [251, 113, 133],
            alpha: 1,
            age: 0
          });
        }
        this.powerUps.splice(i, 1);
        continue;
      }

      // Stationary Beat Node: stays anchored at (targetX, targetY) with a tiny 2.4px musical hover
      if (typeof pu.targetX === "number" && typeof pu.targetY === "number") {
        const hover = 2.4;
        pu.x = pu.targetX + Math.cos(this.time * 3.0 + (pu.phase || 0)) * hover;
        pu.y = pu.targetY + Math.sin(this.time * 3.6 + (pu.phase || 0)) * hover;
      }

      const dx = this.playerX - pu.x;
      const dy = this.playerY - pu.y;
      const dist = Math.max(1, Math.hypot(dx, dy));

      // Pull toward hero when Magnet power-up or Tractor Reach is active
      if (this.magnetTimer > 0 || pu.keyWordEffect === "TRACTOR_REACH") {
        const magnetRange = 290;
        if (dist < magnetRange) {
          const pullStrength = (1 - dist / magnetRange) * 580;
          pu.x += (dx / dist) * pullStrength * dt;
          pu.y += (dy / dist) * pullStrength * dt;
          pu.targetX = pu.x;
          pu.targetY = pu.y;
        }
      }

      // Generous Collection Check for responsive sequential note hits
      const collectRadius = this.playerRadius + (pu.type === "SCORE_ORB" ? pu.radius * 1.38 : pu.radius * 1.22);
      if (dist <= collectRadius) {
        this.collectPowerUp(pu);
        this.powerUps.splice(i, 1);
      }
    }

    // 9. Update Enemies (with 135px Beat-Node Repulsion Buffer so enemies never camp on notes!)
    for (let i = this.enemies.length - 1; i >= 0; i--) {
      const e = this.enemies[i];
      if (e.type === "SEEKER") {
        const dx = this.playerX - e.x;
        const dy = this.playerY - e.y;
        const d = Math.max(1, Math.hypot(dx, dy));
        const curSpeed = Math.hypot(e.vx, e.vy);
        e.vx += (dx / d) * 195 * worldDt;
        e.vy += (dy / d) * 195 * worldDt;
        const newSpeed = Math.max(1, Math.hypot(e.vx, e.vy));
        e.vx = (e.vx / newSpeed) * curSpeed;
        e.vy = (e.vy / newSpeed) * curSpeed;
      } else if (e.type === "WANDERER") {
        e.vx += Math.sin(this.time * 3.8 + e.phase) * 105 * worldDt;
        e.vy += Math.cos(this.time * 3.8 + e.phase) * 105 * worldDt;
      }

      // Repel enemies away from all active stationary beat nodes so players always have a clean path!
      for (let bIdx = 0; bIdx < this.powerUps.length; bIdx++) {
        const orb = this.powerUps[bIdx];
        const bdx = e.x - orb.x;
        const bdy = e.y - orb.y;
        const bDist = Math.hypot(bdx, bdy);
        const safeZone = 135;
        if (bDist > 1 && bDist < safeZone) {
          const push = ((safeZone - bDist) / safeZone) * 420 * worldDt;
          e.vx += (bdx / bDist) * push;
          e.vy += (bdy / bDist) * push;
        }
      }

      const drumCutScale = this.multiBandState.bass < 0.16 ? 0.42 : 1.0;
      const bassBoost = (1 + this.multiBandState.bass * 0.34) * drumCutScale;
      e.x += e.vx * bassBoost * worldDt;
      e.y += e.vy * bassBoost * worldDt;

      // Despawn far off-screen
      if (e.x < -160 || e.x > this.W + 160 || e.y < -160 || e.y > this.H + 160) {
        this.enemies.splice(i, 1);
        continue;
      }

      // Near-Miss Adrenaline Graze Check (passing within a hair's breadth of an enemy!)
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
      if (dist < this.playerRadius * 0.82 + e.radius * 0.82) {
        if (this.overdriveTimer > 0) {
          // Vaporize enemy in Overdrive (does NOT end Overdrive)!
          this.playSfx("orb", 0.75);
          this.triggerHaptic([30]);
          const vapPts = 150 * Math.max(2, this.combo);
          this.score += vapPts;
          this.prefs.points += 4;
          this.screenShake = Math.max(this.screenShake, 9);
          this.spawnBurstMotes(e.x, e.y, [250, 204, 21], 16);
          this.shockwaves.push({ x: e.x, y: e.y, radius: 6, maxRadius: 190, color: [250, 204, 21], alpha: 1 });
          this.showToast("💥", `ENEMY VAPORIZED! +${vapPts}`, `OVERDRIVE RAMMING ACTIVE (${this.overdriveTimer.toFixed(1)}s)`, "#FACC15", 1.6);
          this.floatingTexts.push({
            x: e.x,
            y: e.y,
            text: `VAPORIZED! +${vapPts}`,
            color: [250, 204, 21],
            alpha: 1,
            age: 0
          });
          this.enemies.splice(i, 1);
        } else if (this.shieldTimer > 0) {
          this.playSfx("shield", 0.8);
          this.shieldTimer = 0;
          this.screenShake = 14;
          this.showToast("🛡️", "SHIELD ABSORBED HIT!", "PROTECTIVE BARRIER SAVED YOUR RUN", "#38BDF8", 2.0);
          this.shockwaves.push({
            x: this.playerX,
            y: this.playerY,
            radius: 10,
            maxRadius: 240,
            color: [56, 189, 248],
            alpha: 1
          });
          this.floatingTexts.push({
            x: this.playerX,
            y: this.playerY - 30,
            text: "SHIELD ABSORBED!",
            color: [56, 189, 248],
            alpha: 1,
            age: 0
          });
          this.enemies.splice(i, 1);
        } else {
          this.screenShake = 20;
          this.triggerGameOver();
          return;
        }
      }
    }

    // 10. Check Satellite Collisions
    for (let i = this.satellites.length - 1; i >= 0; i--) {
      const sat = this.satellites[i];
      const dist = Math.hypot(this.playerX - sat.x, this.playerY - sat.y);
      if (dist < this.playerRadius * 0.8 + sat.radius * 0.8) {
        if (this.overdriveTimer > 0 || this.shieldTimer > 0) {
          this.shieldTimer = 0;
          this.satellites.splice(i, 1);
        } else {
          this.triggerGameOver();
          return;
        }
      }
    }

    // 11. Update Particles, Shockwaves & Floating Texts
    for (let i = this.motes.length - 1; i >= 0; i--) {
      const m = this.motes[i];
      m.x += m.vx * dt;
      m.y += m.vy * dt;
      m.alpha -= dt * 1.4;
      if (m.alpha <= 0) this.motes.splice(i, 1);
    }

    for (let i = this.shockwaves.length - 1; i >= 0; i--) {
      const sw = this.shockwaves[i];
      sw.radius += (sw.maxRadius - sw.radius) * Math.min(1, dt * 6.5) + dt * 120;
      if (sw.radius >= sw.maxRadius * 0.98) this.shockwaves.splice(i, 1);
    }

    for (let i = this.floatingTexts.length - 1; i >= 0; i--) {
      const ft = this.floatingTexts[i];
      ft.age += dt;
      ft.y -= dt * 42;
      ft.alpha = Math.max(0, 1 - ft.age / 1.15);
      if (ft.alpha <= 0) this.floatingTexts.splice(i, 1);
    }

    this.updateLiveHUD();
  },

  spawnChartedOrb(cue, beatPeriod = 0.48) {
    const isSupernova = cue.isGoldenCue || (cue.keyWordEffect && cue.keyWordEffect !== "NONE");
    const targetX = (cue.normX || 0.5) * this.W;
    const targetY = (cue.normY || 0.5) * this.H;

    // Stationary Beat Node: spawns directly at its choreographed screen coordinate (targetX, targetY)
    // with an Osu!-style shrinking approach ring and a Time-To-Live (TTL) expiration window!
    const cueType = (!isSupernova && cue.powerUpType && cue.powerUpType !== "SCORE_ORB") ? cue.powerUpType : "SCORE_ORB";
    const targetBeatSec = cue.timestampSec || this.trackElapsedSec;
    const ttlGraceSec = Math.max(0.45, beatPeriod * 0.92);
    const expireBeatSec = targetBeatSec + ttlGraceSec;
    const seqIdx = cue.sequenceIndex || (this.powerUps.length + 1);
    const phraseBeatNum = ((seqIdx - 1) % 4) + 1;

    this.powerUps.push({
      x: targetX,
      y: targetY,
      targetX,
      targetY,
      vx: 0,
      vy: 0,
      speed: 0,
      angle: 0,
      radius: isSupernova ? 28 : 23,
      type: cueType,
      keyWordEffect: cue.keyWordEffect || (cue.isGoldenCue ? "CONSTELLATION_STAR" : "NONE"),
      isSupernova,
      sequenceIndex: seqIdx,
      phraseBeatNum,
      targetBeatSec,
      spawnBeatSec: this.trackElapsedSec,
      expireBeatSec,
      age: 0,
      maxLifetime: Math.max(2.2, (expireBeatSec - this.trackElapsedSec) + 0.35),
      phase: Math.random() * 6.28,
      waveFreq: 2.4 + Math.random() * 1.2,
      wavePhase: Math.random() * 6.28
    });
  },

  spawnTravelingCollectible(type, laneRatio, speed) {
    const edge = Math.floor(Math.random() * 4);
    const margin = 36;
    let x, y;
    if (edge === 0) { // Top
      x = margin + laneRatio * Math.max(40, this.W - margin * 2);
      y = -24;
    } else if (edge === 1) { // Bottom
      x = margin + laneRatio * Math.max(40, this.W - margin * 2);
      y = this.H + 24;
    } else if (edge === 2) { // Left
      x = -24;
      y = margin + laneRatio * Math.max(40, this.H - margin * 2);
    } else { // Right
      x = this.W + 24;
      y = margin + laneRatio * Math.max(40, this.H - margin * 2);
    }

    const aimX = this.W * (0.28 + Math.random() * 0.44);
    const aimY = this.H * (0.28 + Math.random() * 0.44);
    const angle = Math.atan2(aimY - y, aimX - x);

    this.powerUps.push({
      x, y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      radius: 17,
      type,
      age: 0,
      maxLifetime: type === "SCORE_ORB" ? 9.5 : 10.5,
      wavePhase: Math.random() * 6.28,
      waveFreq: 2.4 + Math.random() * 1.4
    });
  },

  collectPowerUp(pu) {
    if (pu.type === "SCORE_ORB") {
      this.playSfx("orb", 0.72);
      this.triggerHaptic([18]);
      this.orbsCollectedRun++;
      const prevCombo = this.combo;
      this.combo = Math.min(8, this.combo + 1);
      if (this.combo > this.maxComboRun) this.maxComboRun = this.combo;
      if (this.combo >= 4 && this.combo > prevCombo) {
        this.synthComboChordStinger(this.combo);
      }
      this.comboTimer = 4.5;

      // Timing Accuracy Check: compare current track time against the beat's targetBeatSec!
      const beatDiff = Math.abs(this.trackElapsedSec - (pu.targetBeatSec || this.trackElapsedSec));
      const isPerfect = beatDiff <= 0.22 || this.beatPulse > 0.45;
      const basePts = isPerfect ? 100 : 50;
      const pts = basePts * this.combo * (this.constellationRushTimer > 0 ? 2 : 1);
      this.score += pts;

      this.spawnBurstMotes(pu.x, pu.y, isPerfect ? [74, 222, 128] : [250, 204, 21], 14);
      this.shockwaves.push({
        x: pu.x,
        y: pu.y,
        radius: 8,
        maxRadius: isPerfect ? 145 : 95,
        color: isPerfect ? [74, 222, 128] : [250, 204, 21],
        alpha: 1
      });
      this.floatingTexts.push({
        x: pu.x,
        y: pu.y - 16,
        text: isPerfect ? `PERFECT! +${pts} (x${this.combo})` : `GOOD! +${pts}`,
        color: isPerfect ? [74, 222, 128] : [250, 204, 21],
        alpha: 1,
        age: 0
      });

      // Supernova keyword effects (1:1 with Android GameScreen.kt)
      const eff = pu.keyWordEffect || "NONE";
      if (pu.isSupernova || eff !== "NONE") {
        this.screenShake = Math.max(this.screenShake, 9);
        if (eff === "CONSTELLATION_STAR") {
          this.overdriveTimer = Math.max(this.overdriveTimer, 8.0);
          this.constellationRushTimer = Math.max(this.constellationRushTimer, 12.0);
          this.showToast("★", "24K OVERDRIVE STAR!", "8s ENEMY VAPORIZE + 2x RUSH", "#FACC15", 2.8);
        } else if (eff === "MAGNETAR_WAR") {
          let n = 0;
          for (let k = this.enemies.length - 1; k >= 0; k--) {
            const en = this.enemies[k];
            if (Math.hypot(en.x - this.playerX, en.y - this.playerY) < 420) {
              this.spawnBurstMotes(en.x, en.y, [253, 224, 71], 8);
              this.enemies.splice(k, 1);
              n++;
            }
          }
          this.showToast("💥", "MAGNETAR SHOCKWAVE!", `${n} ENEMIES VAPORIZED`, "#FB7185", 2.6);
        } else if (eff === "ECLIPSE_DARK") {
          this.shieldTimer = Math.max(this.shieldTimer, 8.5);
          this.showToast("🛡️", "SOLAR ECLIPSE SHIELD!", "8.5s INVINCIBILITY BARRIER", "#FDE047", 2.5);
        } else if (eff === "TRACTOR_REACH") {
          this.magnetTimer = Math.max(this.magnetTimer, 6.0);
          this.showToast("🧲", "TRACTOR REACH!", "PULLING ORBS TOWARD YOU", "#22D3EE", 2.5);
        } else if (eff === "PRISM_SHATTER") {
          this.showToast("💎", "PRISM SHATTER!", "BONUS RESONANCE POINTS", "#E879F9", 2.5);
        } else if (eff === "BINARY_HEART") {
          this.constellationRushTimer = Math.max(this.constellationRushTimer, 12.0);
          this.showToast("💚", "HARMONIC PULSE!", "2x SCORE RUSH • 12s", "#4ADE80", 2.5);
        }
      }
    } else {
      this.playSfx("powerup", 0.8);
      this.triggerHaptic([30]);
      let col = [56, 189, 248];
      if (pu.type === "BLAST" || pu.type === "OVERDRIVE") {
        col = [249, 115, 22];
        this.overdriveTimer = Math.max(this.overdriveTimer, 5.5);
        this.screenShake = Math.max(this.screenShake, 14);
        let vaporized = 0;
        for (let k = 0; k < this.enemies.length; k++) {
          const en = this.enemies[k];
          vaporized++;
          this.score += 100 * this.combo;
          this.spawnBurstMotes(en.x, en.y, k % 2 === 0 ? [250, 204, 21] : [249, 115, 22], 10);
          this.shockwaves.push({ x: en.x, y: en.y, radius: 6, maxRadius: 130, color: [250, 204, 21], alpha: 1 });
        }
        this.enemies = [];
        this.telegraphs = [];
        this.shockwaves.push({ x: pu.x, y: pu.y, radius: 10, maxRadius: 520, color: col, alpha: 1 });
        this.showToast("💥", "NOVA BLAST!",
          vaporized > 0 ? `${vaporized} ENEMIES VAPORIZED + 5.5s OVERDRIVE!` : "5.5s OVERDRIVE • RAM ENEMIES TO VAPORIZE!",
          "#F97316", 2.6);
      } else if (pu.type === "SHIELD") {
        this.shieldTimer = Math.max(this.shieldTimer, 7.0);
        col = [56, 189, 248];
        this.showToast("🛡️", "AEGIS SHIELD!", "ABSORBS NEXT COLLISION • 7.0s ACTIVE", "#38BDF8", 2.4);
      } else if (pu.type === "SLOW_MO") {
        this.slowMoTimer = Math.max(this.slowMoTimer, 5.5);
        col = [168, 85, 247];
        this.showToast("⏳", "CHRONO SHIFT!", "ENEMIES SLOWED 50% • 5.5s ACTIVE", "#A855F7", 2.4);
      } else if (pu.type === "MAGNET") {
        this.magnetTimer = Math.max(this.magnetTimer, 7.0);
        col = [16, 185, 129];
        this.showToast("🧲", "ORB MAGNET!", "PULLING NEARBY ORBS • 7.0s ACTIVE", "#10B981", 2.4);
      }
      this.spawnBurstMotes(pu.x, pu.y, col, 16);
      this.shockwaves.push({
        x: pu.x,
        y: pu.y,
        radius: 10,
        maxRadius: 175,
        color: col,
        alpha: 1
      });
    }
  },

  // Candy-Catcher-Style Top-Center Pickup Toast (1:1 with Android GameHUD.kt PowerUpBannerToast)
  showToast(icon, title, subtitle, accentHex, duration = 2.4) {
    this.activeToast = { icon, title, subtitle, accent: accentHex, timer: duration, maxTimer: duration };
    const el = document.getElementById("hud-pickup-toast");
    if (el) {
      el.classList.remove("toast-pop");
      void el.offsetWidth; // restart bounce animation
      el.classList.add("toast-pop");
    }
  },

  startWormholeSlide(wh, startX, startY, endX, endY) {
    this.playSfx("powerup", 0.78);
    this.triggerHaptic([22]);
    wh.teleportCooldown = 2.2;
    this.shieldTimer = Math.max(this.shieldTimer, 1.8);
    this.activeWormholeSlide = {
      startX,
      startY,
      ctrlX: (typeof wh.ctrlX === "number" && wh.ctrlX !== 0) ? wh.ctrlX : (startX + endX) * 0.5,
      ctrlY: (typeof wh.ctrlY === "number" && wh.ctrlY !== 0) ? wh.ctrlY : (startY + endY) * 0.5 - 60,
      endX,
      endY,
      progress: 0,
      durationSec: 0.85,
      ticksCollected: 0
    };
    this.shockwaves.push({
      x: startX,
      y: startY,
      radius: 10,
      maxRadius: 185,
      color: [34, 211, 238],
      alpha: 1
    });
    this.floatingTexts.push({
      x: startX,
      y: startY - 22,
      text: "SLIDE STREAM! ♪",
      color: [34, 211, 238],
      alpha: 1,
      age: 0
    });
  },

  spawnOrbitalPhenomenon(type) {
    const cx = this.W * (0.30 + Math.random() * 0.40);
    const cy = this.H * (0.28 + Math.random() * 0.36);
    this.orbitalPhenomenon = {
      x: cx,
      y: cy,
      radius: 38,
      type,
      age: 0,
      maxLifetime: 11.0
    };
    this.satellites = [];
    const satCount = 3;
    for (let i = 0; i < satCount; i++) {
      const orbitRadius = 85 + i * 34;
      const angleRad = i * ((Math.PI * 2) / satCount);
      this.satellites.push({
        x: cx + Math.cos(angleRad) * orbitRadius,
        y: cy + Math.sin(angleRad) * orbitRadius,
        radius: 13,
        orbitRadius,
        angleRad,
        angularSpeed: (i % 2 === 0 ? 1 : -1) * (1.6 - i * 0.25),
        isDebris: type === "BLACK_HOLE"
      });
    }
  },

  initiateEnemyTelegraph(sectionSpeedMult = 1.0) {
    const edge = Math.floor(Math.random() * 4);
    let edgeX, edgeY;
    if (edge === 0) { edgeX = 36 + Math.random() * (this.W - 72); edgeY = 24; }
    else if (edge === 1) { edgeX = 36 + Math.random() * (this.W - 72); edgeY = this.H - 24; }
    else if (edge === 2) { edgeX = 24; edgeY = 80 + Math.random() * (this.H - 160); }
    else { edgeX = this.W - 24; edgeY = 80 + Math.random() * (this.H - 160); }

    const mode = this.getRemixMode();
    const isTrackTwo = this.selectedStageIndex >= 1;
    const trackDiffMult = isTrackTwo ? 1.15 : 1.0;

    // Target angle aims toward player's current position with tight lead
    const aimAngle = Math.atan2(this.playerY - edgeY, this.playerX - edgeX) + (Math.random() - 0.5) * 0.22;
    const roll = Math.random();
    const type = roll < 0.38 ? "PULSAR" : (roll < 0.76 ? "SEEKER" : "WANDERER");
    const speed = (178 + this.level * 14) * (type === "SEEKER" ? 1.12 : 0.98) * trackDiffMult * sectionSpeedMult * (mode.speed || 1.0);

    this.telegraphs.push({
      edgeX,
      edgeY,
      targetAngle: aimAngle,
      speed,
      type,
      timer: 0.80, // Crisp warning before launch
      maxTimer: 0.80
    });
  },

  spawnEnemy() {
    const edge = Math.floor(Math.random() * 4);
    let x, y;
    if (edge === 0) { x = Math.random() * this.W; y = -45; }
    else if (edge === 1) { x = Math.random() * this.W; y = this.H + 45; }
    else if (edge === 2) { x = -45; y = Math.random() * this.H; }
    else { x = this.W + 45; y = Math.random() * this.H; }

    const roll = Math.random();
    const type = roll < 0.36 ? "PULSAR" : roll < 0.68 ? "SEEKER" : "WANDERER";
    const baseSpeed = (145 + this.level * 14) * (type === "SEEKER" ? 1.08 : 0.95);
    const angle = Math.atan2(this.playerY - y, this.playerX - x) + (Math.random() - 0.5) * 0.35;

    this.enemies.push({
      x, y,
      vx: Math.cos(angle) * baseSpeed,
      vy: Math.sin(angle) * baseSpeed,
      radius: type === "PULSAR" ? 21 : 19,
      type,
      phase: Math.random() * 6.28
    });
  },

  spawnBurstMotes(x, y, color, count = 12) {
    for (let i = 0; i < count; i++) {
      const a = Math.random() * Math.PI * 2;
      const spd = 60 + Math.random() * 190;
      this.motes.push({
        x, y,
        vx: Math.cos(a) * spd,
        vy: Math.sin(a) * spd,
        radius: 2.2 + Math.random() * 3.2,
        color,
        alpha: 1
      });
    }
  },


  // ==========================================================================
  // RENDERING & UI SYNCHRONIZATION
  // ==========================================================================
});
