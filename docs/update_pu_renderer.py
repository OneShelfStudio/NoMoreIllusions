import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Let's write the exact 1:1 Android drawPowerUps matching GameRenderer.kt:
# Includes:
# 1. Direct Hero -> Primary Target Orb Harmonic Guidance Beam (guide photon racing from hero to target orb)
# 2. Preview tether from Primary Target Orb to Next Upcoming Beat Orb
# 3. Supernova Beat-Drop Orbs (2x larger, 8 solar flare spikes, 2 counter-rotating sacred gyroscope rings)
# 4. Regular Beat Orbs (Clean Cyan-Emerald Beat-Pulsing Pearl with radial glow, inner core, white center, and contracting beat timing ring)
# 5. Support power-ups (Shield, Overdrive, Magnet, Chrono)

exact_draw_powerups = '''function drawPowerUps(ctx, powerUps, playerX, playerY, time, trackSec, beatPulse, isConstellationActive, multiBand) {
  if (!powerUps || powerUps.length === 0) return;

  const sorted = powerUps.slice().sort((a, b) => (a.sequenceIndex || 0) - (b.sequenceIndex || 0));
  const primaryTarget = sorted[0];

  // 1. Direct Hero -> Primary Target Orb Harmonic Guidance Beam
  if (primaryTarget) {
    const isPrimarySupernova = primaryTarget.isSupernova || primaryTarget.keyWordEffect !== "NONE";
    const guideCol = isPrimarySupernova ? [250, 204, 21] : [34, 211, 238];
    const gdx = primaryTarget.x - playerX;
    const gdy = primaryTarget.y - playerY;
    const gDist = Math.hypot(gdx, gdy);

    if (gDist > 36) {
      const beamGrad = ctx.createLinearGradient(playerX, playerY, primaryTarget.x, primaryTarget.y);
      beamGrad.addColorStop(0, rgba(guideCol, 0.14 + beatPulse * 0.16));
      beamGrad.addColorStop(1, rgba(guideCol, isPrimarySupernova ? 0.55 : 0.36));

      ctx.beginPath();
      ctx.moveTo(playerX, playerY);
      ctx.lineTo(primaryTarget.x, primaryTarget.y);
      ctx.strokeStyle = beamGrad;
      ctx.lineWidth = isPrimarySupernova ? 2.8 : 1.8;
      ctx.lineCap = "round";
      ctx.stroke();

      // Beat-synced guide photon racing along the beam from Hero to Target Orb
      const photonT = ((time * 2.2) % 1);
      ctx.beginPath();
      ctx.arc(playerX + gdx * photonT, playerY + gdy * photonT, isPrimarySupernova ? 3.8 : 2.6, 0, Math.PI * 2);
      ctx.fillStyle = "rgba(255, 255, 255, 0.92)";
      ctx.fill();
    }

    // 2. Preview tether from Primary Target Orb to the Next Upcoming Beat Orb
    if (sorted.length >= 2) {
      const nextOrb = sorted[1];
      ctx.beginPath();
      ctx.moveTo(primaryTarget.x, primaryTarget.y);
      ctx.lineTo(nextOrb.x, nextOrb.y);
      ctx.strokeStyle = "rgba(56, 189, 248, 0.25)";
      ctx.lineWidth = 1.4;
      ctx.lineCap = "round";
      ctx.stroke();
    }
  }

  // 3. Render Each Collectible Orb with 100% Visual Clarity & Distinction
  for (let idx = 0; idx < sorted.length; idx++) {
    const pu = sorted[idx];
    const isPrimary = idx === 0;
    const isScoreOrb = pu.type === "SCORE_ORB";
    const isSupernova = isScoreOrb && (pu.isSupernova || pu.keyWordEffect !== "NONE");

    const fadeIn = Math.min(1, pu.age / 0.35);
    const fadeOut = Math.min(1, (pu.maxLifetime - pu.age) / 1.0);
    const puAlpha = Math.max(0, Math.min(1, fadeIn * fadeOut));
    if (puAlpha <= 0.01) continue;

    let effectCol;
    if (isScoreOrb) {
      switch (pu.keyWordEffect) {
        case "TRACTOR_REACH": effectCol = [34, 211, 238]; break; // Cyan
        case "PRISM_SHATTER": effectCol = [232, 121, 249]; break; // Magenta
        case "MAGNETAR_WAR": effectCol = [251, 113, 133]; break; // Coral Rose
        case "CONSTELLATION_STAR": effectCol = [250, 204, 21]; break; // 24K Gold
        case "BINARY_HEART": effectCol = [74, 222, 128]; break; // Emerald
        case "ECLIPSE_DARK": effectCol = [253, 224, 71]; break; // Solar Yellow
        default: effectCol = isSupernova ? [250, 204, 21] : [6, 182, 212];
      }
    } else {
      switch (pu.type) {
        case "SHIELD": effectCol = [56, 189, 248]; break;
        case "SLOW_MO": effectCol = [168, 85, 247]; break;
        case "OVERDRIVE": effectCol = [244, 63, 94]; break;
        case "MAGNET": effectCol = [52, 211, 153]; break;
        default: effectCol = [250, 204, 21];
      }
    }

    const popInScale = Math.min(1.0, 0.45 + (pu.age / 0.25) * 0.55);
    const beatPulseScale = 1.0 + (isSupernova ? 0.38 : 0.26) * beatPulse;
    const coreRad = (pu.radius || 20) * popInScale * beatPulseScale * (isPrimary ? 1.06 : 0.88);

    // 0. Traveling Harmonic Comet Wake trailing behind flight vector (-cos, -sin)
    const moveAngle = pu.angle !== undefined ? pu.angle : Math.atan2(pu.vy, pu.vx);
    const tailDirX = -Math.cos(moveAngle);
    const tailDirY = -Math.sin(moveAngle);
    const perpX = -tailDirY;
    const perpY = tailDirX;
    const tailSegs = isSupernova ? 6 : 5;

    for (let seg = 1; seg <= tailSegs; seg++) {
      const t = seg / tailSegs;
      const dist = t * (isSupernova ? 64 : 42);
      const sway = Math.sin(time * 7 - seg * 0.7 + (pu.phase || 0)) * (t * 7);
      const tx = pu.x + tailDirX * dist + perpX * sway;
      const ty = pu.y + tailDirY * dist + perpY * sway;
      const tailCol = isScoreOrb ? (isSupernova ? [250, 204, 21] : [6, 182, 212]) : effectCol;

      ctx.beginPath();
      ctx.arc(tx, ty, coreRad * (1 - t * 0.7) * 0.48, 0, Math.PI * 2);
      ctx.fillStyle = rgba(tailCol, (1 - t) * 0.52 * puAlpha);
      ctx.fill();
    }

    // Converging Beat Timing Ring (contracts onto the orb core on every beat!)
    const ringRadius = coreRad * (1.04 + (1 - beatPulse) * (isSupernova ? 1.65 : 1.30));
    const isOnBeatWindow = beatPulse > 0.45;
    ctx.beginPath();
    ctx.arc(pu.x, pu.y, ringRadius, 0, Math.PI * 2);
    ctx.strokeStyle = rgba(isOnBeatWindow ? [250, 204, 21] : effectCol, (isOnBeatWindow ? 0.95 : 0.65) * puAlpha);
    ctx.lineWidth = isOnBeatWindow ? 3.6 : 2.0;
    ctx.stroke();

    if (isSupernova) {
      // -----------------------------------------------------------------------
      // SPECIAL BEAT-DROP SUPERNOVA ORB (24K Gold & Prismatic Gyroscope Star!)
      // -----------------------------------------------------------------------
      // Volumetric Radial Halo
      const haloGrad = ctx.createRadialGradient(pu.x, pu.y, 2, pu.x, pu.y, coreRad * 2.5);
      haloGrad.addColorStop(0, rgba([255, 255, 255], 0.98 * puAlpha));
      haloGrad.addColorStop(0.35, rgba(effectCol, 0.82 * puAlpha));
      haloGrad.addColorStop(0.70, rgba([250, 204, 21], 0.45 * puAlpha));
      haloGrad.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(pu.x, pu.y, coreRad * 2.5, 0, Math.PI * 2);
      ctx.fillStyle = haloGrad;
      ctx.fill();

      // 8 Pulsing Solar Flare Spikes
      const spikeCount = 8;
      for (let sIdx = 0; sIdx < spikeCount; sIdx++) {
        const ang = (sIdx * (Math.PI / 4)) + time * 0.7;
        const isMajor = sIdx % 2 === 0;
        const spikeLen = coreRad * (isMajor ? (2.1 + 0.55 * beatPulse) : (1.45 + 0.28 * beatPulse));
        ctx.beginPath();
        ctx.moveTo(pu.x - Math.cos(ang) * spikeLen, pu.y - Math.sin(ang) * spikeLen);
        ctx.lineTo(pu.x + Math.cos(ang) * spikeLen, pu.y + Math.sin(ang) * spikeLen);
        ctx.strokeStyle = rgba(isMajor ? [255, 254, 235] : effectCol, 0.88 * puAlpha);
        ctx.lineWidth = isMajor ? 3.0 : 1.8;
        ctx.lineCap = "round";
        ctx.stroke();
      }

      // Counter-Rotating Outer Sacred Geometry Gyroscope Ring #1
      ctx.save();
      ctx.translate(pu.x, pu.y);
      ctx.rotate((time * 115 * Math.PI) / 180);
      const g1 = coreRad * 0.92;
      ctx.strokeStyle = rgba([250, 204, 21], 0.92 * puAlpha);
      ctx.lineWidth = 2.6;
      roundRectPath(ctx, -g1, -g1, g1 * 2, g1 * 2, g1 * 0.22);
      ctx.stroke();
      ctx.restore();

      // Counter-Rotating Inner Sacred Geometry Gyroscope Ring #2
      ctx.save();
      ctx.translate(pu.x, pu.y);
      ctx.rotate((-time * 140 * Math.PI) / 180 + 0.78);
      const g2 = coreRad * 0.68;
      ctx.strokeStyle = rgba(effectCol, 0.95 * puAlpha);
      ctx.lineWidth = 2.4;
      roundRectPath(ctx, -g2, -g2, g2 * 2, g2 * 2, g2 * 0.18);
      ctx.stroke();
      ctx.restore();

      // Blazing White-Gold Singularity Core
      ctx.beginPath();
      ctx.arc(pu.x, pu.y, coreRad * 0.46, 0, Math.PI * 2);
      ctx.fillStyle = rgba([250, 204, 21], 0.98 * puAlpha);
      ctx.fill();

      ctx.beginPath();
      ctx.arc(pu.x, pu.y, coreRad * 0.28, 0, Math.PI * 2);
      ctx.fillStyle = "#FFFFFF";
      ctx.fill();

    } else if (isScoreOrb) {
      // -----------------------------------------------------------------------
      // REGULAR BEAT ORB: Clean Cyan-Emerald Beat-Pulsing Pearl
      // -----------------------------------------------------------------------
      const grad = ctx.createRadialGradient(pu.x, pu.y, 2, pu.x, pu.y, coreRad * 1.85);
      grad.addColorStop(0, rgba([255, 255, 255], 0.92 * puAlpha));
      grad.addColorStop(0.40, rgba(effectCol, 0.68 * puAlpha));
      grad.addColorStop(0.75, rgba([16, 185, 129], 0.28 * puAlpha));
      grad.addColorStop(1, "rgba(0,0,0,0)");

      ctx.beginPath();
      ctx.arc(pu.x, pu.y, coreRad * 1.85, 0, Math.PI * 2);
      ctx.fillStyle = grad;
      ctx.fill();

      ctx.beginPath();
      ctx.arc(pu.x, pu.y, coreRad * 0.58, 0, Math.PI * 2);
      ctx.fillStyle = rgba(effectCol, 0.92 * puAlpha);
      ctx.fill();

      ctx.beginPath();
      ctx.arc(pu.x, pu.y, coreRad * 0.30, 0, Math.PI * 2);
      ctx.fillStyle = "#FFFFFF";
      ctx.fill();

    } else {
      // -----------------------------------------------------------------------
      // SPECIAL SUPPORT POWER-UP (Aegis Shield, Chrono, Overdrive, Magnet)
      // -----------------------------------------------------------------------
      const grad = ctx.createRadialGradient(pu.x, pu.y, 2, pu.x, pu.y, coreRad * 2.0);
      grad.addColorStop(0, rgba([255, 255, 255], 0.95 * puAlpha));
      grad.addColorStop(0.45, rgba(effectCol, 0.75 * puAlpha));
      grad.addColorStop(1, "rgba(0,0,0,0)");

      ctx.beginPath();
      ctx.arc(pu.x, pu.y, coreRad * 2.0, 0, Math.PI * 2);
      ctx.fillStyle = grad;
      ctx.fill();

      // Outer geometric badge frame
      const rot = (time * 90 * Math.PI) / 180;
      ctx.save();
      ctx.translate(pu.x, pu.y);
      ctx.rotate(rot);
      ctx.strokeStyle = rgba(effectCol, 0.95 * puAlpha);
      ctx.lineWidth = 2.4;
      roundRectPath(ctx, -coreRad * 0.75, -coreRad * 0.75, coreRad * 1.5, coreRad * 1.5, coreRad * 0.3);
      ctx.stroke();
      ctx.restore();

      ctx.beginPath();
      ctx.arc(pu.x, pu.y, coreRad * 0.36, 0, Math.PI * 2);
      ctx.fillStyle = "#FFFFFF";
      ctx.fill();
    }
  }
}'''

# Replace drawPowerUps in engine.js
old_pu_start = js.find('function drawPowerUps(')
old_pu_end = js.find('function drawEnemyTelegraphs(', old_pu_start)
js = js[:old_pu_start] + exact_draw_powerups + '\n\n' + js[old_pu_end:]

# Also update the call to drawPowerUps inside renderGameCanvas():
js = js.replace(
    'drawPowerUps(ctx, this.powerUps, this.time, this.beatPulse, this.constellationRushTimer > 0);',
    'drawPowerUps(ctx, this.powerUps, this.playerX, this.playerY, this.time, this.trackElapsedSec, this.beatPulse, this.constellationRushTimer > 0, this.multiBandState);'
)

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'w', encoding='utf-8') as f:
    f.write(js)

print('Updated drawPowerUps to 1:1 Android GameRenderer.kt specification!')
