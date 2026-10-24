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
function drawNebulaBackground(ctx, W, H, palette, stars, time, beatPulse, multiBand, isEclipseActive, isConstellationActive, trackArtwork, trackProgress) {
  const energy = Math.max(0.08, Math.min(1, multiBand.rms || 0.35));
  const bass = Math.max(0, Math.min(1, multiBand.bass || 0.30));
  const vocal = Math.max(0.05, Math.min(1, multiBand.vocal || 0.30));
  const high = Math.max(0, Math.min(1, multiBand.high || 0.25));
  const swellBoost = Math.max(0, Math.min(0.7, (multiBand.swellDelta || 0.1) * 2.4));

  // 1. Deep Obsidian Space Base
  const bgGrad = ctx.createLinearGradient(0, 0, 0, H);
  bgGrad.addColorStop(0, palette.c1);
  bgGrad.addColorStop(0.5, palette.c2);
  bgGrad.addColorStop(1, palette.c3);
  ctx.fillStyle = bgGrad;
  ctx.fillRect(0, 0, W, H);

  const centerX = W * 0.5;
  const centerY = H * 0.46;
  const maxDim = Math.max(W, H);

  // 2. Parallax Starfield
  if (stars && stars.length) {
    for (let i = 0; i < stars.length; i++) {
      const s = stars[i];
      const tw = 0.5 + 0.5 * Math.sin(time * 3 + s.twinkleOffset);
      ctx.beginPath();
      ctx.arc(s.x, s.y, s.size * (0.8 + 0.3 * tw), 0, Math.PI * 2);
      ctx.fillStyle = rgba(palette.starTint, s.alpha * (0.4 + 0.6 * tw));
      ctx.fill();
    }
  }

  // 3. Sub-Bass Gravity Expanding Shock-Rings
  const ringPeriod = 2.4;
  for (let r = 0; r < 3; r++) {
    const rPhase = ((time * 0.45 + r * (ringPeriod / 3)) % ringPeriod) / ringPeriod;
    const currentR = rPhase * maxDim * 0.65;
    const ringAlpha = Math.max(0, Math.min(1, Math.sin(rPhase * Math.PI))) * (0.10 + bass * 0.22);
    ctx.beginPath();
    ctx.arc(centerX, centerY, currentR, 0, Math.PI * 2);
    ctx.strokeStyle = rgba(palette.nebTint, ringAlpha);
    ctx.lineWidth = 1.2 + bass * 2.5;
    ctx.stroke();
  }

  // 4. Organic Aurora Silk Waves (Smooth Quadratic Bezier Curves + Volumetric Chorus Blooms)
  for (let rib = 0; rib < 3; rib++) {
    const ribbonY = centerY - H * 0.20 + rib * (H * 0.14);
    const waveFreq = 0.0032 + rib * 0.0009;
    const waveAmp = (26 + rib * 16) * (1 + bass * 0.48 + vocal * 0.25);
    const speed = time * (1.05 + rib * 0.42);
    const ribCol = rib % 2 === 0 ? palette.nebTint : palette.starTint;
    const ribAlpha = (0.07 + energy * 0.16) * (rib === 1 ? 1.25 : 0.85);

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
    ctx.strokeStyle = rgba(ribCol, ribAlpha * 0.42);
    ctx.lineWidth = 14 + bass * 10;
    ctx.lineCap = "round";
    ctx.stroke();

    // Inner silk core
    ctx.strokeStyle = rgba(ribCol, ribAlpha);
    ctx.lineWidth = 3.2 + high * 2.5;
    ctx.stroke();
  }

  // Volumetric Chorus Radial Blooms (Replaces harsh ceiling laser lines)
  if (isConstellationActive || energy > 0.62) {
    const bloomCount = 3;
    for (let b = 0; b < bloomCount; b++) {
      const bx = W * (0.22 + b * 0.28) + Math.sin(time * 0.9 + b * 2.1) * (W * 0.08);
      const by = H * (0.24 + (b % 2) * 0.16) + Math.cos(time * 0.7 + b) * (H * 0.06);
      const bRad = maxDim * (0.22 + bass * 0.10 + beatPulse * 0.05);
      const bCol = isConstellationActive ? [250, 204, 21] : (b % 2 === 0 ? palette.nebTint : palette.starTint);
      const bGrad = ctx.createRadialGradient(bx, by, 4, bx, by, bRad);
      bGrad.addColorStop(0, rgba(bCol, 0.16 + energy * 0.12));
      bGrad.addColorStop(0.55, rgba(bCol, 0.06));
      bGrad.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(bx, by, bRad, 0, Math.PI * 2);
      ctx.fillStyle = bGrad;
      ctx.fill();
    }
  }

  // 5. Organic Harmonic Halo Petal Rings (Replaces boxy rectangular squircle tunnel wireframes)
  const ringCount = isEclipseActive ? 4 : 6;
  const ringSpeed = multiBand.bassDropOut ? 0.035 : (0.06 + bass * 0.10 + high * 0.05);
  for (let i = 0; i < ringCount; i++) {
    const rawPhase = ((i / ringCount) + time * ringSpeed) % 1;
    const curvedPhase = rawPhase * rawPhase;
    const ringRadius = curvedPhase * maxDim * (0.58 + bass * 0.06);
    if (ringRadius < 18) continue;

    const isEven = i % 2 === 0;
    const baseRingColor = isConstellationActive
      ? (isEven ? [250, 204, 21] : [56, 189, 248])
      : multiBand.bassDropOut
      ? [56, 189, 248]
      : (isEven ? palette.nebTint : palette.starTint);

    const fadeAlpha = Math.max(0, Math.min(1, Math.sin(rawPhase * Math.PI)));
    const finalAlpha = Math.max(0, Math.min(0.32, fadeAlpha * (0.05 + energy * 0.18 + swellBoost * 0.12)));
    const strokeW = 1.2 + rawPhase * (1.5 + bass * 2.2);
    const baseSpin = time * (isEven ? 0.38 : -0.32) + i * 0.75;

    // 3 soft rotating crescent arcs per halo ring
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

  // 6. Progressive Album Artwork Hologram Watermark Reveal in Center
  if (trackArtwork && trackArtwork.complete && trackProgress > 0.01) {
    const p = Math.max(0, Math.min(1, trackProgress));
    const artBoxSize = Math.min(W, H) * (0.46 + bass * 0.04);
    ctx.save();
    ctx.globalCompositeOperation = "screen";
    ctx.globalAlpha = Math.min(0.24, 0.04 + p * 0.14 + vocal * 0.05);
    ctx.drawImage(trackArtwork, (W - artBoxSize) / 2, (H - artBoxSize) / 2, artBoxSize, artBoxSize);
    ctx.restore();
  }
}


