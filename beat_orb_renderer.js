// ============================================================================
// NO MORE ILLUSIONS — BEAT ORBS, SHRINKING TTL RINGS & SLIDE WORMHOLES
// ============================================================================

function getTrailPalette(trailId) {
  switch (trailId) {
    case "trail_gold": return [[250, 204, 21], [245, 158, 11]];
    case "trail_emerald": return [[52, 211, 153], [16, 185, 129]];
    case "trail_prism": return [[232, 121, 249], [192, 132, 252]];
    case "trail_laser": return [[34, 211, 238], [250, 204, 21]];
    default: return [[56, 189, 248], [129, 140, 248]];
  }
}

function drawMotes(ctx, motes) {
  for (let i = 0; i < motes.length; i++) {
    const m = motes[i];
    if (m.alpha > 0.01) {
      ctx.beginPath();
      ctx.arc(m.x, m.y, m.radius, 0, Math.PI * 2);
      ctx.fillStyle = rgba(m.color, Math.max(0, Math.min(1, m.alpha)) * 0.7);
      ctx.fill();
    }
  }
}

function drawPlayerTrail(ctx, trailPoints, trailLength, selectedTrailId) {
  if (trailLength <= 1) return;
  const [trailStart, trailEnd] = getTrailPalette(selectedTrailId);
  for (let i = 0; i < trailLength - 1; i++) {
    const p1 = trailPoints[i];
    const p2 = trailPoints[i + 1];
    const prog = i / trailLength;
    const tCol = lerpColor(trailStart, trailEnd, prog);
    ctx.beginPath();
    ctx.moveTo(p1.x, p1.y);
    ctx.lineTo(p2.x, p2.y);
    ctx.strokeStyle = rgba(tCol, (1 - prog) * 0.48);
    ctx.lineWidth = (1 - prog) * 18;
    ctx.lineCap = "round";
    ctx.stroke();
  }
}

function drawShockwaves(ctx, shockwaves) {
  for (let i = 0; i < shockwaves.length; i++) {
    const sw = shockwaves[i];
    const p = Math.max(0, Math.min(1, sw.radius / sw.maxRadius));
    const swAlpha = Math.pow(1 - p, 1.5);
    if (swAlpha > 0.01) {
      ctx.beginPath();
      ctx.arc(sw.x, sw.y, sw.radius, 0, Math.PI * 2);
      ctx.strokeStyle = rgba(sw.color, swAlpha * 0.35);
      ctx.lineWidth = (1 - p) * 26 + 6;
      ctx.stroke();

      ctx.beginPath();
      ctx.arc(sw.x, sw.y, sw.radius, 0, Math.PI * 2);
      ctx.strokeStyle = rgba([255, 255, 255], swAlpha * 0.9);
      ctx.lineWidth = (1 - p) * 6 + 1.5;
      ctx.stroke();
    }
  }
}

