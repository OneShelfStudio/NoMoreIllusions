// ============================================================================
// NO MORE ILLUSIONS — BACKGROUND & VOLUMETRIC NEBULA RENDERER
// ============================================================================

function lerpColor(c1, c2, t) {
  const clampT = Math.max(0, Math.min(1, t));
  return [
    Math.round(c1[0] + (c2[0] - c1[0]) * clampT),
    Math.round(c1[1] + (c2[1] - c1[1]) * clampT),
    Math.round(c1[2] + (c2[2] - c1[2]) * clampT)
  ];
}

function rgb(c) {
  if (Array.isArray(c)) return `rgb(${c[0]}, ${c[1]}, ${c[2]})`;
  return c;
}

function rgba(rgbArr, a) {
  if (typeof rgbArr === "string") return rgbArr;
  return `rgba(${rgbArr[0]}, ${rgbArr[1]}, ${rgbArr[2]}, ${Math.max(0, Math.min(1, a))})`;
}

function roundRectPath(ctx, x, y, w, h, r) {
  const rad = Math.max(0, Math.min(r, w / 2, h / 2));
  ctx.beginPath();
  ctx.moveTo(x + rad, y);
  ctx.arcTo(x + w, y, x + w, y + h, rad);
  ctx.arcTo(x + w, y + h, x, y + h, rad);
  ctx.arcTo(x, y + h, x, y, rad);
  ctx.arcTo(x, y, x + w, y, rad);
  ctx.closePath();
}

function getThemePalette(bgTheme, isFocus = false) {
  switch (bgTheme) {
    case "CYBER":
    case "theme_matrix":
      return {
        c1: isFocus ? "#020705" : "#010403",
        c2: isFocus ? "#05180E" : "#031109",
        c3: isFocus ? "#071D12" : "#04120B",
        bgTop: [2, 7, 5], bgMid: [5, 24, 14], bgBot: [7, 29, 18],
        nebTint: [34, 197, 94], starTint: [129, 140, 248]
      };
    case "SUNSET":
    case "theme_sunset":
      return {
        c1: isFocus ? "#07020A" : "#040106",
        c2: isFocus ? "#14051C" : "#0D0314",
        c3: isFocus ? "#090418" : "#05020E",
        bgTop: [7, 2, 10], bgMid: [20, 5, 28], bgBot: [9, 4, 24],
        nebTint: [168, 85, 247], starTint: [244, 63, 94]
      };
    case "PRISM":
      return {
        c1: isFocus ? "#03030C" : "#020208",
        c2: isFocus ? "#090820" : "#060516",
        c3: isFocus ? "#04140C" : "#020C07",
        bgTop: [3, 3, 12], bgMid: [9, 8, 32], bgBot: [4, 20, 12],
        nebTint: [99, 102, 241], starTint: [74, 222, 128]
      };
    case "GOLD_VINYL":
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
    default: // OBSIDIAN / theme_nebula
      return {
        c1: isFocus ? "#020504" : "#010302",
        c2: isFocus ? "#040D08" : "#030805",
        c3: isFocus ? "#04030E" : "#020208",
        bgTop: [2, 5, 4], bgMid: [4, 13, 8], bgBot: [4, 3, 14],
        nebTint: [56, 189, 248], starTint: [168, 85, 247]
      };
  }
}

