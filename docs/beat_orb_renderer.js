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
      if (m.isShard) {
        // High-velocity spinning multifaceted crystal glass shard
        const a = Math.max(0, Math.min(1, m.alpha));
        ctx.save();
        ctx.translate(m.x, m.y);
        ctx.rotate(m.rot || 0);

        ctx.beginPath();
        const rad = m.radius || 4;
        ctx.moveTo(0, -rad * 1.5);
        ctx.lineTo(rad * 0.9, -rad * 0.3);
        ctx.lineTo(rad * 0.7, rad * 1.4);
        ctx.lineTo(-rad * 0.7, rad * 1.2);
        ctx.lineTo(-rad * 0.8, -rad * 0.4);
        ctx.closePath();

        ctx.fillStyle = rgba(m.color, a * 0.88);
        ctx.shadowColor = rgba(m.color, a * 0.8);
        ctx.shadowBlur = 8;
        ctx.fill();

        // White specular rim
        ctx.strokeStyle = `rgba(255, 255, 255, ${a * 0.95})`;
        ctx.lineWidth = 1.2;
        ctx.stroke();

        ctx.restore();
      } else if (m.isStarfall) {
        // Melodic Starfall Petal: vertical aurora tail + swaying diamond core
        const a = Math.max(0, Math.min(1, m.alpha));
        const grad = ctx.createLinearGradient(m.x, m.y - m.radius * 4.2, m.x, m.y);
        grad.addColorStop(0, "rgba(255,255,255,0)");
        grad.addColorStop(1, rgba(m.color, a * 0.72));
        ctx.beginPath();
        ctx.moveTo(m.x, m.y - m.radius * 4.2);
        ctx.lineTo(m.x, m.y);
        ctx.strokeStyle = grad;
        ctx.lineWidth = m.radius * 1.1;
        ctx.lineCap = "round";
        ctx.stroke();

        ctx.beginPath();
        ctx.arc(m.x, m.y, m.radius * 1.4, 0, Math.PI * 2);
        ctx.fillStyle = rgba(m.color, a * 0.9);
        ctx.fill();

        ctx.beginPath();
        ctx.arc(m.x, m.y, m.radius * 0.65, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(255,255,255,${a})`;
        ctx.fill();
      } else {
        ctx.beginPath();
        ctx.arc(m.x, m.y, m.radius, 0, Math.PI * 2);
        ctx.fillStyle = rgba(m.color, Math.max(0, Math.min(1, m.alpha)) * 0.7);
        ctx.fill();
      }
    }
  }
}

function drawPlayerTrail(ctx, trailPoints, trailLength, selectedTrailId, isStreamContrail = false) {
  if (trailLength <= 1) return;
  const [trailStart, trailEnd] = getTrailPalette(selectedTrailId);
  const boosted = Boolean(isStreamContrail) || trailLength > 26;
  // Smooth continuous quadratic Bezier silk ribbon + thick, long Neon Contrail during vocal streams!
  ctx.save();
  ctx.lineCap = "round";
  ctx.lineJoin = "round";
  for (let i = 0; i < trailLength - 1; i++) {
    const p0 = trailPoints[i];
    const p1 = trailPoints[i + 1];
    const pNext = trailPoints[Math.min(trailLength - 1, i + 2)];
    const mid1X = (p0.x + p1.x) * 0.5;
    const mid1Y = (p0.y + p1.y) * 0.5;
    const mid2X = (p1.x + pNext.x) * 0.5;
    const mid2Y = (p1.y + pNext.y) * 0.5;
    const prog = i / trailLength;
    const tCol = lerpColor(trailStart, trailEnd, prog);
    const alpha = Math.pow(1 - prog, boosted ? 1.1 : 1.35) * (boosted ? 0.68 : 0.44);
    const r = (1 - prog) * (boosted ? 18 : 10) + (boosted ? 3.0 : 1.5);

    if (boosted) {
      ctx.beginPath();
      if (i === 0) {
        ctx.moveTo(p0.x, p0.y);
        ctx.quadraticCurveTo(p1.x, p1.y, mid2X, mid2Y);
      } else {
        ctx.moveTo(mid1X, mid1Y);
        ctx.quadraticCurveTo(p1.x, p1.y, mid2X, mid2Y);
      }
      ctx.strokeStyle = rgba([236, 72, 153], alpha * 0.35);
      ctx.lineWidth = r * 2.55;
      ctx.stroke();
    }

    ctx.beginPath();
    if (i === 0) {
      ctx.moveTo(p0.x, p0.y);
      ctx.quadraticCurveTo(p1.x, p1.y, mid2X, mid2Y);
    } else {
      ctx.moveTo(mid1X, mid1Y);
      ctx.quadraticCurveTo(p1.x, p1.y, mid2X, mid2Y);
    }
    ctx.strokeStyle = rgba(tCol, alpha);
    ctx.lineWidth = r * 1.85;
    ctx.stroke();

    if (boosted && i < trailLength - 2) {
      ctx.beginPath();
      if (i === 0) {
        ctx.moveTo(p0.x, p0.y);
        ctx.quadraticCurveTo(p1.x, p1.y, mid2X, mid2Y);
      } else {
        ctx.moveTo(mid1X, mid1Y);
        ctx.quadraticCurveTo(p1.x, p1.y, mid2X, mid2Y);
      }
      ctx.strokeStyle = rgba([255, 255, 255], alpha * 0.85);
      ctx.lineWidth = Math.max(2, r * 0.42);
      ctx.stroke();
    }

    if (i % (boosted ? 2 : 3) === 0) {
      ctx.beginPath();
      ctx.arc(p1.x, p1.y, r * (boosted ? 0.78 : 0.65), 0, Math.PI * 2);
      ctx.fillStyle = rgba([255, 255, 255], alpha * 0.82);
      ctx.fill();
    }
  }
  ctx.restore();
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

function computeHighwayPoint(laneFloat, u, w, h) {
  const horizonY = h * 0.15;
  // Raised Beat Strike Line to 65% of screen height so the bottom 35% is an open Thumb Steering Zone!
  const strikeY = h * 0.65;
  const topHalfW = w * 0.14;
  const strikeHalfW = w * 0.42;
  const clampedU = Math.max(-0.05, Math.min(1.32, u));
  const p = clampedU >= 0 ? Math.pow(clampedU, 1.25) : clampedU;
  const halfW = topHalfW + (strikeHalfW - topHalfW) * p;
  const laneNorm = ((laneFloat - 1.5) / 1.5) * 0.78;
  return {
    x: w * 0.5 + laneNorm * halfW,
    y: horizonY + (strikeY - horizonY) * p
  };
}

function computeHighwayScale(u) {
  const clampedU = Math.max(0, Math.min(1.25, u));
  return Math.max(0.42, Math.min(1.15, 0.44 + 0.56 * Math.pow(clampedU, 1.15)));
}

const HIGHWAY_LANE_COLORS = [
  [34, 197, 94],   // Lane 1: Emerald Fret
  [251, 113, 133], // Lane 2: Crimson Fret
  [250, 204, 21],  // Lane 3: Gold Fret
  [56, 189, 248]   // Lane 4: Cyan Fret
];

function drawWormholes(ctx, wormholes, time, beatPulse = 0, isBandHeroMode = false) {
  // Swooping slide ribbons removed in v11 — replaced by clean choreographed fluid peg formations!
}

function drawPowerUps(ctx, powerUps, playerX, playerY, time, trackSec, beatPulse, isConstellationActive, multiBand, isBandHeroMode = false) {
  const w = window.innerWidth || ctx.canvas.width;
  const h = window.innerHeight || ctx.canvas.height;
  if (w <= 0 || h <= 0) return;

  // =========================================================================
  // 1. 3D PERSPECTIVE BAND-HERO / GUITAR-HERO RHYTHM HIGHWAY & BEAT STRIKE LINE (Band Hero Mode ONLY!)
  // =========================================================================
  if (isBandHeroMode) {
    const topL = computeHighwayPoint(-0.45, 0, w, h);
    const topR = computeHighwayPoint(3.45, 0, w, h);
    const strikeL = computeHighwayPoint(-0.45, 1.0, w, h);
    const strikeR = computeHighwayPoint(3.45, 1.0, w, h);
    const botL = computeHighwayPoint(-0.45, 1.18, w, h);
    const botR = computeHighwayPoint(3.45, 1.18, w, h);

    ctx.save();
    const deckGrad = ctx.createLinearGradient(0, topL.y, 0, botL.y);
    deckGrad.addColorStop(0, "rgba(7, 10, 22, 0.22)");
    deckGrad.addColorStop(0.5, "rgba(10, 16, 34, 0.64)");
    deckGrad.addColorStop(0.85, "rgba(17, 24, 50, 0.78)");
    deckGrad.addColorStop(1, "rgba(7, 10, 22, 0.30)");

    ctx.beginPath();
    ctx.moveTo(topL.x, topL.y);
    ctx.lineTo(topR.x, topR.y);
    ctx.lineTo(botR.x, botR.y);
    ctx.lineTo(botL.x, botL.y);
    ctx.closePath();
    ctx.fillStyle = deckGrad;
    ctx.fill();

    const railCol = isConstellationActive ? [250, 204, 21] : [56, 189, 248];
    ctx.strokeStyle = rgba(railCol, 0.45 + beatPulse * 0.25);
    ctx.lineWidth = 3;
    ctx.lineCap = "round";
    ctx.beginPath();
    ctx.moveTo(topL.x, topL.y);
    ctx.lineTo(botL.x, botL.y);
    ctx.moveTo(topR.x, topR.y);
    ctx.lineTo(botR.x, botR.y);
    ctx.stroke();

    // Scrolling horizontal beat-measure bars synced to trackSec
    const phaseFrac = trackSec > 0 ? ((trackSec / 0.48387) % 1) : ((time * 2.06) % 1);
    for (let bIdx = 0; bIdx < 4; bIdx++) {
      const uBar = Math.max(0.02, Math.min(1.12, (bIdx + phaseFrac) / 4));
      const bL = computeHighwayPoint(-0.42, uBar, w, h);
      const bR = computeHighwayPoint(3.42, uBar, w, h);
      const barAlpha = Math.min(0.46, (0.10 + 0.26 * uBar) * (uBar >= 0.92 && uBar <= 1.05 ? 1.5 : 1.0));
      ctx.beginPath();
      ctx.moveTo(bL.x, bL.y);
      ctx.lineTo(bR.x, bR.y);
      ctx.strokeStyle = rgba([226, 232, 240], barAlpha);
      ctx.lineWidth = 1.3 + uBar * 1.6;
      ctx.stroke();
    }

    // 4 Perspective Fret Lane Strings
    for (let lane = 0; lane < 4; lane++) {
      const lTop = computeHighwayPoint(lane, 0, w, h);
      const lBot = computeHighwayPoint(lane, 1.16, w, h);
      const lCol = HIGHWAY_LANE_COLORS[lane];
      const lGrad = ctx.createLinearGradient(0, lTop.y, 0, lBot.y);
      lGrad.addColorStop(0, rgba(lCol, 0.14));
      lGrad.addColorStop(0.65, rgba(lCol, 0.45 + beatPulse * 0.18));
      lGrad.addColorStop(1, rgba(lCol, 0.72));
      ctx.beginPath();
      ctx.moveTo(lTop.x, lTop.y);
      ctx.lineTo(lBot.x, lBot.y);
      ctx.strokeStyle = lGrad;
      ctx.lineWidth = 2.4;
      ctx.stroke();
    }

    // GLOWING BEAT STRIKE LINE at u = 1.0 (Y = 65%)
    ctx.beginPath();
    ctx.moveTo(strikeL.x - 10, strikeL.y);
    ctx.lineTo(strikeR.x + 10, strikeR.y);
    ctx.strokeStyle = rgba(railCol, 0.30 + beatPulse * 0.25);
    ctx.lineWidth = 14 + beatPulse * 6;
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(strikeL.x, strikeL.y);
    ctx.lineTo(strikeR.x, strikeR.y);
    ctx.strokeStyle = rgba([255, 255, 255], 0.85 + beatPulse * 0.15);
    ctx.lineWidth = 3.2;
    ctx.stroke();

    // 4 Fret Receptor Rings on the Beat Strike Line
    for (let lane = 0; lane < 4; lane++) {
      const recPt = computeHighwayPoint(lane, 1.0, w, h);
      const lCol = HIGHWAY_LANE_COLORS[lane];
      const hasNoteOnLine = powerUps && powerUps.some((it) => {
        const lead = Math.max(0.45, (it.targetBeatSec || trackSec) - (it.spawnBeatSec || (trackSec - 1.8)));
        const uNote = (trackSec - (it.spawnBeatSec || 0)) / lead;
        return ((it.phraseBeatNum || 1) - 1) === lane && uNote >= 0.85 && uNote <= 1.14;
      });
      const recR = hasNoteOnLine ? (26 + beatPulse * 5) : (20 + beatPulse * 2);

      ctx.beginPath();
      ctx.arc(recPt.x, recPt.y, recR * 1.35, 0, Math.PI * 2);
      ctx.fillStyle = rgba(lCol, hasNoteOnLine ? 0.52 : 0.20);
      ctx.fill();

      ctx.beginPath();
      ctx.arc(recPt.x, recPt.y, recR, 0, Math.PI * 2);
      ctx.fillStyle = "rgba(9, 13, 26, 0.90)";
      ctx.fill();
      ctx.strokeStyle = hasNoteOnLine ? "#FFFFFF" : rgba(lCol, 0.86);
      ctx.lineWidth = hasNoteOnLine ? 4.0 : 2.5;
      ctx.stroke();

      ctx.beginPath();
      ctx.arc(recPt.x, recPt.y, recR * 0.38, 0, Math.PI * 2);
      ctx.fillStyle = rgba(lCol, hasNoteOnLine ? 0.96 : 0.45);
      ctx.fill();
    }

    ctx.restore();
  }

  if (!powerUps || powerUps.length === 0) return;
  const sorted = powerUps.slice().sort((a, b) => (a.sequenceIndex || 0) - (b.sequenceIndex || 0));

  // =========================================================================
  // 2. RENDER EACH STANDALONE 3D PULSING BEAT NOTE & LABELED PICKUP (Zero clutter!)
  // =========================================================================
  for (let idx = 0; idx < sorted.length; idx++) {
    const pu = sorted[idx];
    const leadDur = Math.max(0.45, (pu.targetBeatSec || trackSec) - (pu.spawnBeatSec || (trackSec - 1.8)));
    const u = Math.max(0, Math.min(1.25, (trackSec - (pu.spawnBeatSec || 0)) / leadDur));
    const perspScale = isBandHeroMode ? computeHighwayScale(u) : 0.92;

    const remTtl = pu.expireBeatSec > 0 ? Math.max(0, pu.expireBeatSec - trackSec) : (pu.maxLifetime - pu.age);
    const puAlpha = Math.max(0.20, Math.min(1, remTtl / 0.22));

    const isElectric = pu.type === "ELECTRIC_HARMONIC";
    const isPrism = pu.type === "PRISM_HARMONY";
    const isVortex = pu.type === "SLOW_MO";
    const isNova = pu.type === "BLAST" || pu.type === "OVERDRIVE";
    const isAegis = pu.type === "SHIELD";
    const isSpecialType = isElectric || isPrism || isVortex || isNova || isAegis;
    const isSupernova = !isSpecialType && (pu.isSupernova || pu.keyWordEffect === "CONSTELLATION_STAR");
    const laneIdx = Math.max(0, Math.min(3, (pu.phraseBeatNum || 1) - 1));

    const effectCol = isElectric
      ? [250, 204, 21]
      : isPrism
      ? [74, 222, 128]
      : isVortex
      ? [192, 132, 252]
      : isNova
      ? [249, 115, 22]
      : isAegis
      ? [56, 189, 248]
      : isSupernova
      ? [250, 204, 21]
      : HIGHWAY_LANE_COLORS[laneIdx];

    const isOnStrikeLine = isBandHeroMode ? (u >= 0.86 && u <= 1.14) : (beatPulse >= 0.45);
    const rhythmPulse = 1.0 + (isOnStrikeLine ? 0.20 : 0.12) * beatPulse + 0.08 * Math.sin(time * 8.0);
    const coreRad = (pu.radius || 32) * perspScale * rhythmPulse;

    const bounceOffsetY = -beatPulse * 6 * perspScale;
    const cx = pu.x;
    const cy = pu.y + bounceOffsetY;

    ctx.save();

    // Natural Physics Elastic Squish & Stretch Deformation
    if (pu.squish && Math.abs(pu.squish) > 0.01) {
      const sAngle = pu.squishAngle || 0;
      ctx.translate(cx, cy);
      ctx.rotate(sAngle);
      ctx.scale(1.0 + pu.squish, 1.0 - pu.squish * 0.7);
      ctx.rotate(-sAngle);
      ctx.translate(-cx, -cy);
    }

    // Subtle, clean contracting beat ring in Original mode
    if (!isBandHeroMode && !isSpecialType) {
      const ringRadius = coreRad * (1.06 + 0.52 * (1 - beatPulse));
      ctx.beginPath();
      ctx.arc(cx, cy, ringRadius, 0, Math.PI * 2);
      ctx.strokeStyle = rgba(beatPulse > 0.58 ? [255, 255, 255] : effectCol, (0.28 + 0.38 * beatPulse) * puAlpha);
      ctx.lineWidth = beatPulse > 0.58 ? 2.6 : 1.5;
      ctx.stroke();
    } else if (isBandHeroMode && isOnStrikeLine) {
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 1.38, 0, Math.PI * 2);
      ctx.strokeStyle = rgba([250, 204, 21], 0.82 * puAlpha);
      ctx.lineWidth = 3.0;
      ctx.stroke();
    }

    // Soft drop shadow
    ctx.beginPath();
    ctx.ellipse(
      pu.x,
      pu.y + coreRad * 0.62,
      coreRad * 0.82,
      coreRad * 0.28,
      0,
      0,
      Math.PI * 2
    );
    ctx.fillStyle = `rgba(0, 0, 0, ${(0.42 * puAlpha).toFixed(3)})`;
    ctx.fill();

    if (isElectric) {
      // =========================================================================
      // ⚡ CUSTOM SCULPTED HIGH-VOLTAGE PLASMA ORB
      // =========================================================================
      // 1. Radiant Electric Corona Bloom
      const glow = ctx.createRadialGradient(cx, cy, 2, cx, cy, coreRad * 2.50);
      glow.addColorStop(0, rgba([255, 255, 255], 0.98 * puAlpha));
      glow.addColorStop(0.30, rgba([250, 204, 21], 0.85 * puAlpha));
      glow.addColorStop(0.65, rgba([56, 189, 248], 0.45 * puAlpha));
      glow.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 2.50, 0, Math.PI * 2);
      ctx.fillStyle = glow;
      ctx.fill();

      // 2. Dual Counter-Rotating Magnetic Containment Rings
      ctx.beginPath();
      ctx.ellipse(cx, cy, coreRad * 1.38, coreRad * 0.46, time * 2.2, 0, Math.PI * 2);
      ctx.strokeStyle = rgba([250, 204, 21], 0.92 * puAlpha);
      ctx.lineWidth = 2.4;
      ctx.stroke();

      ctx.beginPath();
      ctx.ellipse(cx, cy, coreRad * 1.38, coreRad * 0.46, -time * 2.6 + 1.2, 0, Math.PI * 2);
      ctx.strokeStyle = rgba([56, 189, 248], 0.92 * puAlpha);
      ctx.lineWidth = 2.4;
      ctx.stroke();

      // Luminous containment charge nodes
      for (let n = 0; n < 3; n++) {
        const ang1 = time * 2.2 + n * (Math.PI * 2 / 3);
        const nx1 = cx + Math.cos(ang1) * (coreRad * 1.38);
        const ny1 = cy + Math.sin(ang1) * (coreRad * 0.46);
        ctx.beginPath();
        ctx.arc(nx1, ny1, 3.2, 0, Math.PI * 2);
        ctx.fillStyle = "#FFFFFF";
        ctx.shadowColor = "#FACC15";
        ctx.shadowBlur = 8;
        ctx.fill();
        ctx.shadowBlur = 0;
      }

      // 3. Volumetric Plasma Sphere Body
      const electricSphere = ctx.createRadialGradient(
        cx - coreRad * 0.32, cy - coreRad * 0.34, coreRad * 0.08,
        cx, cy, coreRad * 1.12
      );
      electricSphere.addColorStop(0, "#FFFFFF");
      electricSphere.addColorStop(0.24, "#FEF08A");
      electricSphere.addColorStop(0.60, "#06B6D4");
      electricSphere.addColorStop(1, "#082F49");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad, 0, Math.PI * 2);
      ctx.fillStyle = electricSphere;
      ctx.fill();
      ctx.strokeStyle = rgba([250, 204, 21], 0.98 * puAlpha);
      ctx.lineWidth = 3.2;
      ctx.stroke();

      // 4. Embossed ⚡ Voltage Glyph
      ctx.font = `900 ${Math.max(15, Math.round(coreRad * 0.92))}px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillStyle = `rgba(8, 47, 73, ${(0.80 * puAlpha).toFixed(3)})`;
      ctx.fillText("⚡", cx + 1, cy + 2);
      ctx.fillStyle = "#FFFFFF";
      ctx.shadowColor = "#FEF08A";
      ctx.shadowBlur = 10;
      ctx.fillText("⚡", cx, cy + 1);
      ctx.shadowBlur = 0;
    } else if (isPrism) {
      // =========================================================================
      // ✨ CUSTOM SCULPTED 3D FACETED CHROMATIC CRYSTAL GEMSTONE
      // =========================================================================
      // 1. Iridescent Prismatic Halo Bloom
      const glow = ctx.createRadialGradient(cx, cy, 2, cx, cy, coreRad * 2.40);
      glow.addColorStop(0, rgba([255, 255, 255], 0.98 * puAlpha));
      glow.addColorStop(0.35, rgba([74, 222, 128], 0.78 * puAlpha));
      glow.addColorStop(0.65, rgba([236, 72, 153], 0.48 * puAlpha));
      glow.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 2.40, 0, Math.PI * 2);
      ctx.fillStyle = glow;
      ctx.fill();

      // 2. Orbiting Iridescent Stardust Shards
      const shardCount = 4;
      for (let s = 0; s < shardCount; s++) {
        const sAng = time * 1.8 + s * (Math.PI * 2 / shardCount);
        const sDist = coreRad * (1.32 + 0.10 * Math.sin(time * 3 + s));
        const sx = cx + Math.cos(sAng) * sDist;
        const sy = cy + Math.sin(sAng) * (sDist * 0.65);
        ctx.beginPath();
        ctx.ellipse(sx, sy, 4.5, 2.2, sAng, 0, Math.PI * 2);
        ctx.fillStyle = s % 2 === 0 ? "#86EFAC" : "#F472B6";
        ctx.shadowColor = "#FFFFFF";
        ctx.shadowBlur = 6;
        ctx.fill();
        ctx.shadowBlur = 0;
      }

      // 3. Faceted Hexagonal Gemstone Geometry
      const facetColors = ["#86EFAC", "#38BDF8", "#F472B6", "#FACC15", "#A78BFA", "#67E8F9"];
      const outerVerts = [];
      const innerVerts = [];
      const numSides = 6;
      for (let i = 0; i < numSides; i++) {
        const ang = (i / numSides) * Math.PI * 2 - Math.PI / 6;
        outerVerts.push({
          x: cx + Math.cos(ang) * coreRad,
          y: cy + Math.sin(ang) * coreRad
        });
        innerVerts.push({
          x: cx + Math.cos(ang) * (coreRad * 0.52),
          y: cy + Math.sin(ang) * (coreRad * 0.52)
        });
      }

      // Draw outer trapezoid facets with chromatic dispersion
      for (let i = 0; i < numSides; i++) {
        const next = (i + 1) % numSides;
        ctx.beginPath();
        ctx.moveTo(outerVerts[i].x, outerVerts[i].y);
        ctx.lineTo(outerVerts[next].x, outerVerts[next].y);
        ctx.lineTo(innerVerts[next].x, innerVerts[next].y);
        ctx.lineTo(innerVerts[i].x, innerVerts[i].y);
        ctx.closePath();
        ctx.fillStyle = facetColors[i % facetColors.length];
        ctx.globalAlpha = 0.85 * puAlpha;
        ctx.fill();
        ctx.strokeStyle = "rgba(255, 255, 255, 0.75)";
        ctx.lineWidth = 1.2;
        ctx.stroke();
      }
      ctx.globalAlpha = 1.0;

      // Draw center crystal table with brilliant white-opal core
      ctx.beginPath();
      ctx.moveTo(innerVerts[0].x, innerVerts[0].y);
      for (let i = 1; i < numSides; i++) {
        ctx.lineTo(innerVerts[i].x, innerVerts[i].y);
      }
      ctx.closePath();
      const centerGrad = ctx.createRadialGradient(cx, cy, 1, cx, cy, coreRad * 0.55);
      centerGrad.addColorStop(0, "#FFFFFF");
      centerGrad.addColorStop(0.6, "#E0F2FE");
      centerGrad.addColorStop(1, "#BAE6FD");
      ctx.fillStyle = centerGrad;
      ctx.fill();
      ctx.strokeStyle = "#FFFFFF";
      ctx.lineWidth = 1.8;
      ctx.stroke();

      // Embossed ✨ Star Glyph
      ctx.font = `900 ${Math.max(14, Math.round(coreRad * 0.80))}px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillStyle = `rgba(15, 23, 42, ${(0.82 * puAlpha).toFixed(3)})`;
      ctx.fillText("✨", cx + 1, cy + 2);
      ctx.fillStyle = "#FFFFFF";
      ctx.shadowColor = "#86EFAC";
      ctx.shadowBlur = 8;
      ctx.fillText("✨", cx, cy + 1);
      ctx.shadowBlur = 0;
    } else if (isVortex) {
      // =========================================================================
      // 🌀 CUSTOM SCULPTED COSMIC SINGULARITY ORB
      // =========================================================================
      // 1. Deep Amethyst Gravitational Halo
      const glow = ctx.createRadialGradient(cx, cy, 2, cx, cy, coreRad * 2.35);
      glow.addColorStop(0, rgba([233, 213, 255], 0.95 * puAlpha));
      glow.addColorStop(0.40, rgba([192, 132, 252], 0.80 * puAlpha));
      glow.addColorStop(0.75, rgba([88, 28, 135], 0.45 * puAlpha));
      glow.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 2.35, 0, Math.PI * 2);
      ctx.fillStyle = glow;
      ctx.fill();

      // 2. Swirling Gravitational Accretion Disc (Spiral Nebula Dust)
      const armCount = 3;
      for (let a = 0; a < armCount; a++) {
        ctx.beginPath();
        const baseAng = a * (Math.PI * 2 / armCount) - time * 2.8;
        for (let pt = 0; pt < 16; pt++) {
          const tNorm = pt / 16;
          const r = coreRad * (0.35 + tNorm * 0.95);
          const ang = baseAng + tNorm * 2.8;
          const px = cx + Math.cos(ang) * r;
          const py = cy + Math.sin(ang) * (r * 0.55);
          if (pt === 0) ctx.moveTo(px, py);
          else ctx.lineTo(px, py);
        }
        ctx.strokeStyle = a % 2 === 0 ? "rgba(233, 213, 255, 0.85)" : "rgba(192, 132, 252, 0.80)";
        ctx.lineWidth = 2.4;
        ctx.stroke();
      }

      // 3. Gravitational Event Horizon Void Core
      const voidGrad = ctx.createRadialGradient(
        cx - coreRad * 0.15, cy - coreRad * 0.15, 2,
        cx, cy, coreRad * 0.75
      );
      voidGrad.addColorStop(0, "#E9D5FF");
      voidGrad.addColorStop(0.25, "#7E22CE");
      voidGrad.addColorStop(0.65, "#2E1065");
      voidGrad.addColorStop(1, "#030008");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 0.78, 0, Math.PI * 2);
      ctx.fillStyle = voidGrad;
      ctx.fill();

      // Luminous Starlight Lensing Rim
      ctx.strokeStyle = "rgba(233, 213, 255, 0.95)";
      ctx.lineWidth = 2.6;
      ctx.shadowColor = "#C084FC";
      ctx.shadowBlur = 12;
      ctx.stroke();
      ctx.shadowBlur = 0;

      // Embossed 🌀 Singularity Glyph
      ctx.font = `900 ${Math.max(14, Math.round(coreRad * 0.82))}px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillStyle = "#FFFFFF";
      ctx.fillText("🌀", cx, cy + 1);
    } else if (isNova) {
      // =========================================================================
      // 💥 CUSTOM SCULPTED THERMONUCLEAR MINIATURE STAR
      // =========================================================================
      // 1. Solar Corona Heatwave Bloom
      const glow = ctx.createRadialGradient(cx, cy, 2, cx, cy, coreRad * 2.40);
      glow.addColorStop(0, rgba([255, 255, 255], 0.98 * puAlpha));
      glow.addColorStop(0.30, rgba([250, 204, 21], 0.88 * puAlpha));
      glow.addColorStop(0.65, rgba([249, 115, 22], 0.52 * puAlpha));
      glow.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 2.40, 0, Math.PI * 2);
      ctx.fillStyle = glow;
      ctx.fill();

      // 2. Solar Prominence Plasma Loops leaping off surface
      const loopCount = 4;
      for (let l = 0; l < loopCount; l++) {
        const lAng = l * (Math.PI / 2) + time * 1.5;
        const lx1 = cx + Math.cos(lAng - 0.22) * (coreRad * 0.85);
        const ly1 = cy + Math.sin(lAng - 0.22) * (coreRad * 0.85);
        const lx2 = cx + Math.cos(lAng + 0.22) * (coreRad * 0.85);
        const ly2 = cy + Math.sin(lAng + 0.22) * (coreRad * 0.85);
        const apexX = cx + Math.cos(lAng) * (coreRad * 1.36);
        const apexY = cy + Math.sin(lAng) * (coreRad * 1.36);
        ctx.beginPath();
        ctx.moveTo(lx1, ly1);
        ctx.quadraticCurveTo(apexX, apexY, lx2, ly2);
        ctx.strokeStyle = "rgba(254, 240, 138, 0.95)";
        ctx.lineWidth = 2.8;
        ctx.stroke();
      }

      // 3. Molten Photosphere Sphere
      const sunGrad = ctx.createRadialGradient(
        cx - coreRad * 0.32, cy - coreRad * 0.34, coreRad * 0.08,
        cx, cy, coreRad * 1.10
      );
      sunGrad.addColorStop(0, "#FFFFFF");
      sunGrad.addColorStop(0.26, "#FEF08A");
      sunGrad.addColorStop(0.62, "#F97316");
      sunGrad.addColorStop(1, "#7C2D12");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad, 0, Math.PI * 2);
      ctx.fillStyle = sunGrad;
      ctx.fill();
      ctx.strokeStyle = "#FACC15";
      ctx.lineWidth = 3.2;
      ctx.stroke();

      // Embossed 💥 Solar Flare Glyph
      ctx.font = `900 ${Math.max(14, Math.round(coreRad * 0.82))}px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillStyle = "#FFFFFF";
      ctx.shadowColor = "#F97316";
      ctx.shadowBlur = 10;
      ctx.fillText("💥", cx, cy + 1);
      ctx.shadowBlur = 0;
    } else if (isAegis) {
      // 🛡️ AEGIS CRYSTAL SHIELD SPHERE (Luminous Aqua Shield Facets)
      const glow = ctx.createRadialGradient(cx, cy, 2, cx, cy, coreRad * 2.15);
      glow.addColorStop(0, rgba([56, 189, 248], 0.76 * puAlpha));
      glow.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 2.15, 0, Math.PI * 2);
      ctx.fillStyle = glow;
      ctx.fill();

      const shieldSphere = ctx.createRadialGradient(
        cx - coreRad * 0.34, cy - coreRad * 0.36, coreRad * 0.08,
        cx, cy, coreRad * 1.08
      );
      shieldSphere.addColorStop(0, rgba([255, 255, 255], 0.98 * puAlpha));
      shieldSphere.addColorStop(0.35, rgba([125, 211, 252], 0.95 * puAlpha));
      shieldSphere.addColorStop(0.75, rgba([2, 132, 199], 0.94 * puAlpha));
      shieldSphere.addColorStop(1, rgba([6, 21, 40], 0.98 * puAlpha));
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad, 0, Math.PI * 2);
      ctx.fillStyle = shieldSphere;
      ctx.fill();
      ctx.strokeStyle = rgba([255, 255, 255], 0.92 * puAlpha);
      ctx.lineWidth = 3.2;
      ctx.stroke();

      ctx.beginPath();
      ctx.ellipse(cx, cy, coreRad * 1.15, coreRad * 0.42, -0.25, 0, Math.PI * 2);
      ctx.strokeStyle = rgba([186, 230, 253], 0.88 * puAlpha);
      ctx.lineWidth = 2.4;
      ctx.stroke();
    } else if (isSupernova) {
      // ★ 3D VOLUMETRIC 24K GOLD STAR POWER SPHERE
      const glow = ctx.createRadialGradient(cx, cy, 2, cx, cy, coreRad * 2.3);
      glow.addColorStop(0, rgba([255, 255, 255], 0.96 * puAlpha));
      glow.addColorStop(0.45, rgba([250, 204, 21], 0.84 * puAlpha));
      glow.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 2.3, 0, Math.PI * 2);
      ctx.fillStyle = glow;
      ctx.fill();

      const starSphere = ctx.createRadialGradient(
        cx - coreRad * 0.34, cy - coreRad * 0.36, coreRad * 0.08,
        cx, cy, coreRad * 1.08
      );
      starSphere.addColorStop(0, "#FFFFFF");
      starSphere.addColorStop(0.32, "#FEF08A");
      starSphere.addColorStop(0.72, "#EAB308");
      starSphere.addColorStop(1, "#422006");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad, 0, Math.PI * 2);
      ctx.fillStyle = starSphere;
      ctx.fill();

      // Tilted 3D equatorial gyro-ring
      ctx.beginPath();
      ctx.ellipse(cx, cy, coreRad * 1.22, coreRad * 0.44, 0.22, 0, Math.PI * 2);
      ctx.strokeStyle = rgba([255, 255, 255], 0.92 * puAlpha);
      ctx.lineWidth = 2.8;
      ctx.stroke();

      // Embossed ★ inside sphere
      ctx.font = `900 ${Math.max(14, Math.round(coreRad * 0.92))}px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillStyle = "#422006";
      ctx.fillText("★", cx, cy + 1);
    } else {
      // =========================================================================
      // 🎵 BESPOKE REFRACTIVE COSMIC VINYL GLASS BEAD
      // =========================================================================
      // 1. Soft Ambient Bioluminescent Core Glow
      const glow = ctx.createRadialGradient(cx, cy, 2, cx, cy, coreRad * 2.05);
      glow.addColorStop(0, rgba(effectCol, 0.78 * puAlpha));
      glow.addColorStop(0.55, rgba(effectCol, 0.24 * puAlpha));
      glow.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 2.05, 0, Math.PI * 2);
      ctx.fillStyle = glow;
      ctx.fill();

      // 2. Off-Center 3D Spherical Refraction Lighting
      const darkRim = [
        Math.round(effectCol[0] * 0.14),
        Math.round(effectCol[1] * 0.14),
        Math.round(effectCol[2] * 0.20)
      ];
      const sphere3D = ctx.createRadialGradient(
        cx - coreRad * 0.35, cy - coreRad * 0.35, coreRad * 0.06,
        cx, cy, coreRad * 1.15
      );
      sphere3D.addColorStop(0, rgba([255, 255, 255], 0.98 * puAlpha));
      sphere3D.addColorStop(0.24, rgba(effectCol, 0.98 * puAlpha));
      sphere3D.addColorStop(0.72, rgba(effectCol, 0.88 * puAlpha));
      sphere3D.addColorStop(1, rgba(darkRim, 0.98 * puAlpha));

      ctx.beginPath();
      ctx.arc(cx, cy, coreRad, 0, Math.PI * 2);
      ctx.fillStyle = sphere3D;
      ctx.fill();

      // 3. Etched Concentric Vinyl Grooves inside the glass bead
      for (let g = 1; g <= 3; g++) {
        ctx.beginPath();
        ctx.arc(cx, cy, coreRad * (0.35 + g * 0.18), 0, Math.PI * 2);
        ctx.strokeStyle = `rgba(255, 255, 255, ${(0.14 * puAlpha).toFixed(3)})`;
        ctx.lineWidth = 1.0;
        ctx.stroke();
      }

      // 4. Outer 3D Polished Chrome Rim
      ctx.strokeStyle = isOnStrikeLine ? "#FFFFFF" : rgba(effectCol, 0.96 * puAlpha);
      ctx.lineWidth = isOnStrikeLine ? 3.8 : 2.6;
      ctx.stroke();

      // 5. Tilted 3D Equatorial Gyro-Ring wrapping the bead
      ctx.beginPath();
      ctx.ellipse(
        cx,
        cy + coreRad * 0.04,
        coreRad * (1.14 + beatPulse * 0.10),
        coreRad * 0.38,
        (laneIdx % 2 === 0 ? -0.18 : 0.18),
        0,
        Math.PI * 2
      );
      ctx.strokeStyle = rgba([255, 255, 255], (0.58 + beatPulse * 0.30) * puAlpha);
      ctx.lineWidth = Math.max(1.8, 2.4 * perspScale);
      ctx.stroke();

      // 6. Refractive Glass Crescent Specular Glint
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 0.78, Math.PI * 1.08, Math.PI * 1.62);
      ctx.strokeStyle = rgba([255, 255, 255], 0.92 * puAlpha);
      ctx.lineWidth = Math.max(2.2, 3.4 * perspScale);
      ctx.lineCap = "round";
      ctx.stroke();

      // 7. Embedded Luminous Musical Note Emblem (Floating Hologram)
      const noteGlyph = laneIdx % 2 === 0 ? "♪" : "♫";
      ctx.font = `900 ${Math.max(13, Math.round(coreRad * 0.82))}px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillStyle = `rgba(4, 8, 20, ${(0.76 * puAlpha).toFixed(3)})`;
      ctx.fillText(noteGlyph, cx + 1, cy + 2);
      ctx.fillStyle = `rgba(255, 255, 255, ${(0.98 * puAlpha).toFixed(3)})`;
      ctx.shadowColor = rgba(effectCol, 0.8);
      ctx.shadowBlur = 6;
      ctx.fillText(noteGlyph, cx, cy + 1);
      ctx.shadowBlur = 0;
      ctx.textBaseline = "alphabetic";
    }
    ctx.restore();
  }
}

function drawHexGem(ctx, x, y, r, col, kind, t, alpha, phraseBeatNum = 1) {
  // Smooth Spherical Glass / Plasma Orb (Zero straight polygon edges!)
  const glow = ctx.createRadialGradient(x, y, 2, x, y, r * 1.95);
  glow.addColorStop(0, rgba(col, 0.48 * alpha));
  glow.addColorStop(0.55, rgba(col, 0.16 * alpha));
  glow.addColorStop(1, "rgba(0,0,0,0)");
  ctx.beginPath();
  ctx.arc(x, y, r * 1.95, 0, Math.PI * 2);
  ctx.fillStyle = glow;
  ctx.fill();

  // Volumetric off-center spherical shading
  const sphereGrad = ctx.createRadialGradient(x - r * 0.28, y - r * 0.28, r * 0.08, x, y, r);
  sphereGrad.addColorStop(0, rgba([Math.min(255, col[0] + 65), Math.min(255, col[1] + 65), Math.min(255, col[2] + 65)], 0.52 * alpha));
  sphereGrad.addColorStop(0.55, rgba([10, 18, 36], 0.92 * alpha));
  sphereGrad.addColorStop(1, rgba([5, 9, 20], 0.96 * alpha));
  ctx.beginPath();
  ctx.arc(x, y, r * 0.96, 0, Math.PI * 2);
  ctx.fillStyle = sphereGrad;
  ctx.fill();

  // Smooth luminous outer ring
  ctx.beginPath();
  ctx.arc(x, y, r * 0.96, 0, Math.PI * 2);
  ctx.strokeStyle = rgba(col, 0.94 * alpha);
  ctx.lineWidth = 2.4;
  ctx.stroke();

  // Soft curved upper-left glass specular crescent
  ctx.beginPath();
  ctx.arc(x, y, r * 0.74, Math.PI * 1.12, Math.PI * 1.54);
  ctx.strokeStyle = rgba([255, 255, 255], 0.72 * alpha);
  ctx.lineWidth = 2.2;
  ctx.lineCap = "round";
  ctx.stroke();

  // Inner harmonic ring
  ctx.beginPath();
  ctx.arc(x, y, r * 0.58, 0, Math.PI * 2);
  ctx.strokeStyle = rgba(col, 0.42 * alpha);
  ctx.lineWidth = 1.2;
  ctx.stroke();

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