function drawWormholes(ctx, wormholes, time, beatPulse = 0) {
  for (let i = 0; i < wormholes.length; i++) {
    const wh = wormholes[i];
    const fadeIn = Math.min(1, wh.age / 0.8);
    const fadeOut = Math.min(1, (wh.maxLifetime - wh.age) / 0.8);
    const alpha = Math.max(0, Math.min(1, fadeIn * fadeOut));
    if (alpha <= 0.02) continue;

    // Musical Slide-Note Ribbon Stream between Portal A and Portal B
    const ctrlX = (typeof wh.ctrlX === "number" && wh.ctrlX !== 0) ? wh.ctrlX : (wh.ax + wh.bx) * 0.5;
    const ctrlY = (typeof wh.ctrlY === "number" && wh.ctrlY !== 0) ? wh.ctrlY : (wh.ay + wh.by) * 0.5 - 60;

    // 1. Outer Volumetric Light-Ribbon Track
    ctx.beginPath();
    ctx.moveTo(wh.ax, wh.ay);
    ctx.quadraticCurveTo(ctrlX, ctrlY, wh.bx, wh.by);
    ctx.strokeStyle = rgba([168, 85, 247], 0.24 * alpha);
    ctx.lineWidth = 22 + beatPulse * 6;
    ctx.lineCap = "round";
    ctx.stroke();

    // 2. Inner Silk Slide Core
    ctx.beginPath();
    ctx.moveTo(wh.ax, wh.ay);
    ctx.quadraticCurveTo(ctrlX, ctrlY, wh.bx, wh.by);
    ctx.strokeStyle = rgba([34, 211, 238], (0.58 + beatPulse * 0.25) * alpha);
    ctx.lineWidth = 5.5;
    ctx.lineCap = "round";
    ctx.stroke();

    // 3. 5 Pulsing Mini Rhythm Pearls along the Slide Stream
    for (let nodeIdx = 1; nodeIdx <= 5; nodeIdx++) {
      const nt = nodeIdx / 6.0;
      const inv = 1 - nt;
      const nx = inv * inv * wh.ax + 2 * inv * nt * ctrlX + nt * nt * wh.bx;
      const ny = inv * inv * wh.ay + 2 * inv * nt * ctrlY + nt * nt * wh.by;
      const nodePulse = 1.0 + 0.30 * Math.sin(time * 6.5 - nodeIdx * 0.9);
      const nCol = nodeIdx % 2 === 0 ? [250, 204, 21] : [34, 211, 238];

      ctx.beginPath();
      ctx.arc(nx, ny, 9.0 * nodePulse, 0, Math.PI * 2);
      ctx.fillStyle = rgba(nCol, 0.35 * alpha);
      ctx.fill();

      ctx.beginPath();
      ctx.arc(nx, ny, 4.5 * nodePulse, 0, Math.PI * 2);
      ctx.fillStyle = rgba([255, 255, 255], 0.92 * alpha);
      ctx.fill();
    }

    // 4. Flowing Speed Chevrons along the Ribbon
    for (let m = 0; m < 4; m++) {
      const mt = ((time * 0.85 + m * 0.25) % 1);
      const inv = 1 - mt;
      const mx = inv * inv * wh.ax + 2 * inv * mt * ctrlX + mt * mt * wh.bx;
      const my = inv * inv * wh.ay + 2 * inv * mt * ctrlY + mt * mt * wh.by;
      ctx.beginPath();
      ctx.arc(mx, my, 4.2 * Math.sin(mt * Math.PI) + 1.5, 0, Math.PI * 2);
      ctx.fillStyle = rgba([250, 204, 21], 0.72 * Math.sin(mt * Math.PI) * alpha);
      ctx.fill();
    }

    const portals = [
      { x: wh.ax, y: wh.ay, col: [56, 189, 248], rim: [168, 85, 247], label: "SLIDE ♪" },
      { x: wh.bx, y: wh.by, col: [244, 114, 182], rim: [250, 204, 21], label: "EXIT ★" }
    ];
    for (const p of portals) {
      const grad = ctx.createRadialGradient(p.x, p.y, 2, p.x, p.y, wh.radius * 1.9);
      grad.addColorStop(0, rgba([5, 3, 15], 0.95 * alpha));
      grad.addColorStop(0.5, rgba(p.col, 0.55 * alpha));
      grad.addColorStop(0.8, rgba(p.rim, 0.25 * alpha));
      grad.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(p.x, p.y, wh.radius * 1.9, 0, Math.PI * 2);
      ctx.fillStyle = grad;
      ctx.fill();

      for (let ring = 0; ring < 3; ring++) {
        const r = wh.radius * (0.55 + ring * 0.32);
        const startRad = ((time * (190 - ring * 55) * (ring % 2 === 0 ? 1 : -1)) % 360) * Math.PI / 180;
        ctx.beginPath();
        ctx.arc(p.x, p.y, r, startRad, startRad + (115 * Math.PI / 180));
        ctx.strokeStyle = rgba(ring % 2 === 0 ? p.col : p.rim, (0.85 - ring * 0.2) * alpha);
        ctx.lineWidth = 4 - ring * 0.8;
        ctx.lineCap = "round";
        ctx.stroke();

        ctx.beginPath();
        ctx.arc(p.x, p.y, r, startRad + Math.PI, startRad + Math.PI + (115 * Math.PI / 180));
        ctx.stroke();
      }
      ctx.beginPath();
      ctx.arc(p.x, p.y, wh.radius * 0.38, 0, Math.PI * 2);
      ctx.fillStyle = rgba([6, 4, 16], 0.94 * alpha);
      ctx.fill();
      ctx.strokeStyle = rgba([255, 255, 255], 0.8 * alpha);
      ctx.lineWidth = 1.8;
      ctx.stroke();

      ctx.save();
      ctx.globalAlpha = alpha * 0.92;
      ctx.font = "900 11px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif";
      ctx.textAlign = "center";
      ctx.fillStyle = "#FFFFFF";
      ctx.fillText(p.label, p.x, p.y - wh.radius * 1.35);
      ctx.restore();
    }
  }
}

