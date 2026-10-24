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

    // Player Spring-Damper Tracking (100% Free Steering — Never Hijacked!)
    const followFactor = 1 - Math.pow(0.0001, dt);
    this.playerX += (this.targetX - this.playerX) * followFactor;
    this.playerY += (this.targetY - this.playerY) * followFactor;

    // Update Player Trail
    for (let i = this.trailPoints.length - 1; i > 0; i--) {
      this.trailPoints[i].x = this.trailPoints[i - 1].x;
      this.trailPoints[i].y = this.trailPoints[i - 1].y;
    }
    this.trailPoints[0].x = this.playerX;
    this.trailPoints[0].y = this.playerY;
    if (this.trailLength < this.trailPoints.length) this.trailLength++;

    // 1. Band Hero / Guitar Hero Scrolling Highway Note Spawner
    const chart = stage.orbChart;
    const spawnLeadTime = Math.max(1.75, Math.min(2.35, beatPeriod * 3.8));
    while (
      this.nextBeatCueIndex < chart.length &&
      this.powerUps.length < 10 &&
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

    // 3. Simplified Pickups (Fret Beat Notes, ★ 4X STAR, 💥 BOMB, 🛡️ SHIELD) arrive via the beat highway chart

    // 4. Guitar-Hero Sustain Slide Ribbon Spawner (Scrolling Hold-and-Slide Ribbon down the Rhythm Highway)
    if (activeSec.allowAnomalies) {
      this.wormholeSpawnTimer += dt;
      if (this.wormholeSpawnTimer >= 11.5 && this.wormholes.length === 0) {
        this.wormholeSpawnTimer = 0;
        const startLane = Math.floor(Math.random() * 4);
        const shift = Math.random() < 0.5 ? -1 : 1;
        const endLane = Math.max(0, Math.min(3, startLane + shift));

        const headPt = typeof this.computeHighwayPoint === "function"
          ? this.computeHighwayPoint(startLane, 0, this.W, this.H)
          : { x: this.W * 0.5, y: this.H * 0.16 };
        const tailPt = typeof this.computeHighwayPoint === "function"
          ? this.computeHighwayPoint(endLane, -0.58, this.W, this.H)
          : { x: this.W * 0.5, y: this.H * 0.08 };

        this.wormholes.push({
          ax: headPt.x,
          ay: headPt.y,
          bx: tailPt.x,
          by: tailPt.y,
          ctrlX: (headPt.x + tailPt.x) * 0.5,
          ctrlY: (headPt.y + tailPt.y) * 0.5,
          startLane,
          endLane,
          radius: 36,
          age: 0,
          maxLifetime: 3.2,
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

    // 7. Update Scrolling Guitar-Hero Sustain Slide Ribbons (Free-Steering Hold & Slide Tracking!)
    this.activeWormholeSlide = null;
    for (let i = this.wormholes.length - 1; i >= 0; i--) {
      const wh = this.wormholes[i];
      wh.age += dt;
      if (wh.teleportCooldown > 0) wh.teleportCooldown = Math.max(0, wh.teleportCooldown - dt);
      if (wh.age >= wh.maxLifetime) {
        this.wormholes.splice(i, 1);
        continue;
      }

      const headU = (wh.age / wh.maxLifetime) * 1.45;
      const tailU = headU - 0.58;
      const sLane = typeof wh.startLane === "number" ? wh.startLane : 1;
      const eLane = typeof wh.endLane === "number" ? wh.endLane : 2;

      if (typeof this.computeHighwayPoint === "function") {
        const headPt = this.computeHighwayPoint(sLane, headU, this.W, this.H);
        const tailPt = this.computeHighwayPoint(eLane, tailU, this.W, this.H);
        const midPt = this.computeHighwayPoint((sLane + eLane) * 0.5, (headU + tailU) * 0.5, this.W, this.H);
        wh.ax = headPt.x;
        wh.ay = headPt.y;
        wh.bx = tailPt.x;
        wh.by = tailPt.y;
        wh.ctrlX = midPt.x;
        wh.ctrlY = midPt.y;

        // Free-steering tracking check: sample 7 points along the scrolling sustain ribbon
        let minDist = 9999;
        let closestPt = headPt;
        for (let s = 0; s <= 6; s++) {
          const frac = s / 6.0;
          const uSample = headU + (tailU - headU) * frac;
          if (uSample < 0.05 || uSample > 1.25) continue;
          const laneSample = sLane + (eLane - sLane) * frac;
          const pt = this.computeHighwayPoint(laneSample, uSample, this.W, this.H);
          const d = Math.hypot(this.playerX - pt.x, this.playerY - pt.y);
          if (d < minDist) {
            minDist = d;
            closestPt = pt;
          }
        }

        if (minDist <= 66 && wh.teleportCooldown <= 0) {
          wh.teleportCooldown = 0.14;
          this.shieldTimer = Math.max(this.shieldTimer, 0.35);
          this.playSfx("orb", 0.58);
          this.triggerHaptic([12]);
          const slidePts = 65 * Math.max(1, this.combo);
          this.score += slidePts;
          this.comboTimer = 4.5;
          this.spawnBurstMotes(closestPt.x, closestPt.y, [56, 189, 248], 6);
          this.shockwaves.push({
            x: closestPt.x,
            y: closestPt.y,
            radius: 6,
            maxRadius: 68,
            color: [56, 189, 248],
            alpha: 1
          });
          this.floatingTexts.push({
            x: this.playerX,
            y: this.playerY - 24,
            text: `SLIDE ♪ +${slidePts}`,
            color: [56, 189, 248],
            alpha: 1,
            age: 0
          });
        }
      }
    }

    // 8. Update Scrolling Guitar-Hero Beat Notes Down the 4-Lane Rhythm Highway
    for (let i = this.powerUps.length - 1; i >= 0; i--) {
      const pu = this.powerUps[i];
      pu.age += worldDt;

      const leadTime = Math.max(0.35, (pu.targetBeatSec || 0) - (pu.spawnBeatSec || 0));
      const u = (this.trackElapsedSec - (pu.spawnBeatSec || 0)) / leadTime;
      const laneIdx = Math.max(0, Math.min(3, (pu.phraseBeatNum || 1) - 1));

      if (typeof this.computeHighwayPoint === "function") {
        const pt = this.computeHighwayPoint(laneIdx, u, this.W, this.H);
        pu.x = pt.x;
        pu.y = pt.y;
        pu.targetX = pt.x;
        pu.targetY = this.H * 0.74;
      }

      // Check expiration once note scrolls past the bottom of the Beat Strike Line (u > 1.24)
      const isExpiredByTtl = u > 1.24 || (pu.expireBeatSec > 0 && this.trackElapsedSec > pu.expireBeatSec);
      if (isExpiredByTtl || pu.age >= pu.maxLifetime) {
        if (isExpiredByTtl) {
          // Missed beat note past the Strike Line: ONLY reset combo (never damages player or ends run!)
          if (this.combo > 1) this.combo = 1;
          this.comboTimer = 0;
          this.shockwaves.push({
            x: pu.x,
            y: Math.min(this.H * 0.84, pu.y),
            radius: 8,
            maxRadius: 68,
            color: [251, 113, 133],
            alpha: 1
          });
          this.floatingTexts.push({
            x: pu.x,
            y: Math.min(this.H * 0.82, pu.y - 12),
            text: "MISS • COMBO RESET",
            color: [251, 113, 133],
            alpha: 1,
            age: 0
          });
        }
        this.powerUps.splice(i, 1);
        continue;
      }

      const dx = this.playerX - pu.x;
      const dy = this.playerY - pu.y;
      const dist = Math.max(1, Math.hypot(dx, dy));

      // Generous Collection Check for responsive highway note hits (NO MAGNET PULL!)
      const collectRadius = this.playerRadius + (pu.type === "SCORE_ORB" ? pu.radius * 1.42 : pu.radius * 1.32);
      if (dist <= collectRadius) {
        this.collectPowerUp(pu, u);
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
    const rawSupernova = cue.isGoldenCue || (cue.keyWordEffect && cue.keyWordEffect !== "NONE");
    const rawLaneIdx = Math.floor(((cue.normX || 0.5) - 0.14) / 0.18 + 0.5);
    const laneIdx = Math.max(0, Math.min(3, rawLaneIdx));
    const phraseBeatNum = laneIdx + 1;

    // Radical Pickup Simplification:
    // ONLY 4 unmistakable highway items exist:
    // 1. Standard Fret Beat Note ("SCORE_ORB" + "NONE")
    // 2. ★ 4X STAR Note ("SCORE_ORB" + "CONSTELLATION_STAR")
    // 3. 💥 BOMB ("BLAST" - vaporizes all enemies on screen)
    // 4. 🛡️ SHIELD ("SHIELD" - blocks 1 hit)
    let cueType = "SCORE_ORB";
    let keyWordEffect = "NONE";
    let isSupernova = false;

    const rawType = cue.powerUpType || "SCORE_ORB";
    const rawEffect = cue.keyWordEffect || "NONE";

    if (rawType === "BLAST" || rawType === "OVERDRIVE" || rawEffect === "MAGNETAR_WAR") {
      cueType = "BLAST";
    } else if (rawType === "SHIELD" || rawType === "SLOW_MO" || rawEffect === "ECLIPSE_DARK") {
      cueType = "SHIELD";
    } else if (rawSupernova) {
      cueType = "SCORE_ORB";
      keyWordEffect = "CONSTELLATION_STAR";
      isSupernova = true;
    }

    const startPt = typeof this.computeHighwayPoint === "function"
      ? this.computeHighwayPoint(laneIdx, 0, this.W, this.H)
      : { x: (0.14 + laneIdx * 0.24) * this.W, y: this.H * 0.16 };
    const receptorPt = typeof this.computeHighwayPoint === "function"
      ? this.computeHighwayPoint(laneIdx, 1.0, this.W, this.H)
      : { x: (0.14 + laneIdx * 0.24) * this.W, y: this.H * 0.74 };

    const targetBeatSec = cue.timestampSec || (this.trackElapsedSec + 1.9);
    const ttlGraceSec = Math.max(0.45, beatPeriod * 0.92);
    const expireBeatSec = targetBeatSec + ttlGraceSec;
    const seqIdx = cue.sequenceIndex || (this.powerUps.length + 1);

    this.powerUps.push({
      x: startPt.x,
      y: startPt.y,
      targetX: receptorPt.x,
      targetY: receptorPt.y,
      vx: 0,
      vy: 0,
      speed: 0,
      angle: 0,
      radius: (isSupernova || cueType !== "SCORE_ORB") ? 28 : 23,
      type: cueType,
      keyWordEffect,
      isSupernova,
      sequenceIndex: seqIdx,
      phraseBeatNum,
      targetBeatSec,
      spawnBeatSec: this.trackElapsedSec,
      expireBeatSec,
      age: 0,
      maxLifetime: Math.max(2.6, (expireBeatSec - this.trackElapsedSec) + 0.45),
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

  collectPowerUp(pu, uHit = 1.0) {
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

      // Timing Accuracy Check: compare highway position against the Glowing Beat Strike Line (Y = 74% / u = 1.0)!
      const beatDiff = Math.abs(this.trackElapsedSec - (pu.targetBeatSec || this.trackElapsedSec));
      const strikeY = this.H * 0.74;
      const isAtStrikeLine = (uHit >= 0.78 && uHit <= 1.18) || Math.abs(pu.y - strikeY) <= 74 || beatDiff <= 0.24;
      const basePts = isAtStrikeLine ? 120 : 60;
      const pts = basePts * this.combo * (this.constellationRushTimer > 0 ? 2 : 1);
      this.score += pts;

      this.spawnBurstMotes(pu.x, pu.y, isAtStrikeLine ? [74, 222, 128] : [250, 204, 21], 16);
      this.shockwaves.push({
        x: pu.x,
        y: isAtStrikeLine ? strikeY : pu.y,
        radius: 10,
        maxRadius: isAtStrikeLine ? 165 : 95,
        color: isAtStrikeLine ? [74, 222, 128] : [250, 204, 21],
        alpha: 1
      });
      this.floatingTexts.push({
        x: pu.x,
        y: pu.y - 20,
        text: isAtStrikeLine ? `★ PERFECT ON-BEAT! +${pts} (x${this.combo})` : `EARLY +${pts}`,
        color: isAtStrikeLine ? [74, 222, 128] : [250, 204, 21],
        alpha: 1,
        age: 0
      });

      // ★ 4X STAR Overdrive Note (1:1 with Android GameOrbCollection.kt)
      const eff = pu.keyWordEffect || "NONE";
      if (pu.isSupernova || eff !== "NONE") {
        this.screenShake = Math.max(this.screenShake, 9);
        this.overdriveTimer = Math.max(this.overdriveTimer, 8.0);
        this.constellationRushTimer = Math.max(this.constellationRushTimer, 10.0);
        this.showToast("★", "STAR POWER OVERDRIVE!", "4X SCORE MULTIPLIER • RAM RED HAZARDS!", "#FACC15", 2.8);
      }
    } else {
      this.playSfx("powerup", 0.8);
      this.triggerHaptic([30]);
      let col = [56, 189, 248];
      if (pu.type === "BLAST" || pu.type === "OVERDRIVE") {
        col = [239, 68, 68];
        this.overdriveTimer = Math.max(this.overdriveTimer, 5.5);
        this.screenShake = Math.max(this.screenShake, 16);
        let vaporized = 0;
        for (let k = 0; k < this.enemies.length; k++) {
          const en = this.enemies[k];
          vaporized++;
          this.score += 150 * Math.max(1, this.combo);
          this.spawnBurstMotes(en.x, en.y, k % 2 === 0 ? [250, 204, 21] : [239, 68, 68], 12);
          this.shockwaves.push({ x: en.x, y: en.y, radius: 8, maxRadius: 150, color: [250, 204, 21], alpha: 1 });
        }
        this.enemies = [];
        this.telegraphs = [];
        this.shockwaves.push({ x: pu.x, y: pu.y, radius: 12, maxRadius: 560, color: col, alpha: 1 });
        this.showToast("💥", "BOMB DETONATED!",
          vaporized > 0 ? `${vaporized} ENEMIES DESTROYED! (+${vaporized * 150} PTS)` : "ALL HAZARDS CLEARED + 5.5s OVERDRIVE!",
          "#EF4444", 2.6);
      } else {
        this.shieldTimer = Math.max(this.shieldTimer, 8.0);
        col = [56, 189, 248];
        this.showToast("🛡️", "SHIELD ACTIVE!", "BLOCKS NEXT ENEMY HIT • 8.0s BARRIER", "#38BDF8", 2.4);
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
