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
      if (m.isStarfall) {
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

function drawWormholes(ctx, wormholes, time, beatPulse = 0) {
  if (!wormholes || wormholes.length === 0) return;
  const w = window.innerWidth || ctx.canvas.width;
  const h = window.innerHeight || ctx.canvas.height;
  for (let i = 0; i < wormholes.length; i++) {
    const wh = wormholes[i];
    if (wh.age >= wh.maxLifetime) continue;

    const travelU = Math.min(1.0, wh.age / 1.8);
    if (travelU <= 0.02) continue;

    const startLane = Math.max(0, Math.min(3, typeof wh.startLane === "number" ? wh.startLane : 0));
    const endLane = Math.max(0, Math.min(3, typeof wh.endLane === "number" ? wh.endLane : 3));
    const leftPt = computeHighwayPoint(startLane, travelU, w, h);
    const rightPt = computeHighwayPoint(endLane, travelU, w, h);
    const scale = computeHighwayScale(travelU);
    const holdProgress = Math.max(0, Math.min(1, wh.holdProgress || 0));
    const isHolding = holdProgress > 0.02;

    ctx.save();
    ctx.lineCap = "round";
    ctx.lineJoin = "round";

    // 1. "Ooh / Aaah" Vocal Aurora Borealis Curtains across the upper stage while holding!
    if (isHolding) {
      const auroraAlpha = Math.max(0.18, Math.min(0.68, holdProgress * 0.65));
      for (let band = 0; band < 5; band++) {
        const bx = w * (0.15 + band * 0.175 + Math.sin(time * 2.2 + band) * 0.04);
        const bw = w * 0.13;
        const bh = h * (0.38 + holdProgress * 0.24);
        const bCol = band % 2 === 0 ? [56, 189, 248] : (band % 3 === 0 ? [250, 204, 21] : [236, 72, 153]);
        const aGrad = ctx.createLinearGradient(0, 0, 0, bh);
        aGrad.addColorStop(0, rgba(bCol, auroraAlpha));
        aGrad.addColorStop(0.6, rgba([168, 85, 247], auroraAlpha * 0.45));
        aGrad.addColorStop(1, "rgba(0,0,0,0)");
        ctx.fillStyle = aGrad;
        ctx.fillRect(bx - bw * 0.5, 0, bw, bh);
      }
    }

    // 2. Electric / Liquid Plasma Bridge connecting the Left & Right Sustain Pegs
    const segments = 18;
    for (let strand = 0; strand < 3; strand++) {
      ctx.beginPath();
      const freq = strand === 0 ? 3.0 : (strand === 1 ? 5.0 : 7.0);
      const amp = (strand === 0 ? 14 : (strand === 1 ? 22 : 10)) * scale * (isHolding ? 1.55 : 0.85);
      const speed = strand === 0 ? 8.5 : (strand === 1 ? -11.2 : 14.8);
      for (let s = 0; s <= segments; s++) {
        const frac = s / segments;
        const bx = leftPt.x + (rightPt.x - leftPt.x) * frac;
        const by = leftPt.y + (rightPt.y - leftPt.y) * frac;
        const env = Math.sin(frac * Math.PI);
        const waveY = Math.sin(frac * Math.PI * freq + time * speed + strand * 2.1) * amp * env;
        if (s === 0) ctx.moveTo(bx, by + waveY);
        else ctx.lineTo(bx, by + waveY);
      }
      const strandCol = strand === 0 ? [236, 72, 153] : (strand === 1 ? [56, 189, 248] : [255, 255, 255]);
      const strandW = (strand === 0 ? 22 : (strand === 1 ? 12 : 4.5)) * scale * (1 + holdProgress * 0.35);
      ctx.strokeStyle = rgba(strandCol, strand === 2 ? 0.96 : (0.55 + holdProgress * 0.35));
      ctx.lineWidth = strandW;
      ctx.stroke();
    }

    // 3. Flowing Plasma Energy Pearls racing back and forth between the two Sustain Pegs
    const numPearls = 6;
    for (let pIdx = 0; pIdx < numPearls; pIdx++) {
      const f = ((pIdx / numPearls) + time * (isHolding ? 2.2 : 1.1)) % 1.0;
      const px = leftPt.x + (rightPt.x - leftPt.x) * f;
      const py = leftPt.y + (rightPt.y - leftPt.y) * f + Math.sin(f * Math.PI * 4 + time * 10) * 10 * scale * Math.sin(f * Math.PI);
      const pr = (7.5 + beatPulse * 3 + holdProgress * 4) * scale;
      ctx.beginPath();
      ctx.arc(px, py, pr * 1.7, 0, Math.PI * 2);
      ctx.fillStyle = rgba([56, 189, 248], 0.72);
      ctx.fill();
      ctx.beginPath();
      ctx.arc(px, py, pr * 0.85, 0, Math.PI * 2);
      ctx.fillStyle = "#FFFFFF";
      ctx.fill();
    }

    // 4. Render Both Paired 3D Sustain Hold Pegs (Left Anchor & Right Anchor)
    const anchors = [
      { pt: leftPt, col: [56, 189, 248] },
      { pt: rightPt, col: [250, 204, 21] }
    ];
    const pegR = 32 * scale * (1 + beatPulse * 0.18 + holdProgress * 0.22);

    for (let aIdx = 0; aIdx < anchors.length; aIdx++) {
      const anchor = anchors[aIdx];
      const pt = anchor.pt;
      const col = anchor.col;

      // Special Effect on Held Pegs: Spinning Liquid-Plasma Vortex Rings
      const vortexRings = isHolding ? 3 : 1;
      for (let v = 0; v < vortexRings; v++) {
        const vr = pegR * (1.35 + v * 0.34 + 0.12 * Math.sin(time * 9 + v));
        const spinA = ((time * (v % 2 === 0 ? 220 : -260) + v * 90) % 360) * Math.PI / 180;
        ctx.beginPath();
        ctx.arc(pt.x, pt.y, vr, spinA, spinA + (isHolding ? 230 : 160) * Math.PI / 180);
        ctx.strokeStyle = rgba(v % 2 === 0 ? col : [236, 72, 153], isHolding ? 0.88 - v * 0.2 : 0.55);
        ctx.lineWidth = (isHolding ? 5.0 : 3.0) * scale;
        ctx.stroke();
      }

      // Radial Hold Progress Gauge (0% -> 100% ring around each peg)
      if (holdProgress > 0.01) {
        ctx.beginPath();
        ctx.arc(pt.x, pt.y, pegR * 1.55, -Math.PI / 2, -Math.PI / 2 + holdProgress * Math.PI * 2);
        ctx.strokeStyle = "#4ADE80";
        ctx.lineWidth = 6.5 * scale;
        ctx.stroke();
      }

      // 3D Volumetric Sustain Sphere
      const sphereGrad = ctx.createRadialGradient(
        pt.x - pegR * 0.32, pt.y - pegR * 0.32, pegR * 0.08,
        pt.x, pt.y, pegR
      );
      sphereGrad.addColorStop(0, "#FFFFFF");
      sphereGrad.addColorStop(0.45, rgba(col, 1));
      sphereGrad.addColorStop(0.85, "#EC4899");
      sphereGrad.addColorStop(1, "#0F172A");
      ctx.beginPath();
      ctx.arc(pt.x, pt.y, pegR, 0, Math.PI * 2);
      ctx.fillStyle = sphereGrad;
      ctx.fill();
      ctx.strokeStyle = "#FFFFFF";
      ctx.lineWidth = 3.2 * scale;
      ctx.stroke();
    }

    // 5. Clear Center Callout & Live Hold Percentage
    const midX = (leftPt.x + rightPt.x) * 0.5;
    const midY = Math.min(leftPt.y, rightPt.y) - pegR * 1.45;
    const pctInt = Math.round(holdProgress * 100);
    const labelText = isHolding
      ? `⚡ SUSTAIN CHARGING: ${pctInt}% ⚡`
      : "⚡ HOLD 2 FINGERS ON VOCAL CHORD ⚡";

    ctx.font = `900 ${Math.max(12, Math.round(14 * scale))}px -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif`;
    ctx.textAlign = "center";
    ctx.fillStyle = isHolding ? "#4ADE80" : "#FEF08A";
    ctx.fillText(labelText, midX, midY);
    ctx.restore();
  }
}

function drawPowerUps(ctx, powerUps, playerX, playerY, time, trackSec, beatPulse, isConstellationActive, multiBand, isBandHeroMode = false) {
  const w = window.innerWidth || ctx.canvas.width;
  const h = window.innerHeight || ctx.canvas.height;
  if (w <= 0 || h <= 0) return;

  // Ethereal "Ooh / Aaah" Vocal Aurora Borealis Curtains & Harmonic Halos during long flowy vocal swells!
  const vLevel = multiBand && typeof multiBand.vocal === "number" ? multiBand.vocal : 0;
  const bLevel = multiBand && typeof multiBand.bass === "number" ? multiBand.bass : 0.4;
  const vocalGlow = (bLevel < 0.22 && vLevel >= 0.34)
    ? Math.min(1, Math.max(0.25, (vLevel - 0.28) * 1.6))
    : Math.min(1, Math.max(0, (vLevel - 0.50) * 2.2));
  if (vocalGlow > 0.04) {
    ctx.save();
    ctx.globalCompositeOperation = "lighter";
    const curtainCols = [[56, 189, 248], [192, 132, 252], [253, 224, 71]];
    for (let c = 0; c < 4; c++) {
      const baseY = h * (0.11 + c * 0.045);
      ctx.beginPath();
      ctx.moveTo(0, baseY);
      for (let step = 1; step <= 12; step++) {
        const xPt = w * (step / 12);
        const yWave = baseY + Math.sin(time * (1.8 + c * 0.35) + step * 0.65 + c) * (18 + 16 * vocalGlow);
        ctx.lineTo(xPt, yWave);
      }
      ctx.strokeStyle = rgba(curtainCols[c % 3], (0.16 + 0.26 * vocalGlow) * (1 - c * 0.16));
      ctx.lineWidth = 5 + (3 - c) * 3.5;
      ctx.lineCap = "round";
      ctx.stroke();
    }
    ctx.beginPath();
    ctx.arc(w * 0.5, h * 0.20, w * (0.24 + 0.06 * Math.sin(time * 2.4)), 0, Math.PI * 2);
    ctx.strokeStyle = rgba([192, 132, 252], 0.22 * vocalGlow);
    ctx.lineWidth = 2.4;
    ctx.stroke();
    ctx.restore();
  }

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

    ctx.font = "900 11px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif";
    ctx.textAlign = "center";
    ctx.fillStyle = "rgba(250, 204, 21, 0.82)";
    ctx.fillText("★ BEAT STRIKE LINE • CATCH NOTES HERE ON TIME ★", w * 0.5, strikeL.y + 34);
    ctx.restore();
  }

  if (!powerUps || powerUps.length === 0) return;
  const sorted = powerUps.slice().sort((a, b) => (a.sequenceIndex || 0) - (b.sequenceIndex || 0));

  // =========================================================================
  // 1B. ORIGINAL FREE-ROAM MODE: GLOWING NEON CONTRAIL STREAM RIBBON BETWEEN ORBS!
  // =========================================================================
  if (!isBandHeroMode && sorted.length >= 2) {
    ctx.save();
    ctx.lineCap = "round";
    ctx.lineJoin = "round";
    for (let i = 0; i < sorted.length - 1; i++) {
      const a = sorted[i];
      const b = sorted[i + 1];
      const isStreamPair = a.isVocalStream || b.isVocalStream || vocalGlow > 0.10;
      const dx = b.x - a.x;
      const dy = b.y - a.y;
      const dist = Math.hypot(dx, dy);
      if (dist <= w * 0.68) {
        const midX = (a.x + b.x) * 0.5 - (dy * 0.14);
        const midY = (a.y + b.y) * 0.5 + (dx * 0.14);
        ctx.beginPath();
        ctx.moveTo(a.x, a.y);
        ctx.quadraticCurveTo(midX, midY, b.x, b.y);
        ctx.strokeStyle = rgba(isStreamPair ? [236, 72, 153] : [56, 189, 248], isStreamPair ? (0.48 + beatPulse * 0.25) : 0.20);
        ctx.lineWidth = isStreamPair ? (11 + beatPulse * 4) : 3.5;
        ctx.stroke();

        if (isStreamPair) {
          ctx.beginPath();
          ctx.moveTo(a.x, a.y);
          ctx.quadraticCurveTo(midX, midY, b.x, b.y);
          ctx.strokeStyle = rgba([255, 255, 255], 0.78);
          ctx.lineWidth = 2.6;
          ctx.stroke();
        }
      }
    }
    ctx.restore();
  }

  // =========================================================================
  // 2. RENDER EACH SCROLLING HIGHWAY BEAT NOTE & LABELED PICKUP (BOMB / SHIELD)
  // =========================================================================
  for (let idx = 0; idx < sorted.length; idx++) {
    const pu = sorted[idx];
    const leadDur = Math.max(0.45, (pu.targetBeatSec || trackSec) - (pu.spawnBeatSec || (trackSec - 1.8)));
    const u = Math.max(0, Math.min(1.25, (trackSec - (pu.spawnBeatSec || 0)) / leadDur));
    const perspScale = isBandHeroMode ? computeHighwayScale(u) : 0.95;

    const remTtl = pu.expireBeatSec > 0 ? Math.max(0, pu.expireBeatSec - trackSec) : (pu.maxLifetime - pu.age);
    const puAlpha = Math.max(0.20, Math.min(1, remTtl / 0.22));

    const isBomb = pu.type === "BLAST" || pu.type === "OVERDRIVE";
    const isShield = pu.type === "SHIELD";
    const isSupernova = !isBomb && !isShield && (pu.isSupernova || pu.keyWordEffect === "CONSTELLATION_STAR");
    const laneIdx = Math.max(0, Math.min(3, (pu.phraseBeatNum || 1) - 1));
    const effectCol = isBomb
      ? [249, 115, 22]
      : isShield
      ? [56, 189, 248]
      : isSupernova
      ? [250, 204, 21]
      : HIGHWAY_LANE_COLORS[laneIdx];

    const isOnStrikeLine = isBandHeroMode ? (u >= 0.86 && u <= 1.14) : (beatPulse >= 0.45);
    const beatPulseScale = 1.0 + (isOnStrikeLine ? 0.30 : 0.16) * beatPulse;
    const coreRad = (pu.radius || 32) * perspScale * beatPulseScale;

    // 3D Vertical Beat-Bounce: orbs physically lift off the stage on every beat!
    const bounceOffsetY = -beatPulse * 11 * perspScale;
    const cx = pu.x;
    const cy = pu.y + bounceOffsetY;

    ctx.save();
    // In Original Free-Roam 8-Beat Mode: draw a contracting Beat-Sync Ring on EVERY beat!
    if (!isBandHeroMode) {
      const ringRadius = coreRad * (1.05 + 0.95 * (1 - beatPulse));
      ctx.beginPath();
      ctx.arc(cx, cy, ringRadius, 0, Math.PI * 2);
      ctx.strokeStyle = rgba(beatPulse > 0.55 ? [255, 255, 255] : effectCol, (0.35 + 0.55 * beatPulse) * puAlpha);
      ctx.lineWidth = beatPulse > 0.55 ? 3.5 : 2.0;
      ctx.stroke();
    }
    // 1. 3D Perspective Ground Drop-Shadow Ellipse on the Highway Deck beneath the floating 3D sphere
    ctx.beginPath();
    ctx.ellipse(
      pu.x,
      pu.y + coreRad * 0.65,
      coreRad * (0.92 - beatPulse * 0.14),
      coreRad * 0.34,
      0,
      0,
      Math.PI * 2
    );
    ctx.fillStyle = `rgba(0, 0, 0, ${(0.52 * puAlpha).toFixed(3)})`;
    ctx.fill();

    // Vertical Fret Flame Column when crossing the Beat Strike Line!
    if (isOnStrikeLine) {
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 1.48, 0, Math.PI * 2);
      ctx.strokeStyle = rgba([250, 204, 21], 0.90 * puAlpha);
      ctx.lineWidth = 3.6;
      ctx.stroke();

      const flameGrad = ctx.createLinearGradient(cx, cy - coreRad * 2.8, cx, cy);
      flameGrad.addColorStop(0, "rgba(0,0,0,0)");
      flameGrad.addColorStop(0.55, rgba(effectCol, 0.68 * puAlpha));
      flameGrad.addColorStop(1, rgba([255, 255, 255], 0.94 * puAlpha));
      ctx.beginPath();
      ctx.moveTo(cx, cy - coreRad * 2.8);
      ctx.lineTo(cx, cy);
      ctx.strokeStyle = flameGrad;
      ctx.lineWidth = coreRad * 0.84;
      ctx.lineCap = "round";
      ctx.stroke();
    }

    if (isBomb) {
      // 💥 3D VOLUMETRIC BOMB SPHERE (LABELED ENEMY-DESTROYER ORDNANCE)
      const glow = ctx.createRadialGradient(cx, cy, 2, cx, cy, coreRad * 2.25);
      glow.addColorStop(0, rgba([250, 204, 21], 0.88 * puAlpha));
      glow.addColorStop(0.5, rgba([249, 115, 22], 0.54 * puAlpha));
      glow.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 2.25, 0, Math.PI * 2);
      ctx.fillStyle = glow;
      ctx.fill();

      for (let nub = 0; nub < 8; nub++) {
        const ang = nub * (Math.PI / 4) + time * 1.8;
        ctx.beginPath();
        ctx.moveTo(cx + Math.cos(ang) * (coreRad * 0.85), cy + Math.sin(ang) * (coreRad * 0.85));
        ctx.lineTo(cx + Math.cos(ang) * (coreRad * 1.32), cy + Math.sin(ang) * (coreRad * 1.32));
        ctx.strokeStyle = rgba([250, 204, 21], 0.96 * puAlpha);
        ctx.lineWidth = Math.max(2.8, 4.4 * perspScale);
        ctx.lineCap = "round";
        ctx.stroke();
      }

      const bombSphere = ctx.createRadialGradient(
        cx - coreRad * 0.34, cy - coreRad * 0.36, coreRad * 0.08,
        cx, cy, coreRad * 1.08
      );
      bombSphere.addColorStop(0, rgba([255, 255, 255], 0.98 * puAlpha));
      bombSphere.addColorStop(0.28, rgba([250, 204, 21], 0.96 * puAlpha));
      bombSphere.addColorStop(0.65, rgba([234, 88, 12], 0.96 * puAlpha));
      bombSphere.addColorStop(1, rgba([30, 8, 8], 0.98 * puAlpha));
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad, 0, Math.PI * 2);
      ctx.fillStyle = bombSphere;
      ctx.fill();
      ctx.strokeStyle = rgba([250, 204, 21], 0.98 * puAlpha);
      ctx.lineWidth = 3.2;
      ctx.stroke();

      ctx.font = `900 ${Math.max(12, Math.round(15 * perspScale))}px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`;
      ctx.textAlign = "center";
      ctx.fillStyle = "#FFFFFF";
      ctx.fillText("💥 BOMB", cx, cy - coreRad * 1.42);
    } else if (isShield) {
      // 🛡️ 3D VOLUMETRIC SHIELD SPHERE (LABELED PROTECTIVE DOME)
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

      ctx.font = `900 ${Math.max(12, Math.round(15 * perspScale))}px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`;
      ctx.textAlign = "center";
      ctx.fillStyle = "#BAE6FD";
      ctx.fillText("🛡️ SHIELD", cx, cy - coreRad * 1.36);
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

      ctx.font = `900 ${Math.max(12, Math.round(14.5 * perspScale))}px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`;
      ctx.textBaseline = "alphabetic";
      ctx.fillStyle = "#FEF08A";
      ctx.fillText("★ 4X STAR", cx, cy - coreRad * 1.38);
    } else {
      // TRUE 3D VOLUMETRIC PULSING BEAT ORB (Spherical Shading + Equatorial Gyro-Ring + Embossed ♪)
      const glow = ctx.createRadialGradient(cx, cy, 2, cx, cy, coreRad * 1.95);
      glow.addColorStop(0, rgba(effectCol, 0.72 * puAlpha));
      glow.addColorStop(0.55, rgba(effectCol, 0.25 * puAlpha));
      glow.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 1.95, 0, Math.PI * 2);
      ctx.fillStyle = glow;
      ctx.fill();

      // Off-center 3D spherical radial lighting (bright top-left specular to deep bottom-right shadow)
      const darkRim = [
        Math.round(effectCol[0] * 0.16),
        Math.round(effectCol[1] * 0.16),
        Math.round(effectCol[2] * 0.22)
      ];
      const sphere3D = ctx.createRadialGradient(
        cx - coreRad * 0.34, cy - coreRad * 0.36, coreRad * 0.08,
        cx, cy, coreRad * 1.12
      );
      sphere3D.addColorStop(0, rgba([255, 255, 255], 0.98 * puAlpha));
      sphere3D.addColorStop(0.28, rgba(effectCol, 0.96 * puAlpha));
      sphere3D.addColorStop(0.74, rgba(effectCol, 0.88 * puAlpha));
      sphere3D.addColorStop(1, rgba(darkRim, 0.98 * puAlpha));

      ctx.beginPath();
      ctx.arc(cx, cy, coreRad, 0, Math.PI * 2);
      ctx.fillStyle = sphere3D;
      ctx.fill();

      // Outer 3D rim ring
      ctx.strokeStyle = isOnStrikeLine ? "#FFFFFF" : rgba(effectCol, 0.96 * puAlpha);
      ctx.lineWidth = isOnStrikeLine ? 3.8 : 2.6;
      ctx.stroke();

      // Tilted 3D equatorial gyro-ring wrapping the orb
      ctx.beginPath();
      ctx.ellipse(
        cx,
        cy + coreRad * 0.05,
        coreRad * (1.14 + beatPulse * 0.10),
        coreRad * 0.38,
        (laneIdx % 2 === 0 ? -0.18 : 0.18),
        0,
        Math.PI * 2
      );
      ctx.strokeStyle = rgba([255, 255, 255], (0.58 + beatPulse * 0.30) * puAlpha);
      ctx.lineWidth = Math.max(1.8, 2.4 * perspScale);
      ctx.stroke();

      // Upper-left curved glass specular highlight arc
      ctx.beginPath();
      ctx.arc(cx, cy, coreRad * 0.76, Math.PI * 1.08, Math.PI * 1.62);
      ctx.strokeStyle = rgba([255, 255, 255], 0.88 * puAlpha);
      ctx.lineWidth = Math.max(2.2, 3.2 * perspScale);
      ctx.lineCap = "round";
      ctx.stroke();

      // Embossed ♪ Musical Note Emblem inside the 3D sphere
      ctx.font = `900 ${Math.max(13, Math.round(coreRad * 0.82))}px -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillStyle = `rgba(6, 10, 25, ${(0.72 * puAlpha).toFixed(3)})`;
      ctx.fillText("♪", cx + 1, cy + 2);
      ctx.fillStyle = `rgba(255, 255, 255, ${(0.96 * puAlpha).toFixed(3)})`;
      ctx.fillText("♪", cx, cy + 1);
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