function drawPowerUps(ctx, powerUps, playerX, playerY, time, trackSec, beatPulse, isConstellationActive, multiBand) {
  if (!powerUps || powerUps.length === 0) return;

  const sorted = powerUps.slice().sort((a, b) => (a.sequenceIndex || 0) - (b.sequenceIndex || 0));
  const primaryTarget = sorted[0];

  // 1. Orbiting Hero Compass Arc + Curved Stardust Wisps to Primary Target Orb (No straight ruler line!)
  if (primaryTarget) {
    const isPrimarySupernova = primaryTarget.isSupernova || primaryTarget.keyWordEffect !== "NONE";
    const guideCol = isPrimarySupernova ? [250, 204, 21] : [34, 211, 238];
    const gdx = primaryTarget.x - playerX;
    const gdy = primaryTarget.y - playerY;
    const gDist = Math.hypot(gdx, gdy);

    if (gDist > 42) {
      const aimAngle = Math.atan2(gdy, gdx);
      const compassRad = 34 + beatPulse * 4;
      const arcSpan = 0.56; // ~32 degrees

      // Soft directional compass crescent around the hero pointing toward the next orb
      ctx.beginPath();
      ctx.arc(playerX, playerY, compassRad, aimAngle - arcSpan, aimAngle + arcSpan);
      ctx.strokeStyle = rgba(guideCol, 0.58 + beatPulse * 0.28);
      ctx.lineWidth = isPrimarySupernova ? 3.6 : 2.6;
      ctx.lineCap = "round";
      ctx.stroke();

      // Leading glowing pearl on the tip of the compass arc
      const tipX = playerX + Math.cos(aimAngle) * (compassRad + 5);
      const tipY = playerY + Math.sin(aimAngle) * (compassRad + 5);
      ctx.beginPath();
      ctx.arc(tipX, tipY, isPrimarySupernova ? 4.2 : 3.2, 0, Math.PI * 2);
      ctx.fillStyle = rgba([255, 255, 255], 0.92);
      ctx.fill();

      // 4 Organic Stardust Wisps drifting along a curved quadratic path to the target orb
      const perpX = -gdy / gDist;
      const perpY = gdx / gDist;
      const curveBend = Math.sin(time * 2.4) * Math.min(48, gDist * 0.18);
      const ctrlX = (playerX + primaryTarget.x) * 0.5 + perpX * curveBend;
      const ctrlY = (playerY + primaryTarget.y) * 0.5 + perpY * curveBend;

      for (let w = 0; w < 4; w++) {
        const wt = ((time * 1.35 + w * 0.25) % 1);
        const inv = 1 - wt;
        const wx = inv * inv * playerX + 2 * inv * wt * ctrlX + wt * wt * primaryTarget.x;
        const wy = inv * inv * playerY + 2 * inv * wt * ctrlY + wt * wt * primaryTarget.y;
        const wAlpha = Math.sin(wt * Math.PI) * (isPrimarySupernova ? 0.78 : 0.55);
        const wRad = (isPrimarySupernova ? 3.6 : 2.6) * (0.65 + 0.5 * Math.sin(wt * Math.PI));

        ctx.beginPath();
        ctx.arc(wx, wy, wRad, 0, Math.PI * 2);
        ctx.fillStyle = rgba(guideCol, wAlpha);
        ctx.fill();
      }
    }

    // 2. Sequential Constellation Path connecting 1 -> 2 -> 3 -> 4 so player sees the linear musical sequence!
    if (sorted.length >= 2) {
      for (let cIdx = 0; cIdx < sorted.length - 1; cIdx++) {
        const curOrb = sorted[cIdx];
        const nextOrb = sorted[cIdx + 1];
        const linkAlpha = cIdx === 0 ? 0.52 : (cIdx === 1 ? 0.34 : 0.20);
        ctx.beginPath();
        ctx.moveTo(curOrb.x, curOrb.y);
        ctx.lineTo(nextOrb.x, nextOrb.y);
        ctx.strokeStyle = rgba([56, 189, 248], linkAlpha);
        ctx.lineWidth = cIdx === 0 ? 2.6 : 1.6;
        ctx.lineCap = "round";
        ctx.stroke();

        for (let m = 1; m <= 3; m++) {
          const mt = ((m / 4) + time * 0.9) % 1;
          const mx = curOrb.x + (nextOrb.x - curOrb.x) * mt;
          const my = curOrb.y + (nextOrb.y - curOrb.y) * mt;
          ctx.beginPath();
          ctx.arc(mx, my, cIdx === 0 ? 2.8 : 2.0, 0, Math.PI * 2);
          ctx.fillStyle = rgba([250, 204, 21], linkAlpha * 1.25);
          ctx.fill();
        }
      }
    }
  }

  // 3. Render Each Collectible Orb with 100% Visual Clarity & Distinction
  for (let idx = 0; idx < sorted.length; idx++) {
    const pu = sorted[idx];
    const isPrimary = idx === 0;
    const isScoreOrb = pu.type === "SCORE_ORB";
    const isSupernova = isScoreOrb && (pu.isSupernova || pu.keyWordEffect !== "NONE");

    const fadeIn = Math.min(1, pu.age / 0.25);
    const remTtl = pu.expireBeatSec > 0 ? Math.max(0, pu.expireBeatSec - trackSec) : (pu.maxLifetime - pu.age);
    const fadeOut = Math.min(1, remTtl / 0.22);
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
        case "BLAST":
        case "OVERDRIVE": effectCol = [249, 115, 22]; break;
        case "MAGNET": effectCol = [16, 185, 129]; break;
        default: effectCol = [250, 204, 21];
      }
    }

    const popInScale = Math.min(1.0, 0.55 + (pu.age / 0.22) * 0.45);
    const beatPulseScale = 1.0 + (isSupernova ? 0.34 : 0.22) * beatPulse;
    const coreRad = (pu.radius || 22) * popInScale * beatPulseScale * (isPrimary ? 1.08 : 0.90);

    // -----------------------------------------------------------------------
    // OSU! / PIANO-TILES SHRINKING APPROACH RING (Time-To-Live Beat Indicator)
    // Contracts smoothly from 2.35x -> 1.0x right at targetBeatSec!
    // -----------------------------------------------------------------------
    const leadWindow = Math.max(0.25, (pu.targetBeatSec || trackSec) - (pu.spawnBeatSec || (trackSec - 1.0)));
    const timeToBeat = (pu.targetBeatSec || trackSec) - trackSec;
    const approachRatio = timeToBeat >= 0
      ? (1.0 + Math.max(0, Math.min(1, timeToBeat / leadWindow)) * 1.35)
      : Math.max(0.86, 1.0 + (timeToBeat / 0.45) * 0.14);
    const isPerfectWindow = Math.abs(timeToBeat) <= 0.19 || beatPulse > 0.48;
    const isExpiringSoon = timeToBeat < -0.18;
    const ringCol = isPerfectWindow ? [74, 222, 128] : (isExpiringSoon ? [251, 113, 133] : effectCol);

    ctx.beginPath();
    ctx.arc(pu.x, pu.y, coreRad * approachRatio, 0, Math.PI * 2);
    ctx.strokeStyle = rgba(ringCol, (isPerfectWindow ? 0.96 : 0.72) * puAlpha);
    ctx.lineWidth = isPerfectWindow ? 3.8 : 2.2;
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

      // Counter-Rotating Curved Celestial Halo Arcs (No boxy squares!)
      for (let h = 0; h < 2; h++) {
        const spin = time * (h === 0 ? 2.2 : -2.6);
        const hRad = coreRad * (h === 0 ? 0.96 : 0.72);
        const hCol = h === 0 ? [250, 204, 21] : effectCol;
        for (let a = 0; a < 2; a++) {
          const startA = spin + a * Math.PI;
          ctx.beginPath();
          ctx.arc(pu.x, pu.y, hRad, startA, startA + 1.85);
          ctx.strokeStyle = rgba(hCol, 0.92 * puAlpha);
          ctx.lineWidth = 2.5;
          ctx.lineCap = "round";
          ctx.stroke();
        }
      }

      // Blazing White-Gold Singularity Core
      ctx.beginPath();
      ctx.arc(pu.x, pu.y, coreRad * 0.46, 0, Math.PI * 2);
      ctx.fillStyle = rgba([250, 204, 21], 0.98 * puAlpha);
      ctx.fill();

      ctx.beginPath();
      ctx.arc(pu.x, pu.y, coreRad * 0.28, 0, Math.PI * 2);
      ctx.fillStyle = "#FFFFFF";
      ctx.fill();

    } else {
      // -----------------------------------------------------------------------
      // FACETED 6-SIDED HEXAGONAL MUSICAL GEMSTONE (1..4 Beat Gem / 💥 Blast / 🛡️ Shield / ⏳ Slow-Mo / 🧲 Magnet)
      // -----------------------------------------------------------------------
      drawHexGem(ctx, pu.x, pu.y, coreRad, effectCol, isScoreOrb ? "SCORE_ORB" : pu.type, time + (pu.phase || 0), puAlpha, pu.phraseBeatNum || (((pu.sequenceIndex || 1) - 1) % 4) + 1);
    }
  }
}