// 1:1 Port of BackgroundRenderer.kt
function drawNebulaBackground(ctx, W, H, palette, stars, time, beatPulse, multiBand, isEclipseActive, isConstellationActive, trackArtwork, trackProgress, specialVisuals = {}) {
  const energy = Math.max(0.08, Math.min(1, multiBand.rms || 0.35));
  const bass = Math.max(0, Math.min(1, multiBand.bass || 0.30));
  const vocal = Math.max(0.05, Math.min(1, multiBand.vocal || 0.30));
  const high = Math.max(0, Math.min(1, multiBand.high || 0.25));
  const swellBoost = Math.max(0, Math.min(0.7, (multiBand.swellDelta || 0.1) * 2.4));

  const isElectric = Boolean(specialVisuals.isElectric);
  const isPrism = Boolean(specialVisuals.isPrism);
  const isSlowMo = Boolean(specialVisuals.isSlowMo);
  const novaFlash = Math.max(0, Math.min(1, specialVisuals.solarNovaFlash || 0));
  const unblurPercent = Math.max(0.12, Math.min(1.0, specialVisuals.unblurPercent || 0.20));

  // 1. Deep Obsidian Space Base (with Dynamic Special Phase Tints)
  const bgGrad = ctx.createLinearGradient(0, 0, 0, H);
  if (isElectric) {
    bgGrad.addColorStop(0, "#083344"); // Electric Cyan/Teal
    bgGrad.addColorStop(0.5, "#1e1b4b"); // Deep Indigo
    bgGrad.addColorStop(1, "#422006"); // Gold Amber Underglow
  } else if (isPrism) {
    bgGrad.addColorStop(0, "#1e1b4b"); // Iridescent Prism Purple
    bgGrad.addColorStop(0.5, "#064e3b"); // Emerald Mid
    bgGrad.addColorStop(1, "#831843"); // Magenta Horizon
  } else if (isSlowMo) {
    bgGrad.addColorStop(0, "#3b0764"); // Deep Royal Amethyst Void
    bgGrad.addColorStop(0.5, "#180629");
    bgGrad.addColorStop(1, "#030108");
  } else {
    bgGrad.addColorStop(0, palette.c1);
    bgGrad.addColorStop(0.5, palette.c2);
    bgGrad.addColorStop(1, palette.c3);
  }
  ctx.fillStyle = bgGrad;
  ctx.fillRect(0, 0, W, H);

  // 2. Parallax Starfield (Rainbow diamonds during Prism, Gold motes during Electric)
  if (stars && stars.length) {
    for (let i = 0; i < stars.length; i++) {
      const s = stars[i];
      const tw = 0.5 + 0.5 * Math.sin(time * 3 + s.twinkleOffset);
      const starCol = isPrism
        ? (i % 3 === 0 ? [74, 222, 128] : i % 3 === 1 ? [236, 72, 153] : [56, 189, 248])
        : isElectric
        ? (i % 2 === 0 ? [250, 204, 21] : [56, 189, 248])
        : palette.starTint;
      ctx.beginPath();
      ctx.arc(s.x, s.y, s.size * (0.8 + 0.3 * tw), 0, Math.PI * 2);
      ctx.fillStyle = rgba(starCol, s.alpha * (0.4 + 0.6 * tw));
      ctx.fill();
    }
  }

  // 3. Sub-Bass Gravity Expanding Shock-Rings
  const ringPeriod = isSlowMo ? 4.8 : 2.4;
  for (let r = 0; r < 3; r++) {
    const rPhase = ((time * (isSlowMo ? 0.22 : 0.45) + r * (ringPeriod / 3)) % ringPeriod) / ringPeriod;
    const currentR = rPhase * maxDim * 0.65;
    const ringAlpha = Math.max(0, Math.min(1, Math.sin(rPhase * Math.PI))) * (0.10 + bass * 0.22);
    const ringColor = isElectric ? [250, 204, 21] : isPrism ? [74, 222, 128] : palette.nebTint;
    ctx.beginPath();
    ctx.arc(centerX, centerY, currentR, 0, Math.PI * 2);
    ctx.strokeStyle = rgba(ringColor, ringAlpha);
    ctx.lineWidth = 1.2 + bass * 2.5;
    ctx.stroke();
  }

  // 4. Organic Aurora Silk Waves (Electric Synth Aurora or Prismatic Ribbon Curtains)
  const ribCount = isElectric ? 5 : 3;
  for (let rib = 0; rib < ribCount; rib++) {
    const ribbonY = centerY - H * 0.25 + rib * (H * 0.12);
    const waveFreq = 0.0032 + rib * 0.0009;
    const waveAmp = (26 + rib * 16) * (1 + bass * 0.48 + vocal * 0.25) * (isElectric ? 1.6 : 1.0);
    const speed = time * ((isSlowMo ? 0.5 : 1.05) + rib * 0.42);
    const ribCol = isElectric
      ? (rib % 2 === 0 ? [250, 204, 21] : [56, 189, 248])
      : isPrism
      ? (rib % 3 === 0 ? [74, 222, 128] : rib % 3 === 1 ? [236, 72, 153] : [56, 189, 248])
      : (rib % 2 === 0 ? palette.nebTint : palette.starTint);
    const ribAlpha = (0.07 + energy * 0.16) * (isElectric ? 1.6 : 1.0) * (rib === 1 ? 1.25 : 0.85);

    ctx.beginPath();
    let prevX = 0;
    let prevY = ribbonY + Math.sin(speed) * waveAmp;
    ctx.moveTo(prevX, prevY);
    const step = Math.max(48, W / 8);
    for (let x = step; x <= W + step; x += step) {
      const currX = Math.min(W, x);
      const currY = ribbonY + Math.sin(currX * waveFreq + speed) * waveAmp + Math.cos(currX * waveFreq * 0.5 - speed * 0.7) * (waveAmp * 0.35);
      const midX = (prevX + currX) * 0.5;
      const midY = (prevY + currY) * 0.5;
      ctx.quadraticCurveTo(prevX, prevY, midX, midY);
      prevX = currX;
      prevY = currY;
    }
    ctx.lineTo(prevX, prevY);

    // Outer soft volumetric aurora glow
    ctx.strokeStyle = rgba(ribCol, ribAlpha * 0.55);
    ctx.lineWidth = (isElectric ? 22 : 14) + bass * 12;
    ctx.lineCap = "round";
    ctx.stroke();

    // Inner silk core
    ctx.strokeStyle = rgba(isElectric ? [255, 255, 255] : ribCol, ribAlpha * 1.2);
    ctx.lineWidth = 3.5 + high * 2.5;
    ctx.stroke();
  }

  // Volumetric Chorus Radial Blooms (Replaces harsh ceiling laser lines)
  if (isConstellationActive || isElectric || isPrism || energy > 0.62) {
    const bloomCount = 4;
    for (let b = 0; b < bloomCount; b++) {
      const bx = W * (0.18 + b * 0.22) + Math.sin(time * 0.9 + b * 2.1) * (W * 0.08);
      const by = H * (0.24 + (b % 2) * 0.16) + Math.cos(time * 0.7 + b) * (H * 0.06);
      const bRad = maxDim * (0.24 + bass * 0.10 + beatPulse * 0.06);
      const bCol = isElectric
        ? (b % 2 === 0 ? [250, 204, 21] : [56, 189, 248])
        : isPrism
        ? (b % 2 === 0 ? [74, 222, 128] : [236, 72, 153])
        : (isConstellationActive ? [250, 204, 21] : (b % 2 === 0 ? palette.nebTint : palette.starTint));
      const bGrad = ctx.createRadialGradient(bx, by, 4, bx, by, bRad);
      bGrad.addColorStop(0, rgba(bCol, (isElectric || isPrism ? 0.28 : 0.16) + energy * 0.12));
      bGrad.addColorStop(0.55, rgba(bCol, 0.08));
      bGrad.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(bx, by, bRad, 0, Math.PI * 2);
      ctx.fillStyle = bGrad;
      ctx.fill();
    }
  }

  // 4b. Dynamic Special Phase Transformations (Full-Screen Visual Overhauls)
  if (isElectric) {
    // ⚡ ELECTRIC OVERDRIVE: 3D Synthwave Perspective Floor Grid + Hyperspace Warp Rays + Branching Plasma
    ctx.save();

    // 1. 3D Perspective Neon Synthwave Ground Grid
    const horizonY = centerY + H * 0.08;
    const gridDepth = H - horizonY;
    if (gridDepth > 20) {
      ctx.beginPath();
      // Draw perspective longitudinal lines vanishing to center
      const lineCount = 14;
      for (let l = 0; l <= lineCount; l++) {
        const xBottom = -W * 0.2 + (W * 1.4) * (l / lineCount);
        ctx.moveTo(centerX, horizonY);
        ctx.lineTo(xBottom, H);
      }
      // Draw horizontal receding transverse lines with speed
      const transCount = 10;
      const scrollOffset = (time * 1.8) % 1.0;
      for (let t = 0; t < transCount; t++) {
        const norm = (t + scrollOffset) / transCount;
        const curY = horizonY + Math.pow(norm, 2.2) * gridDepth;
        ctx.moveTo(0, curY);
        ctx.lineTo(W, curY);
      }
      ctx.strokeStyle = "rgba(250, 204, 21, 0.28)";
      ctx.lineWidth = 1.4;
      ctx.stroke();

      // Neon horizon glow bar
      const horizGrad = ctx.createLinearGradient(0, horizonY - 12, 0, horizonY + 18);
      horizGrad.addColorStop(0, "rgba(56, 189, 248, 0)");
      horizGrad.addColorStop(0.5, "rgba(250, 204, 21, 0.42)");
      horizGrad.addColorStop(1, "rgba(56, 189, 248, 0)");
      ctx.fillStyle = horizGrad;
      ctx.fillRect(0, horizonY - 12, W, 30);
    }

    // 2. Hyperspace Star-Warp Velocity Streaks
    const warpCount = 24;
    for (let w = 0; w < warpCount; w++) {
      const warpAngle = (w / warpCount) * Math.PI * 2 + time * 0.15;
      const warpLen = (40 + 75 * beatPulse) * (0.8 + 0.4 * ((w % 3) / 2));
      const distFromCenter = ((time * 0.9 + w * 0.13) % 1.0) * maxDim * 0.72 + 25;
      const sx = centerX + Math.cos(warpAngle) * distFromCenter;
      const sy = centerY + Math.sin(warpAngle) * distFromCenter;
      const ex = centerX + Math.cos(warpAngle) * (distFromCenter + warpLen);
      const ey = centerY + Math.sin(warpAngle) * (distFromCenter + warpLen);

      ctx.beginPath();
      ctx.moveTo(sx, sy);
      ctx.lineTo(ex, ey);
      ctx.strokeStyle = w % 2 === 0 ? "rgba(250, 204, 21, 0.75)" : "rgba(56, 189, 248, 0.75)";
      ctx.lineWidth = 2.2;
      ctx.stroke();
    }

    // 3. Multi-Branching Atmospheric Lightning Arcs
    for (let arc = 0; arc < 3; arc++) {
      const startX = ((arc * 0.33 + (time * 0.24) % 1.0) % 1.0) * W;
      const arcSeed = Math.floor(time * 18 + arc * 89);
      let curX = startX;
      let curY = 0;
      ctx.beginPath();
      ctx.moveTo(curX, curY);
      for (let s = 1; s <= 8; s++) {
        curY += H / 8;
        const jitter = Math.sin(arcSeed + s * 4.8) * (W * 0.08);
        curX += jitter;
        ctx.lineTo(curX, curY);
      }
      ctx.strokeStyle = arc % 2 === 0 ? "rgba(250, 204, 21, 0.85)" : "rgba(56, 189, 248, 0.90)";
      ctx.lineWidth = 2.8;
      ctx.shadowColor = arc % 2 === 0 ? "#FACC15" : "#38BDF8";
      ctx.shadowBlur = 18;
      ctx.stroke();
    }
    ctx.restore();
  }

  if (isPrism) {
    // ✨ PRISM VOCAL BLOOM: Chromatic Dispersion Ripple Waves + Sacred Geometry Mandala + Iridescent Rays
    ctx.save();

    // 1. Concentric Chromatic Dispersion Ripple Waves (pulsing with Ethan's vocal energy)
    const waveCount = 5;
    for (let w = 0; w < waveCount; w++) {
      const wProg = ((time * 0.35 + w / waveCount) % 1.0);
      const wRad = wProg * maxDim * 0.80;
      const wAlpha = (1.0 - wProg) * (0.35 + vocal * 0.45);
      const colors = ["#4ADE80", "#38BDF8", "#EC4899", "#FACC15"];
      const col = colors[w % colors.length];

      ctx.beginPath();
      ctx.arc(centerX, centerY, wRad, 0, Math.PI * 2);
      ctx.strokeStyle = col;
      ctx.globalAlpha = Math.max(0, Math.min(1, wAlpha));
      ctx.lineWidth = 3.2 * (1.0 - wProg * 0.5);
      ctx.shadowColor = col;
      ctx.shadowBlur = 14;
      ctx.stroke();
    }
    ctx.globalAlpha = 1.0;

    // 2. Sacred Geometry Kaleidoscope / Mandala Rings (Interlocking Petals)
    const petalRings = 3;
    for (let r = 0; r < petalRings; r++) {
      const pRad = maxDim * (0.22 + r * 0.14) * (1.0 + vocal * 0.12);
      const pCount = 6 + r * 2;
      const rot = time * (r % 2 === 0 ? 0.28 : -0.22);
      ctx.beginPath();
      for (let p = 0; p < pCount; p++) {
        const ang = rot + (p / pCount) * Math.PI * 2;
        const px = centerX + Math.cos(ang) * pRad;
        const py = centerY + Math.sin(ang) * pRad;
        ctx.arc(px, py, pRad * 0.42, 0, Math.PI * 2);
      }
      ctx.strokeStyle = r === 0 ? "rgba(236, 72, 153, 0.45)" : r === 1 ? "rgba(74, 222, 128, 0.38)" : "rgba(56, 189, 248, 0.32)";
      ctx.lineWidth = 1.8;
      ctx.stroke();
    }

    // 3. Rotating Rainbow Prismatic Diffraction Beams
    const beamCount = 16;
    for (let b = 0; b < beamCount; b++) {
      const beamAngle = time * 0.40 + (b * (Math.PI * 2 / beamCount));
      const bCol = b % 4 === 0 ? [74, 222, 128] : b % 4 === 1 ? [56, 189, 248] : b % 4 === 2 ? [236, 72, 153] : [250, 204, 21];
      ctx.beginPath();
      ctx.moveTo(centerX, centerY);
      ctx.arc(centerX, centerY, maxDim * 0.85, beamAngle - 0.06, beamAngle + 0.06);
      ctx.closePath();
      ctx.fillStyle = rgba(bCol, 0.15 + 0.08 * Math.sin(time * 5.0 + b));
      ctx.fill();
    }
    ctx.restore();
  }

  if (isSlowMo) {
    // 🌀 VORTEX SLOW-MO: Gravitational Accretion Disc + Time-Dilation Horizon Waves + Royal Void
    ctx.save();

    // 1. Spacetime Gravitational Accretion Swirling Spiral Arms
    const armCount = 4;
    const spiralPoints = 28;
    for (let a = 0; a < armCount; a++) {
      ctx.beginPath();
      const armOffset = a * (Math.PI * 2 / armCount) + time * 0.32;
      for (let s = 0; s < spiralPoints; s++) {
        const st = s / spiralPoints;
        const sDist = st * maxDim * 0.70;
        const sAngle = armOffset + st * 4.2;
        const sx = centerX + Math.cos(sAngle) * sDist;
        const sy = centerY + Math.sin(sAngle) * sDist;
        if (s === 0) ctx.moveTo(sx, sy);
        else ctx.lineTo(sx, sy);
      }
      ctx.strokeStyle = a % 2 === 0 ? "rgba(192, 132, 252, 0.52)" : "rgba(147, 51, 234, 0.40)";
      ctx.lineWidth = 3.6;
      ctx.shadowColor = "#C084FC";
      ctx.shadowBlur = 16;
      ctx.stroke();
    }

    // 2. Gravitational Time-Dilation Event Horizon Expansion Rings
    for (let v = 0; v < 5; v++) {
      const vRadius = ((time * 0.14 + v * 0.20) % 1.0) * maxDim * 0.75;
      const vFade = 1.0 - (vRadius / (maxDim * 0.75));
      ctx.beginPath();
      ctx.arc(centerX, centerY, vRadius, 0, Math.PI * 2);
      ctx.strokeStyle = `rgba(192, 132, 252, ${0.45 * vFade})`;
      ctx.lineWidth = 4.0 * vFade;
      ctx.stroke();
    }
    ctx.restore();
  }

  // 5. Organic Harmonic Halo Petal Rings
  const ringCount = isEclipseActive ? 4 : 6;
  const ringSpeed = multiBand.bassDropOut ? 0.035 : ((isSlowMo ? 0.03 : 0.06) + bass * 0.10 + high * 0.05);
  for (let i = 0; i < ringCount; i++) {
    const rawPhase = ((i / ringCount) + time * ringSpeed) % 1;
    const curvedPhase = rawPhase * rawPhase;
    const ringRadius = curvedPhase * maxDim * (0.58 + bass * 0.06);
    if (ringRadius < 18) continue;

    const isEven = i % 2 === 0;
    const baseRingColor = isElectric
      ? (isEven ? [250, 204, 21] : [56, 189, 248])
      : isPrism
      ? (isEven ? [74, 222, 128] : [236, 72, 153])
      : isConstellationActive
      ? (isEven ? [250, 204, 21] : [56, 189, 248])
      : (isEven ? palette.nebTint : palette.starTint);

    const fadeAlpha = Math.max(0, Math.min(1, Math.sin(rawPhase * Math.PI)));
    const finalAlpha = Math.max(0, Math.min(0.40, fadeAlpha * (0.06 + energy * 0.18 + swellBoost * 0.12)));
    const strokeW = 1.4 + rawPhase * (1.6 + bass * 2.2);
    const baseSpin = time * (isEven ? 0.38 : -0.32) + i * 0.75;

    for (let p = 0; p < 3; p++) {
      const startA = baseSpin + p * ((Math.PI * 2) / 3);
      const sweepA = (74 + vocal * 24) * (Math.PI / 180);
      ctx.beginPath();
      ctx.arc(centerX, centerY, ringRadius, startA, startA + sweepA);
      ctx.strokeStyle = rgba(baseRingColor, finalAlpha);
      ctx.lineWidth = strokeW;
      ctx.lineCap = "round";
      ctx.stroke();
    }
  }

  // 6. Progressive Waterfall Album Artwork Fill in Center (Top-to-Bottom Fill & Splash Meniscus)
  const effectiveProgress = Math.max(0.02, Math.min(1.0, (trackProgress || 0) + (specialVisuals.prismSurgeBonus || 0)));
  const artBoxSize = Math.min(W, H) * (0.52 + bass * 0.05);
  const artRadius = artBoxSize * 0.48;
  const artTop = centerY - artRadius;
  const artLeft = centerX - artRadius;
  const waterLevelY = artTop + (artRadius * 2) * effectiveProgress;

  // 6a. Dark Frosted Disc Silhouette & Concentric Vinyl Grooves (Unrevealed Base)
  ctx.save();
  ctx.beginPath();
  ctx.arc(centerX, centerY, artRadius, 0, Math.PI * 2);
  ctx.fillStyle = "rgba(7, 11, 22, 0.72)";
  ctx.fill();
  ctx.strokeStyle = "rgba(100, 116, 139, 0.35)";
  ctx.lineWidth = 2.0;
  ctx.stroke();

  for (let g = 1; g <= 4; g++) {
    ctx.beginPath();
    ctx.arc(centerX, centerY, artRadius * (g / 5), 0, Math.PI * 2);
    ctx.strokeStyle = "rgba(148, 163, 184, 0.08)";
    ctx.lineWidth = 1.0;
    ctx.stroke();
  }
  ctx.restore();

  // 6b. Waterfall Clipped Album Artwork revealing from top down to waterLevelY
  if (trackArtwork && trackArtwork.complete) {
    ctx.save();
    // Circular Vinyl Bounds Clip
    ctx.beginPath();
    ctx.arc(centerX, centerY, artRadius, 0, Math.PI * 2);
    ctx.clip();

    // Waterfall Vertical Threshold Clip (top down to waterLevelY)
    ctx.beginPath();
    ctx.rect(centerX - artRadius - 10, artTop - 10, artRadius * 2 + 20, (artRadius * 2) * effectiveProgress + 10);
    ctx.clip();

    // Draw high-vibrance artwork inside the waterfall revealed region
    ctx.globalAlpha = Math.min(1.0, 0.82 + effectiveProgress * 0.18 + (isPrism ? 0.15 : 0));
    ctx.drawImage(trackArtwork, artLeft, artTop, artRadius * 2, artRadius * 2);
    ctx.restore();

    // 6c. Glowing undulating liquid meniscus wave across waterLevelY
    if (effectiveProgress > 0.02 && effectiveProgress < 0.995) {
      ctx.save();
      ctx.beginPath();
      ctx.arc(centerX, centerY, artRadius, 0, Math.PI * 2);
      ctx.clip();

      const waveCol = isPrism ? [74, 222, 128] : (isElectric ? [250, 204, 21] : [56, 189, 248]);
      ctx.beginPath();
      const waveStep = 6;
      let first = true;
      for (let wx = artLeft - 10; wx <= artLeft + artRadius * 2 + 10; wx += waveStep) {
        const waveOffset = Math.sin(wx * 0.055 + time * 7.5) * 3.5 + Math.cos(wx * 0.028 - time * 5.0) * 2.0;
        const wy = waterLevelY + waveOffset;
        if (first) {
          ctx.moveTo(wx, wy);
          first = false;
        } else {
          ctx.lineTo(wx, wy);
        }
      }
      ctx.strokeStyle = rgba(waveCol, 0.95);
      ctx.lineWidth = 3.5;
      ctx.shadowColor = rgba(waveCol, 0.85);
      ctx.shadowBlur = 14;
      ctx.stroke();

      // Bright white-hot crest
      ctx.strokeStyle = "rgba(255, 255, 255, 0.9)";
      ctx.lineWidth = 1.5;
      ctx.stroke();

      // Waterfall spray droplets trickling along the filling meniscus
      const dropletCount = 6;
      for (let d = 0; d < dropletCount; d++) {
        const dx = artLeft + ((d * 47 + time * 85) % (artRadius * 2));
        const dy = waterLevelY + ((d * 19 + time * 95) % 18) - 4;
        ctx.beginPath();
        ctx.arc(dx, dy, 2.0, 0, Math.PI * 2);
        ctx.fillStyle = rgba(waveCol, 0.82);
        ctx.fill();
      }
      ctx.restore();
    } else if (effectiveProgress >= 0.995) {
      // 100% UNLOCKED CROWN GLOW
      ctx.save();
      ctx.beginPath();
      ctx.arc(centerX, centerY, artRadius, 0, Math.PI * 2);
      ctx.strokeStyle = rgba([250, 204, 21], 0.75 + 0.25 * Math.sin(time * 6));
      ctx.lineWidth = 3.5;
      ctx.shadowColor = "rgba(250, 204, 21, 0.85)";
      ctx.shadowBlur = 18;
      ctx.stroke();
      ctx.restore();
    }
  }

  // 7. Full-Screen Solar Nova Blast Wave (Blinding White-Hot Expanding Corona Shockwave)
  if (novaFlash > 0.01) {
    const novaRadius = (1 - novaFlash) * maxDim * 1.15;
    ctx.save();
    const novaGrad = ctx.createRadialGradient(centerX, centerY, 5, centerX, centerY, Math.max(10, novaRadius));
    novaGrad.addColorStop(0, rgba([255, 255, 255], novaFlash * 0.92));
    novaGrad.addColorStop(0.25, rgba([254, 240, 138], novaFlash * 0.75));
    novaGrad.addColorStop(0.55, rgba([250, 204, 21], novaFlash * 0.55));
    novaGrad.addColorStop(0.85, rgba([249, 115, 22], novaFlash * 0.35));
    novaGrad.addColorStop(1, "rgba(0,0,0,0)");
    ctx.fillStyle = novaGrad;
    ctx.fillRect(0, 0, W, H);

    // Blinding white-hot shockwave rim
    ctx.beginPath();
    ctx.arc(centerX, centerY, Math.max(10, novaRadius), 0, Math.PI * 2);
    ctx.strokeStyle = `rgba(255, 255, 255, ${novaFlash * 0.95})`;
    ctx.lineWidth = 6.0 * novaFlash;
    ctx.shadowColor = "#FACC15";
    ctx.shadowBlur = 24;
    ctx.stroke();
    ctx.restore();
  }
}