function drawHexGem(ctx, x, y, r, col, kind, t, alpha, phraseBeatNum = 1) {
  // Soft outer glow
  const glow = ctx.createRadialGradient(x, y, 2, x, y, r * 1.9);
  glow.addColorStop(0, rgba(col, 0.42 * alpha));
  glow.addColorStop(1, "rgba(0,0,0,0)");
  ctx.beginPath();
  ctx.arc(x, y, r * 1.9, 0, Math.PI * 2);
  ctx.fillStyle = glow;
  ctx.fill();

  const rot = t * 0.6;
  const verts = [];
  for (let v = 0; v < 6; v++) {
    const a = rot + v * (Math.PI / 3);
    verts.push([x + Math.cos(a) * r, y + Math.sin(a) * r]);
  }
  // Facets: alternate light/dark triangles toward center for a cut-gem look
  for (let v = 0; v < 6; v++) {
    const p1 = verts[v];
    const p2 = verts[(v + 1) % 6];
    ctx.beginPath();
    ctx.moveTo(x, y);
    ctx.lineTo(p1[0], p1[1]);
    ctx.lineTo(p2[0], p2[1]);
    ctx.closePath();
    const shade = v % 2 === 0 ? 1.0 : 0.62;
    ctx.fillStyle = rgba([Math.round(col[0] * shade), Math.round(col[1] * shade), Math.round(col[2] * shade)], 0.92 * alpha);
    ctx.fill();
  }
  // Rim
  ctx.beginPath();
  verts.forEach((p, i) => (i === 0 ? ctx.moveTo(p[0], p[1]) : ctx.lineTo(p[0], p[1])));
  ctx.closePath();
  ctx.strokeStyle = rgba([255, 255, 255], 0.9 * alpha);
  ctx.lineWidth = 2;
  ctx.stroke();
  // Inner table (dark core plate for emblem contrast)
  ctx.beginPath();
  for (let v = 0; v < 6; v++) {
    const a = rot + v * (Math.PI / 3);
    const px = x + Math.cos(a) * r * 0.58;
    const py = y + Math.sin(a) * r * 0.58;
    if (v === 0) ctx.moveTo(px, py); else ctx.lineTo(px, py);
  }
  ctx.closePath();
  ctx.fillStyle = rgba([8, 10, 22], 0.78 * alpha);
  ctx.fill();
  ctx.strokeStyle = rgba(col, 0.95 * alpha);
  ctx.lineWidth = 1.2;
  ctx.stroke();
  // Specular glint
  ctx.beginPath();
  ctx.arc(x - r * 0.38, y - r * 0.42, r * 0.12, 0, Math.PI * 2);
  ctx.fillStyle = rgba([255, 255, 255], 0.85 * alpha);
  ctx.fill();
  // Etched emblem: phrase sequence number (1, 2, 3, 4) for SCORE_ORB, or power-up icon
  const isScoreGem = kind === "SCORE_ORB";
  const emblem = kind === "BLAST" || kind === "OVERDRIVE" ? "💥"
    : kind === "SHIELD" ? "🛡️"
    : kind === "SLOW_MO" ? "⏳"
    : kind === "MAGNET" ? "🧲"
    : String(phraseBeatNum);
  ctx.save();
  ctx.globalAlpha = alpha;
  ctx.textAlign = "center";
  ctx.textBaseline = "middle";
  ctx.font = isScoreGem
    ? `900 ${Math.round(r * 0.88)}px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`
    : `${Math.round(r * 0.78)}px "Apple Color Emoji", "Segoe UI Emoji", "Noto Color Emoji", sans-serif`;
  ctx.fillStyle = "#FFFFFF";
  ctx.fillText(emblem, x, y + (isScoreGem ? r * 0.04 : r * 0.02));
  ctx.restore();
}

