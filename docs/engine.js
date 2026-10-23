// ============================================================================
// NO MORĒ ILLUSĪONS — 1:1 UNIFIED AUDIO-REACTIVE ENGINE (iOS + ANDROID)
// ============================================================================

class MultiBandState {
  constructor() {
    this.rms = 0.35;
    this.bass = 0.32;
    this.vocal = 0.35;
    this.high = 0.28;
    this.bassDropOut = false;
    this.swellDelta = 0.1;
    this.isClimax = false;
  }
}

function quantizeToBeat(sec, beatSec) {
  const b = Math.round(sec / beatSec);
  return Number((b * beatSec).toFixed(3));
}

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

function sample4Hz(arr, sec) {
  if (!arr || !arr.length) return 0.45;
  const exact = Math.max(0, sec) / 0.25;
  const i0 = Math.min(arr.length - 1, Math.floor(exact));
  const i1 = Math.min(arr.length - 1, i0 + 1);
  const f = exact - i0;
  return Math.max(0, Math.min(1, arr[i0] * (1 - f) + arr[i1] * f));
}

function getMultiBandStateAt(trackIndexOrId, sec) {
  const d = window.MULTI_BAND_DATA;
  if (!d) return new MultiBandState();
  const isT1 = trackIndexOrId === 1 || trackIndexOrId === "track_01" || trackIndexOrId === 0;
  const rmsArr = isT1 ? d.confideRms4Hz : d.numberOneRms4Hz;
  const bassArr = isT1 ? d.confideBass4Hz : d.numberOneBass4Hz;
  const vocalArr = isT1 ? d.confideVocal4Hz : d.numberOneVocal4Hz;
  const highArr = isT1 ? d.confideHigh4Hz : d.numberOneHigh4Hz;

  const rms = sample4Hz(rmsArr, sec);
  const bass = sample4Hz(bassArr, sec);
  const vocal = sample4Hz(vocalArr, sec);
  const high = sample4Hz(highArr, sec);
  const prevRms = sample4Hz(rmsArr, Math.max(0, sec - 0.75));
  const prevVocal = sample4Hz(vocalArr, Math.max(0, sec - 0.75));
  const prevHigh = sample4Hz(highArr, Math.max(0, sec - 0.75));
  const swellDelta = Math.max(-0.5, Math.min(0.7, Math.max(rms - prevRms, (vocal - prevVocal) * 0.85, (high - prevHigh) * 0.85)));
  const bassDropOut = sec > 8.0 && bass < 0.14;
  return { rms, bass, vocal, high, bassDropOut, swellDelta, isClimax: rms > 0.82 };
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


function buildConfideOrbChart() {
  const beatSec = 60 / 124;
  const specialMoments = [
    [7.26, "TRACTOR_REACH"], [14.52, "CONSTELLATION_STAR"], [21.29, "MAGNETAR_WAR"], [28.55, "PRISM_SHATTER"],
    [31.94, "TRACTOR_REACH"], [36.29, "ECLIPSE_DARK"], [43.06, "PRISM_SHATTER"], [45.97, "MAGNETAR_WAR"],
    [53.71, "MAGNETAR_WAR"], [60.97, "ECLIPSE_DARK"], [65.81, "BINARY_HEART"], [69.68, "TRACTOR_REACH"],
    [72.58, "MAGNETAR_WAR"], [75.48, "CONSTELLATION_STAR"], [83.23, "CONSTELLATION_STAR"], [90.97, "MAGNETAR_WAR"],
    [97.26, "PRISM_SHATTER"], [102.10, "MAGNETAR_WAR"], [107.90, "BINARY_HEART"], [113.71, "TRACTOR_REACH"],
    [119.52, "PRISM_SHATTER"], [127.26, "ECLIPSE_DARK"], [131.61, "TRACTOR_REACH"], [134.52, "MAGNETAR_WAR"],
    [137.42, "CONSTELLATION_STAR"], [145.16, "PRISM_SHATTER"], [152.90, "MAGNETAR_WAR"], [160.65, "BINARY_HEART"],
    [169.35, "MAGNETAR_WAR"], [176.13, "BINARY_HEART"], [184.84, "CONSTELLATION_STAR"], [192.58, "PRISM_SHATTER"],
    [201.29, "CONSTELLATION_STAR"], [208.06, "ECLIPSE_DARK"]
  ];
  const cues = [];
  const used = new Set();
  let beatIdx = 4, seq = 1;
  while (beatIdx * beatSec <= 214.0) {
    const t = beatIdx * beatSec;
    const inBreakdown = (t >= 62.0 && t <= 68.8) || (t >= 124.0 && t <= 130.8);
    const inRiser = (t >= 69.0 && t <= 75.4) || (t >= 131.0 && t <= 137.3);
    let matchedIdx = -1, bestDist = beatSec * 1.6;
    for (let i = 0; i < specialMoments.length; i++) {
      if (!used.has(i)) {
        const d = Math.abs(specialMoments[i][0] - t);
        if (d <= bestDist) { bestDist = d; matchedIdx = i; }
      }
    }
    let anchor = null;
    if (matchedIdx !== -1) { used.add(matchedIdx); anchor = specialMoments[matchedIdx]; }
    const effect = anchor ? anchor[1] : "NONE";
    let nx, ny;
    if (inRiser) {
      const rp = Math.max(0, Math.min(1, (t < 100 ? (t - 69) : (t - 131)) / 6.5));
      nx = (seq % 2 === 0) ? 0.32 : 0.68;
      ny = Math.max(0.28, Math.min(0.74, 0.72 - rp * 0.40));
    } else if (inBreakdown) {
      nx = 0.50 + 0.16 * Math.sin(seq * 0.8);
      ny = 0.52 + 0.10 * Math.cos(seq * 0.8);
    } else {
      const rp = seq * 0.58;
      nx = Math.max(0.20, Math.min(0.80, 0.50 + 0.28 * Math.sin(rp)));
      ny = Math.max(0.28, Math.min(0.74, 0.50 + 0.21 * Math.cos(rp * 0.70)));
    }
    cues.push({
      sequenceIndex: seq,
      timestampSec: quantizeToBeat(anchor ? anchor[0] : t, beatSec),
      normX: nx,
      normY: ny,
      keyWordEffect: effect,
      isGoldenCue: effect === "CONSTELLATION_STAR"
    });
    seq++;
    beatIdx += inRiser ? 3 : (inBreakdown ? 6 : 4);
  }
  return cues;
}

function buildNumberOneOrbChart() {
  const beatSec = 60 / 88;
  const specialMoments = [
    [6.82, "TRACTOR_REACH"], [10.23, "BINARY_HEART"], [17.73, "TRACTOR_REACH"],
    [24.55, "TRACTOR_REACH"], [31.36, "BINARY_HEART"], [39.55, "MAGNETAR_WAR"],
    [45.00, "PRISM_SHATTER"], [52.50, "ECLIPSE_DARK"], [55.23, "CONSTELLATION_STAR"],
    [58.64, "CONSTELLATION_STAR"], [64.09, "BINARY_HEART"], [70.23, "BINARY_HEART"],
    [75.00, "CONSTELLATION_STAR"], [79.09, "ECLIPSE_DARK"], [84.55, "TRACTOR_REACH"],
    [91.36, "BINARY_HEART"], [94.09, "PRISM_SHATTER"], [97.50, "MAGNETAR_WAR"],
    [104.32, "ECLIPSE_DARK"], [109.77, "CONSTELLATION_STAR"], [113.18, "CONSTELLATION_STAR"],
    [120.68, "PRISM_SHATTER"], [124.09, "BINARY_HEART"], [129.55, "CONSTELLATION_STAR"],
    [135.00, "BINARY_HEART"], [139.09, "PRISM_SHATTER"], [145.91, "MAGNETAR_WAR"],
    [148.64, "CONSTELLATION_STAR"], [152.73, "CONSTELLATION_STAR"], [159.55, "ECLIPSE_DARK"]
  ];
  const cues = [];
  const used = new Set();
  let beatIdx = 3, seq = 1;
  while (beatIdx * beatSec <= 162.0) {
    const t = beatIdx * beatSec;
    const isRap = t >= 91.0 && t <= 98.5;
    const isClimax = t >= 129.0 && t <= 152.5;
    let matchedIdx = -1, bestDist = beatSec * 1.4;
    for (let i = 0; i < specialMoments.length; i++) {
      if (!used.has(i)) {
        const d = Math.abs(specialMoments[i][0] - t);
        if (d <= bestDist) { bestDist = d; matchedIdx = i; }
      }
    }
    let anchor = null;
    if (matchedIdx !== -1) { used.add(matchedIdx); anchor = specialMoments[matchedIdx]; }
    const effect = anchor ? anchor[1] : "NONE";
    const rp = isRap ? seq * 0.65 : seq * 0.52;
    const nx = Math.max(0.20, Math.min(0.80, 0.50 + 0.28 * Math.cos(rp)));
    const ny = Math.max(0.28, Math.min(0.74, 0.50 + 0.21 * Math.sin(rp * 0.72)));
    cues.push({
      sequenceIndex: seq,
      timestampSec: quantizeToBeat(anchor ? anchor[0] : t, beatSec),
      normX: nx,
      normY: ny,
      keyWordEffect: effect,
      isGoldenCue: effect === "CONSTELLATION_STAR"
    });
    seq++;
    beatIdx += (isRap || isClimax) ? 2 : 3;
  }
  return cues;
}


const OFFICIAL_TRACKS = [
  {
    id: "track_01",
    index: 1,
    title: "Confide",
    subtitle: "Official Single • 126 BPM",
    bpm: 124,
    durationSec: 219,
    durationLabel: "3:39",
    unlockCost: 0,
    src: "assets/track_01.mp3",
    audioSrc: "assets/track_01.mp3",
    artKey: "track01Art",
    artSrc: "assets/track_01.png",
    sections: [
      { t: 0, timeSec: 0, title: "INTRO", subtitle: "SYNTH RESONANCE", effectType: "STANDARD" },
      { t: 7.25, timeSec: 7.25, title: "BASS & GUITAR SWELL", subtitle: "SUB-BASS DRIFT", effectType: "WARP_PULSE" },
      { t: 14.5, timeSec: 14.5, title: "DRUM & SYNTH DROP", subtitle: "SUPERNOVA BURST", effectType: "CONSTELLATION_RUSH" },
      { t: 31.2, timeSec: 31.2, title: "VERSE ONE", subtitle: "124 BPM SYNTH GROOVE", effectType: "STANDARD" },
      { t: 45.5, timeSec: 45.5, title: "PRE-CHORUS SURGE", subtitle: "HARMONIC RISER", effectType: "SPEED_PORTAL" },
      { t: 62.0, timeSec: 62.0, title: "VOCAL BREAKDOWN", subtitle: "ZERO-BASS PURITY", effectType: "ECLIPSE_VORTEX" },
      { t: 69.25, timeSec: 69.25, title: "PRE-CHORUS RISER", subtitle: "ENERGY CHARGE", effectType: "SPEED_PORTAL" },
      { t: 75.5, timeSec: 75.5, title: "CHORUS SUPERNOVA", subtitle: "FULL AUDIO OVERDRIVE", effectType: "CONSTELLATION_RUSH" },
      { t: 93.0, timeSec: 93.0, title: "VERSE TWO", subtitle: "KICK & VOCAL LOCK", effectType: "STANDARD" },
      { t: 107.8, timeSec: 107.8, title: "MID-VERSE BASS SURGE", subtitle: "SUB-BASS EXPANSION", effectType: "WARP_PULSE" },
      { t: 124.0, timeSec: 124.0, title: "VOCAL BREAKDOWN II", subtitle: "ATMOSPHERIC VOID", effectType: "ECLIPSE_VORTEX" },
      { t: 131.25, timeSec: 131.25, title: "PRE-CHORUS RISER II", subtitle: "ASCENDING STAIRCASE", effectType: "SPEED_PORTAL" },
      { t: 137.5, timeSec: 137.5, title: "CHORUS II SUPERNOVA", subtitle: "MAXIMUM RESONANCE", effectType: "CONSTELLATION_RUSH" },
      { t: 155.0, timeSec: 155.0, title: "BRIDGE BREATHER", subtitle: "WARM CHORDS", effectType: "STANDARD" },
      { t: 169.25, timeSec: 169.25, title: "VOCAL BATTLE SWELL", subtitle: "DUAL HARMONIES", effectType: "WARP_PULSE" },
      { t: 184.75, timeSec: 184.75, title: "GRAND FINALE CLIMAX", subtitle: "24K GOLD OVERDRIVE", effectType: "SUPERNOVA_BURST" },
      { t: 201.25, timeSec: 201.25, title: "SUB-BASS HEARTBEAT", subtitle: "WARM PULSE", effectType: "STANDARD" },
      { t: 208.0, timeSec: 208.0, title: "OUTRO ECLIPSE", subtitle: "VELVET DECAY", effectType: "ECLIPSE_VORTEX" }
    ],
    choreography: [
      { timeSec: 0, title: "INTRO", subtitle: "SYNTH RESONANCE", effectType: "STANDARD" },
      { timeSec: 14.5, title: "CHORUS ONE", subtitle: "SUPERNOVA BURST", effectType: "CONSTELLATION_RUSH" },
      { timeSec: 62.0, title: "VOCAL BREAKDOWN", subtitle: "ZERO-BASS PURITY", effectType: "ECLIPSE_VORTEX" },
      { timeSec: 75.5, title: "CHORUS SUPERNOVA", subtitle: "FULL AUDIO OVERDRIVE", effectType: "CONSTELLATION_RUSH" },
      { timeSec: 124.0, title: "VOCAL BREAKDOWN II", subtitle: "ATMOSPHERIC VOID", effectType: "ECLIPSE_VORTEX" },
      { timeSec: 137.5, title: "CHORUS II SUPERNOVA", subtitle: "MAXIMUM RESONANCE", effectType: "CONSTELLATION_RUSH" },
      { timeSec: 184.75, title: "GRAND FINALE CLIMAX", subtitle: "24K GOLD OVERDRIVE", effectType: "SUPERNOVA_BURST" },
      { timeSec: 208.0, title: "OUTRO ECLIPSE", subtitle: "VELVET DECAY", effectType: "ECLIPSE_VORTEX" }
    ],
    orbChart: buildConfideOrbChart()
  },
  {
    id: "track_02",
    index: 2,
    title: "Number One",
    subtitle: "Hypnotic Hip-Hop • 88 BPM",
    bpm: 88,
    durationSec: 166,
    durationLabel: "2:46",
    unlockCost: 500,
    src: "assets/track_02.mp3",
    audioSrc: "assets/track_02.mp3",
    artKey: "track02Art",
    artSrc: "assets/track_02.png",
    sections: [
      { t: 0, timeSec: 0, title: "INTRO", subtitle: "WARM CHORD BED", effectType: "STANDARD" },
      { t: 6.5, timeSec: 6.5, title: "SUB-BASS WARMTH", subtitle: "808 PULSE", effectType: "WARP_PULSE" },
      { t: 9.75, timeSec: 9.75, title: "PERCUSSION SHIMMER", subtitle: "HI-HAT ROLL", effectType: "STANDARD" },
      { t: 22.0, timeSec: 22.0, title: "VERSE ONE", subtitle: "VOCAL FLOW", effectType: "STANDARD" },
      { t: 42.25, timeSec: 42.25, title: "PRE-CHORUS SWELL", subtitle: "RISER WAVE", effectType: "SPEED_PORTAL" },
      { t: 55.25, timeSec: 55.25, title: "CHORUS DROP", subtitle: "SUPERNOVA CADENCE", effectType: "CONSTELLATION_RUSH" },
      { t: 76.0, timeSec: 76.0, title: "VERSE TWO", subtitle: "RHYTHMIC POCKET", effectType: "STANDARD" },
      { t: 91.25, timeSec: 91.25, title: "RAP CADENCE", subtitle: "DOUBLE-TIME RUSH", effectType: "OVERDRIVE_DROP" },
      { t: 101.5, timeSec: 101.5, title: "PRE-CHORUS II", subtitle: "CRESCENDO SWEEP", effectType: "SPEED_PORTAL" },
      { t: 109.75, timeSec: 109.75, title: "CHORUS II DROP", subtitle: "MAXIMUM IMPACT", effectType: "CONSTELLATION_RUSH" },
      { t: 129.5, timeSec: 129.5, title: "GRAND FINALE CLIMAX", subtitle: "ALL INSTRUMENTS PEAK", effectType: "SUPERNOVA_BURST" },
      { t: 152.5, timeSec: 152.5, title: "VELVET TWILIGHT OUTRO", subtitle: "SUBLIME RESOLUTION", effectType: "ECLIPSE_VORTEX" }
    ],
    choreography: [
      { timeSec: 0, title: "INTRO", subtitle: "WARM CHORD BED", effectType: "STANDARD" },
      { timeSec: 55.25, title: "CHORUS DROP", subtitle: "SUPERNOVA CADENCE", effectType: "CONSTELLATION_RUSH" },
      { timeSec: 91.25, title: "RAP CADENCE", subtitle: "DOUBLE-TIME RUSH", effectType: "OVERDRIVE_DROP" },
      { timeSec: 109.75, title: "CHORUS II DROP", subtitle: "MAXIMUM IMPACT", effectType: "CONSTELLATION_RUSH" },
      { timeSec: 129.5, title: "GRAND FINALE CLIMAX", subtitle: "ALL INSTRUMENTS PEAK", effectType: "SUPERNOVA_BURST" },
      { timeSec: 152.5, title: "VELVET TWILIGHT OUTRO", subtitle: "SUBLIME RESOLUTION", effectType: "ECLIPSE_VORTEX" }
    ],
    orbChart: buildNumberOneOrbChart()
  }
];


const TRACK_REMIX_MODES = [
  { id: "ORIGINAL", displayName: "NORMAL", title: "Normal Speed", shortTag: "1.0x", subtitle: "Regular song speed • Balanced challenge", speed: 1.0, pitch: 1.0, bonusText: "1.0x Score", color: "#4ADE80" },
  { id: "OVERCLOCK", displayName: "FAST", title: "Fast Remix (1.2x)", shortTag: "1.2x", subtitle: "Faster song & pitch • Intense speed • 2x Notes", speed: 1.20, pitch: 1.12, bonusText: "2.0x ♪ Notes", color: "#F43F5E" },
  { id: "SLOWED", displayName: "CHILL", title: "Slowed & Reverb (0.85x)", shortTag: "0.85x", subtitle: "Deep slowed vocals • Smooth flow • 1.5x Notes", speed: 0.85, pitch: 0.86, bonusText: "1.5x ♪ Notes", color: "#38BDF8" },
  { id: "ENDLESS", displayName: "ENDLESS", title: "Endless Survival", shortTag: "∞ LOOP", subtitle: "Non-stop looping song • Rising speed • 2.5x Notes", speed: 1.0, pitch: 1.0, isEndless: true, bonusText: "2.5x ♪ Notes", color: "#FACC15" }
];

const LOBBY_TRACKS = [
  { id: "VELVET", title: "Velvet Synth", bpm: 92, src: "assets/lobby_velvet.mp3" },
  { id: "MIDNIGHT", title: "Midnight Pulse", bpm: 105, src: "assets/lobby_midnight.mp3" },
  { id: "CRYSTAL", title: "Crystal Mirage", bpm: 118, src: "assets/lobby_crystal.mp3" }
];


// 5. GAME RENDERER (1:1 Port of GameRenderer.kt & TrackRevealOverlay.kt)
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

function drawWormholes(ctx, wormholes, time) {
  for (let i = 0; i < wormholes.length; i++) {
    const wh = wormholes[i];
    const fadeIn = Math.min(1, wh.age / 0.8);
    const fadeOut = Math.min(1, (wh.maxLifetime - wh.age) / 0.8);
    const alpha = Math.max(0, Math.min(1, fadeIn * fadeOut));
    if (alpha <= 0.02) continue;

    // Flowing curved stardust stream between Portals A and B (replaces straight dashed line)
    const wdx = wh.bx - wh.ax;
    const wdy = wh.by - wh.ay;
    const wDist = Math.max(1, Math.hypot(wdx, wdy));
    const wPerpX = -wdy / wDist;
    const wPerpY = wdx / wDist;
    const ctrlX = (wh.ax + wh.bx) * 0.5 + wPerpX * Math.sin(time * 2.2) * 38;
    const ctrlY = (wh.ay + wh.by) * 0.5 + wPerpY * Math.sin(time * 2.2) * 38;

    for (let m = 0; m < 5; m++) {
      const mt = ((time * 0.65 + m * 0.2) % 1);
      const inv = 1 - mt;
      const mx = inv * inv * wh.ax + 2 * inv * mt * ctrlX + mt * mt * wh.bx;
      const my = inv * inv * wh.ay + 2 * inv * mt * ctrlY + mt * mt * wh.by;
      ctx.beginPath();
      ctx.arc(mx, my, 2.6 * Math.sin(mt * Math.PI) + 1.2, 0, Math.PI * 2);
      ctx.fillStyle = rgba([192, 132, 252], 0.48 * Math.sin(mt * Math.PI) * alpha);
      ctx.fill();
    }

    const portals = [
      { x: wh.ax, y: wh.ay, col: [56, 189, 248], rim: [168, 85, 247] },
      { x: wh.bx, y: wh.by, col: [244, 114, 182], rim: [250, 204, 21] }
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
    }
  }
}

function drawOrbitalPhenomenonAndSatellites(ctx, orbitalPhenomenon, satellites, time, beatPulse, vocalEnvelope) {
  if (orbitalPhenomenon) {
    const op = orbitalPhenomenon;
    const isBlackHole = op.type === "BLACK_HOLE";
    const outerRadius = op.radius * (3.1 + beatPulse * 0.35);

    if (isBlackHole) {
      const grad = ctx.createRadialGradient(op.x, op.y, 4, op.x, op.y, outerRadius);
      grad.addColorStop(0, "rgba(0,0,0,0.98)");
      grad.addColorStop(0.32, "rgba(18,6,46,0.92)");
      grad.addColorStop(0.55, "rgba(168,85,247,0.48)");
      grad.addColorStop(0.78, "rgba(56,189,248,0.18)");
      grad.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(op.x, op.y, outerRadius, 0, Math.PI * 2);
      ctx.fillStyle = grad;
      ctx.fill();

      for (let ring = 0; ring < 4; ring++) {
        const r = op.radius * (1.15 + ring * 0.42);
        const startRad = ((time * (150 - ring * 30) * (ring % 2 === 0 ? 1 : -1)) % 360) * Math.PI / 180;
        const col = ring % 2 === 0 ? [192, 132, 252] : [56, 189, 248];
        ctx.beginPath();
        ctx.arc(op.x, op.y, r, startRad, startRad + (135 * Math.PI / 180));
        ctx.strokeStyle = rgba(col, 0.82 - ring * 0.14);
        ctx.lineWidth = 5 - ring * 0.9;
        ctx.lineCap = "round";
        ctx.stroke();
      }
      ctx.beginPath();
      ctx.arc(op.x, op.y, op.radius * 0.72, 0, Math.PI * 2);
      ctx.fillStyle = "#030108";
      ctx.fill();
      ctx.beginPath();
      ctx.arc(op.x, op.y, op.radius * 0.76, 0, Math.PI * 2);
      ctx.strokeStyle = rgba([233, 213, 255], 0.9);
      ctx.lineWidth = 2.5;
      ctx.stroke();
    } else {
      const grad = ctx.createRadialGradient(op.x, op.y, 4, op.x, op.y, outerRadius);
      grad.addColorStop(0, "rgba(255,255,255,0.98)");
      grad.addColorStop(0.32, "rgba(254,240,138,0.85)");
      grad.addColorStop(0.58, "rgba(245,158,11,0.45)");
      grad.addColorStop(0.82, "rgba(244,63,94,0.18)");
      grad.addColorStop(1, "rgba(0,0,0,0)");
      ctx.beginPath();
      ctx.arc(op.x, op.y, outerRadius, 0, Math.PI * 2);
      ctx.fillStyle = grad;
      ctx.fill();

      const rayCount = 8;
      for (let rIdx = 0; rIdx < rayCount; rIdx++) {
        const angle = (rIdx * (360 / rayCount) + time * 42) * Math.PI / 180;
        const rayLen = op.radius * (1.8 + 0.45 * Math.sin(time * 5 + rIdx) + vocalEnvelope * 0.5);
        ctx.beginPath();
        ctx.moveTo(op.x, op.y);
        ctx.lineTo(op.x + Math.cos(angle) * rayLen, op.y + Math.sin(angle) * rayLen);
        ctx.strokeStyle = rgba([253, 224, 71], 0.55);
        ctx.lineWidth = 3.5;
        ctx.lineCap = "round";
        ctx.stroke();
      }
      ctx.beginPath();
      ctx.arc(op.x, op.y, op.radius * 0.68, 0, Math.PI * 2);
      ctx.fillStyle = "#FFFBEB";
      ctx.fill();
    }
  }

  for (let i = 0; i < satellites.length; i++) {
    const sat = satellites[i];
    const satCol = sat.isDebris ? [192, 132, 252] : [251, 146, 60];
    if (orbitalPhenomenon) {
      ctx.beginPath();
      ctx.arc(orbitalPhenomenon.x, orbitalPhenomenon.y, sat.orbitRadius, 0, Math.PI * 2);
      ctx.strokeStyle = rgba(satCol, 0.14);
      ctx.lineWidth = 1.2;
      ctx.stroke();
    }
    const g = ctx.createRadialGradient(sat.x, sat.y, 2, sat.x, sat.y, sat.radius * 2.1);
    g.addColorStop(0, "rgba(255,255,255,0.95)");
    g.addColorStop(0.45, rgba(satCol, 0.72));
    g.addColorStop(1, "rgba(0,0,0,0)");
    ctx.beginPath();
    ctx.arc(sat.x, sat.y, sat.radius * 2.1, 0, Math.PI * 2);
    ctx.fillStyle = g;
    ctx.fill();

    ctx.beginPath();
    const pts = 6;
    for (let v = 0; v < pts; v++) {
      const a = (v * 60 + time * 180) * Math.PI / 180;
      const rad = v % 2 === 0 ? sat.radius : sat.radius * 0.68;
      const vx = sat.x + Math.cos(a) * rad;
      const vy = sat.y + Math.sin(a) * rad;
      if (v === 0) ctx.moveTo(vx, vy);
      else ctx.lineTo(vx, vy);
    }
    ctx.closePath();
    ctx.fillStyle = sat.isDebris ? "#3B0764" : "#7C2D12";
    ctx.fill();
    ctx.strokeStyle = rgba(satCol, 0.95);
    ctx.lineWidth = 2.2;
    ctx.stroke();
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

    // 2. Soft curved constellation motes from Primary Orb to Next Upcoming Beat Orb
    if (sorted.length >= 2) {
      const nextOrb = sorted[1];
      for (let m = 1; m <= 3; m++) {
        const mt = m / 4;
        const mx = primaryTarget.x + (nextOrb.x - primaryTarget.x) * mt;
        const my = primaryTarget.y + (nextOrb.y - primaryTarget.y) * mt - Math.sin(mt * Math.PI) * 16;
        ctx.beginPath();
        ctx.arc(mx, my, 2.2, 0, Math.PI * 2);
        ctx.fillStyle = "rgba(56, 189, 248, 0.36)";
        ctx.fill();
      }
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

      // Outer rotating crescent aura arcs (no boxy square frame)
      const rot = time * 2.1;
      for (let a = 0; a < 2; a++) {
        const startA = rot + a * Math.PI;
        ctx.beginPath();
        ctx.arc(pu.x, pu.y, coreRad * 0.86, startA, startA + 1.9);
        ctx.strokeStyle = rgba(effectCol, 0.95 * puAlpha);
        ctx.lineWidth = 2.4;
        ctx.lineCap = "round";
        ctx.stroke();
      }

      ctx.beginPath();
      ctx.arc(pu.x, pu.y, coreRad * 0.36, 0, Math.PI * 2);
      ctx.fillStyle = "#FFFFFF";
      ctx.fill();
    }
  }
}

function drawEnemyTelegraphs(ctx, telegraphs, time, beatPulse) {
  if (!telegraphs || telegraphs.length === 0) return;

  for (let i = 0; i < telegraphs.length; i++) {
    const tg = telegraphs[i];
    const progress = Math.max(0, Math.min(1, 1 - tg.timer / tg.maxTimer));
    const urgencyPulse = 0.65 + 0.35 * Math.sin(time * 24);
    const warnCol = [255, 30, 66]; // High-contrast Crimson

    // 1. 3 Expanding Directional Sonar Warning Ripples (Replaces harsh straight red dashed sightlines!)
    const arcSpread = 0.52; // ~30 deg half-cone
    for (let w = 0; w < 3; w++) {
      const wavePhase = ((time * 2.6 + w * 0.33) % 1);
      const waveRad = 22 + wavePhase * (95 + beatPulse * 24);
      const waveAlpha = (1 - wavePhase) * (0.45 + 0.35 * urgencyPulse);
      ctx.beginPath();
      ctx.arc(tg.edgeX, tg.edgeY, waveRad, tg.targetAngle - arcSpread, tg.targetAngle + arcSpread);
      ctx.strokeStyle = rgba(warnCol, waveAlpha);
      ctx.lineWidth = 3.2 * (1 - wavePhase * 0.5);
      ctx.lineCap = "round";
      ctx.stroke();
    }

    // 2. Soft Crimson Edge Glow Bloom
    const bloomGrad = ctx.createRadialGradient(tg.edgeX, tg.edgeY, 2, tg.edgeX, tg.edgeY, 38);
    bloomGrad.addColorStop(0, rgba(warnCol, 0.55 * urgencyPulse));
    bloomGrad.addColorStop(1, "rgba(0,0,0,0)");
    ctx.beginPath();
    ctx.arc(tg.edgeX, tg.edgeY, 38, 0, Math.PI * 2);
    ctx.fillStyle = bloomGrad;
    ctx.fill();

    // 3. High-Contrast Crimson Warning Diamond (◆) on screen perimeter
    const dSize = 14 + urgencyPulse * 4;
    ctx.save();
    ctx.translate(tg.edgeX, tg.edgeY);
    ctx.rotate(Math.PI / 4);
    ctx.fillStyle = rgba(warnCol, 0.95);
    ctx.fillRect(-dSize / 2, -dSize / 2, dSize, dSize);
    ctx.strokeStyle = "#FFFFFF";
    ctx.lineWidth = 1.8;
    ctx.strokeRect(-dSize / 2, -dSize / 2, dSize, dSize);
    ctx.restore();

    // 4. Countdown ring contracting onto the diamond
    ctx.beginPath();
    ctx.arc(tg.edgeX, tg.edgeY, (1 - progress) * 26 + 11, 0, Math.PI * 2);
    ctx.strokeStyle = rgba([255, 183, 3], 0.88);
    ctx.lineWidth = 2.2;
    ctx.stroke();
  }
}

function drawEnemySprite(ctx, e, enemyBitmaps, time, beatPulse) {
  let primaryCol, secondaryCol, bmp;
  switch (e.type) {
    case "SEEKER":
      primaryCol = [244, 63, 94];
      secondaryCol = [251, 146, 60];
      bmp = enemyBitmaps.SEEKER;
      break;
    case "PULSAR":
      primaryCol = [250, 204, 21];
      secondaryCol = [56, 189, 248];
      bmp = enemyBitmaps.PULSAR;
      break;
    default:
      primaryCol = [168, 85, 247];
      secondaryCol = [236, 72, 153];
      bmp = enemyBitmaps.WANDERER;
  }

  const speed = Math.max(1, Math.hypot(e.vx, e.vy));
  const dirX = e.vx / speed;
  const dirY = e.vy / speed;
  const headingDeg = Math.atan2(dirY, dirX) * 180 / Math.PI;
  const perpX = -dirY;
  const perpY = dirX;

  // 1. Articulated Energy Spine / Tail (6 segments)
  const segments = 6;
  for (let s = segments; s >= 1; s--) {
    const frac = s / segments;
    const waveOffset = Math.sin(time * 9 - s * 0.75 + e.phase) * (e.radius * 0.28 * frac);
    const segDist = s * (e.radius * 0.36);
    const sx = e.x - dirX * segDist + perpX * waveOffset;
    const sy = e.y - dirY * segDist + perpY * waveOffset;
    const segRad = e.radius * (0.48 * (1 - frac * 0.65));

    ctx.beginPath();
    ctx.arc(sx, sy, segRad * 1.45, 0, Math.PI * 2);
    ctx.fillStyle = rgba(primaryCol, (1 - frac) * 0.38);
    ctx.fill();

    ctx.beginPath();
    ctx.arc(sx, sy, segRad * 0.65, 0, Math.PI * 2);
    ctx.fillStyle = rgba(secondaryCol, (1 - frac) * 0.72);
    ctx.fill();
  }

  // 2. Outer Mandala Energy Halo
  const haloGrad = ctx.createRadialGradient(e.x, e.y, 2, e.x, e.y, e.radius * 2.15);
  haloGrad.addColorStop(0, rgba(primaryCol, 0.55));
  haloGrad.addColorStop(0.55, rgba(secondaryCol, 0.22));
  haloGrad.addColorStop(1, "rgba(0,0,0,0)");
  ctx.beginPath();
  ctx.arc(e.x, e.y, e.radius * 2.15, 0, Math.PI * 2);
  ctx.fillStyle = haloGrad;
  ctx.fill();

  // 3. Boss-Grade Procedural Geometry by Archetype
  if (e.type === "SEEKER") {
    const wingSweep = 16 + 10 * Math.sin(time * 12 + e.phase) - beatPulse * 12;
    for (const side of [-1, 1]) {
      const wingAngle = (headingDeg + 180 + side * (58 + wingSweep)) * Math.PI / 180;
      const tipX = e.x + Math.cos(wingAngle) * e.radius * 1.75;
      const tipY = e.y + Math.sin(wingAngle) * e.radius * 1.75;
      const midAngle = (headingDeg + 180 + side * 28) * Math.PI / 180;
      const midX = e.x + Math.cos(midAngle) * e.radius * 1.05;
      const midY = e.y + Math.sin(midAngle) * e.radius * 1.05;

      ctx.beginPath();
      ctx.moveTo(e.x + dirX * e.radius * 0.55, e.y + dirY * e.radius * 0.55);
      ctx.lineTo(tipX, tipY);
      ctx.lineTo(midX, midY);
      ctx.closePath();
      ctx.fillStyle = rgba(primaryCol, 0.78);
      ctx.fill();
      ctx.strokeStyle = rgba([255, 255, 255], 0.85);
      ctx.lineWidth = 2;
      ctx.stroke();
    }
  } else if (e.type === "PULSAR") {
    const bladeCount = 10;
    const spinDeg = (time * 210 + e.phase * 50) % 360;
    ctx.beginPath();
    for (let b = 0; b < bladeCount * 2; b++) {
      const aRad = (spinDeg + b * (180 / bladeCount)) * Math.PI / 180;
      const r = b % 2 === 0 ? e.radius * (1.42 + beatPulse * 0.22) : e.radius * 0.82;
      const bx = e.x + Math.cos(aRad) * r;
      const by = e.y + Math.sin(aRad) * r;
      if (b === 0) ctx.moveTo(bx, by);
      else ctx.lineTo(bx, by);
    }
    ctx.closePath();
    ctx.fillStyle = rgba(primaryCol, 0.35);
    ctx.fill();
    ctx.strokeStyle = rgba([254, 240, 138], 0.9);
    ctx.lineWidth = 2.4;
    ctx.stroke();
  } else {
    for (const side of [-1, 1]) {
      const finWave = Math.sin(time * 7 + e.phase + side) * 18;
      const finAngle = (headingDeg + 180 + side * (72 + finWave)) * Math.PI / 180;
      const finX = e.x + Math.cos(finAngle) * e.radius * 1.55;
      const finY = e.y + Math.sin(finAngle) * e.radius * 1.55;
      ctx.beginPath();
      ctx.moveTo(e.x, e.y);
      ctx.lineTo(finX, finY);
      ctx.strokeStyle = rgba(secondaryCol, 0.75);
      ctx.lineWidth = 4.5;
      ctx.lineCap = "round";
      ctx.stroke();
    }
    const ringSpin = (-time * 120 + e.phase * 40) % 360;
    ctx.beginPath();
    ctx.arc(e.x, e.y, e.radius * 1.22, ringSpin * Math.PI / 180, (ringSpin + 140) * Math.PI / 180);
    ctx.strokeStyle = rgba([233, 213, 255], 0.8);
    ctx.lineWidth = 2.6;
    ctx.lineCap = "round";
    ctx.stroke();

    ctx.beginPath();
    ctx.arc(e.x, e.y, e.radius * 1.22, (ringSpin + 180) * Math.PI / 180, (ringSpin + 320) * Math.PI / 180);
    ctx.stroke();
  }

  // 4. Actual Android Mandala Core Sprite with Screen Blending
  const spriteDrawSize = Math.max(16, Math.round(e.radius * 2.55));
  const rotRad = (e.type === "SEEKER"
    ? headingDeg + 90
    : (time * (e.type === "PULSAR" ? 135 : 58) + e.phase * 40) % 360) * Math.PI / 180;

  if (bmp && bmp.complete && bmp.naturalWidth > 0) {
    ctx.save();
    ctx.translate(e.x, e.y);
    ctx.rotate(rotRad);
    ctx.globalCompositeOperation = "screen";
    ctx.drawImage(bmp, -spriteDrawSize / 2, -spriteDrawSize / 2, spriteDrawSize, spriteDrawSize);
    ctx.restore();
  }

  // 5. Glowing Sentient Core Eye locked on direction of travel
  const eyeX = e.x + dirX * (e.radius * 0.24);
  const eyeY = e.y + dirY * (e.radius * 0.24);
  ctx.beginPath();
  ctx.arc(eyeX, eyeY, e.radius * 0.28, 0, Math.PI * 2);
  ctx.fillStyle = "#FFFFFF";
  ctx.fill();
  ctx.beginPath();
  ctx.arc(eyeX + dirX * 2, eyeY + dirY * 2, e.radius * 0.14, 0, Math.PI * 2);
  ctx.fillStyle = rgba(primaryCol, 1);
  ctx.fill();
}

function drawPlayerSprite(
  ctx,
  playerX,
  playerY,
  playerRadius,
  playerBitmap,
  selectedSkinId,
  shieldTimer,
  overdriveTimer,
  magnetTimer,
  isFocusHolding,
  focusCharge,
  beatPulse,
  time
) {
  let skinHaloColor;
  switch (selectedSkinId) {
    case "skin_gold": skinHaloColor = [250, 204, 21]; break;
    case "skin_prism": skinHaloColor = [236, 72, 153]; break;
    default: skinHaloColor = [56, 189, 248];
  }

  if (shieldTimer > 0) {
    ctx.beginPath();
    ctx.arc(playerX, playerY, playerRadius * 1.9 + 5 * Math.sin(time * 8), 0, Math.PI * 2);
    ctx.strokeStyle = rgba([56, 189, 248], 0.78);
    ctx.lineWidth = 3.5;
    ctx.stroke();
  }

  if (overdriveTimer > 0) {
    ctx.beginPath();
    ctx.arc(playerX, playerY, playerRadius * 2.2 + 8 * Math.sin(time * 14), 0, Math.PI * 2);
    ctx.strokeStyle = rgba([244, 63, 94], 0.85);
    ctx.lineWidth = 5;
    ctx.stroke();
  }

  if (magnetTimer > 0) {
    ctx.beginPath();
    ctx.arc(playerX, playerY, playerRadius * 2.6 + 6 * Math.cos(time * 10), 0, Math.PI * 2);
    ctx.strokeStyle = rgba([52, 211, 153], 0.52);
    ctx.lineWidth = 2.5;
    ctx.stroke();
  }

  if (isFocusHolding && focusCharge > 0.02) {
    ctx.beginPath();
    ctx.arc(playerX, playerY, playerRadius * 2.35, -Math.PI / 2, -Math.PI / 2 + focusCharge * Math.PI * 2);
    ctx.strokeStyle = rgba([168, 85, 247], 0.9);
    ctx.lineWidth = 4;
    ctx.lineCap = "round";
    ctx.stroke();
  }

  const pScale = 1 + beatPulse * 0.14;
  const haloGrad = ctx.createRadialGradient(playerX, playerY, 2, playerX, playerY, playerRadius * 2.5 * pScale);
  haloGrad.addColorStop(0, rgba(skinHaloColor, 0.58));
  haloGrad.addColorStop(1, "rgba(0,0,0,0)");
  ctx.beginPath();
  ctx.arc(playerX, playerY, playerRadius * 2.5 * pScale, 0, Math.PI * 2);
  ctx.fillStyle = haloGrad;
  ctx.fill();

  const heroDrawSize = Math.max(24, Math.round(playerRadius * 3.1 * pScale));
  const heroRotRad = ((time * 42) % 360) * Math.PI / 180;
  if (playerBitmap && playerBitmap.complete && playerBitmap.naturalWidth > 0) {
    ctx.save();
    ctx.translate(playerX, playerY);
    ctx.rotate(heroRotRad);
    ctx.globalCompositeOperation = "screen";
    ctx.drawImage(playerBitmap, -heroDrawSize / 2, -heroDrawSize / 2, heroDrawSize, heroDrawSize);
    ctx.restore();
  }
}

function drawFloatingTexts(ctx, floatingTexts) {
  ctx.save();
  ctx.textAlign = "center";
  ctx.font = "900 15px -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";
  for (let i = 0; i < floatingTexts.length; i++) {
    const ft = floatingTexts[i];
    if (ft.alpha > 0.02) {
      ctx.fillStyle = rgba([0, 0, 0], ft.alpha * 0.75);
      ctx.fillText(ft.text, ft.x + 2, ft.y + 2);
      ctx.fillStyle = rgba(ft.color, ft.alpha);
      ctx.fillText(ft.text, ft.x, ft.y);
    }
  }
  ctx.restore();
}

// ============================================================================
// 6. TRACK REVEAL OVERLAY CANVAS (1:1 Port of TrackRevealOverlay.kt)
// ============================================================================
function drawTrackRevealCanvas(ctx, logoImg, progress, beatPulse, isConstellationActive) {
  const w = ctx.canvas.width;
  const h = ctx.canvas.height;
  ctx.clearRect(0, 0, w, h);

  const clampedProgress = Math.max(0, Math.min(1, progress));
  const totalTiles = 16;
  const unlockedTiles = Math.round(clampedProgress * totalTiles);

  // 1. Dimmed locked silhouette underneath
  if (logoImg && logoImg.complete && logoImg.naturalWidth > 0) {
    ctx.save();
    ctx.globalAlpha = 0.22;
    ctx.drawImage(logoImg, 0, 0, w, h);
    ctx.restore();
  }

  const gridCols = 4;
  const gridRows = 4;
  const cellW = w / gridCols;
  const cellH = h / gridRows;
  const revealOrder = [5, 6, 9, 10, 1, 2, 4, 7, 8, 11, 13, 14, 0, 3, 12, 15];

  // 2. Illuminated unlocked tiles + locked glass tiles
  for (let idx = 0; idx < totalTiles; idx++) {
    const tileIndex = revealOrder[idx];
    const r = Math.floor(tileIndex / gridCols);
    const c = tileIndex % gridCols;
    const left = c * cellW;
    const top = r * cellH;

    if (idx < unlockedTiles) {
      if (logoImg && logoImg.complete && logoImg.naturalWidth > 0) {
        ctx.save();
        ctx.beginPath();
        ctx.rect(left, top, cellW + 0.5, cellH + 0.5);
        ctx.clip();
        ctx.globalAlpha = 0.96;
        ctx.drawImage(logoImg, 0, 0, w, h);
        ctx.restore();
      }
      if (idx === unlockedTiles - 1 && clampedProgress < 1) {
        ctx.strokeStyle = rgba([250, 204, 21], 0.55 + beatPulse * 0.35);
        ctx.lineWidth = 1.5;
        ctx.strokeRect(left, top, cellW, cellH);
      }
    } else {
      ctx.fillStyle = "rgba(8, 6, 18, 0.72)";
      ctx.fillRect(left, top, cellW, cellH);
      ctx.strokeStyle = "rgba(56, 189, 248, 0.16)";
      ctx.lineWidth = 0.8;
      ctx.strokeRect(left, top, cellW, cellH);
    }
  }

  // 3. Outer reactive frame border
  const borderCol = isConstellationActive || clampedProgress >= 0.99 ? [250, 204, 21] : [56, 189, 248];
  ctx.strokeStyle = rgba(borderCol, 0.65 + beatPulse * 0.3);
  ctx.lineWidth = 2;
  roundRectPath(ctx, 1, 1, w - 2, h - 2, 12);
  ctx.stroke();
}


// ============================================================================
// 7. MENU TURNTABLE & CUSTOMIZER PREVIEW RENDERERS
// ============================================================================
function drawMenuTurntable(ctx, logoImg, time, isGoldMode) {
  const w = ctx.canvas.width;
  const h = ctx.canvas.height;
  ctx.clearRect(0, 0, w, h);

  const cx = w / 2;
  const cy = h / 2;
  const outerRadius = Math.min(cx, cy) * 0.90;
  const vinylRadius = outerRadius * 0.80;
  const pulse = 0.5 + 0.5 * Math.sin(time * 2.5 * Math.PI * 2);

  // 1. 36-Bar Radial Equalizer Spectrum Crown
  const barCount = 36;
  for (let i = 0; i < barCount; i++) {
    const angleRad = (i * (360 / barCount) + time * 18) * Math.PI / 180;
    const wave = 0.25 + 0.75 * Math.abs(Math.sin(time * 5.2 + i * 0.45));
    const barLen = outerRadius * (0.06 + 0.14 * wave * (0.65 + pulse * 0.35));
    const startR = vinylRadius * 1.04;
    const endR = startR + barLen;

    const barCol = isGoldMode
      ? (i % 2 === 0 ? [250, 204, 21] : [253, 224, 71])
      : i % 3 === 0 ? [56, 189, 248] : i % 3 === 1 ? [168, 85, 247] : [250, 204, 21];

    ctx.beginPath();
    ctx.moveTo(cx + Math.cos(angleRad) * startR, cy + Math.sin(angleRad) * startR);
    ctx.lineTo(cx + Math.cos(angleRad) * endR, cy + Math.sin(angleRad) * endR);
    ctx.strokeStyle = rgba(barCol, 0.75);
    ctx.lineWidth = 3.2;
    ctx.lineCap = "round";
    ctx.stroke();
  }

  // 2. Vinyl Disc Base & Concentric Micro-Grooves
  ctx.beginPath();
  ctx.arc(cx, cy, vinylRadius, 0, Math.PI * 2);
  ctx.fillStyle = "#080612";
  ctx.fill();

  for (let g = 1; g <= 4; g++) {
    const gr = vinylRadius * (0.64 + g * 0.08);
    ctx.beginPath();
    ctx.arc(cx, cy, gr, 0, Math.PI * 2);
    ctx.strokeStyle = "rgba(255,255,255,0.09)";
    ctx.lineWidth = 1.1;
    ctx.stroke();
  }

  // Spinning specular sheen arc on vinyl
  const spinDeg = (time * 50) % 360;
  ctx.beginPath();
  ctx.arc(cx, cy, vinylRadius * 0.86, spinDeg * Math.PI / 180, (spinDeg + 50) * Math.PI / 180);
  ctx.strokeStyle = rgba([56, 189, 248], 0.22);
  ctx.lineWidth = 5;
  ctx.lineCap = "round";
  ctx.stroke();

  ctx.beginPath();
  ctx.arc(cx, cy, vinylRadius * 0.86, (spinDeg + 180) * Math.PI / 180, (spinDeg + 230) * Math.PI / 180);
  ctx.strokeStyle = rgba([168, 85, 247], 0.22);
  ctx.lineWidth = 5;
  ctx.lineCap = "round";
  ctx.stroke();

  // Outer metallic rim
  ctx.beginPath();
  ctx.arc(cx, cy, vinylRadius, 0, Math.PI * 2);
  ctx.strokeStyle = isGoldMode ? rgba([250, 204, 21], 0.88) : rgba([56, 189, 248], 0.65);
  ctx.lineWidth = 2.5;
  ctx.stroke();

  // 3. Spinning Center Album Art Label (nmi_logo.png)
  const labelSize = vinylRadius * 1.16;
  if (logoImg && logoImg.complete && logoImg.naturalWidth > 0) {
    ctx.save();
    ctx.beginPath();
    ctx.arc(cx, cy, labelSize / 2, 0, Math.PI * 2);
    ctx.clip();
    ctx.translate(cx, cy);
    ctx.rotate(spinDeg * Math.PI / 180);
    ctx.drawImage(logoImg, -labelSize / 2, -labelSize / 2, labelSize, labelSize);
    ctx.restore();

    ctx.beginPath();
    ctx.arc(cx, cy, labelSize / 2, 0, Math.PI * 2);
    ctx.strokeStyle = isGoldMode ? "#FACC15" : "rgba(56, 189, 248, 0.72)";
    ctx.lineWidth = 1.6;
    ctx.stroke();
  }

  // Center spindle pin
  ctx.beginPath();
  ctx.arc(cx, cy, 4.5, 0, Math.PI * 2);
  ctx.fillStyle = "#080612";
  ctx.fill();
  ctx.strokeStyle = "#FACC15";
  ctx.lineWidth = 1.5;
  ctx.stroke();

  // 4. Stylized Laser Tone-Arm (Top-Right Pivot)
  const pivotX = cx + vinylRadius * 0.82;
  const pivotY = cy - vinylRadius * 0.72;
  const needleX = cx + vinylRadius * 0.42;
  const needleY = cy + vinylRadius * 0.18;

  ctx.beginPath();
  ctx.arc(pivotX, pivotY, 6.5, 0, Math.PI * 2);
  ctx.fillStyle = "#1E1B2E";
  ctx.fill();
  ctx.strokeStyle = "#FACC15";
  ctx.lineWidth = 2;
  ctx.stroke();

  ctx.beginPath();
  ctx.moveTo(pivotX, pivotY);
  ctx.lineTo(needleX, needleY);
  ctx.strokeStyle = "rgba(226,232,240,0.85)";
  ctx.lineWidth = 2.8;
  ctx.lineCap = "round";
  ctx.stroke();

  ctx.beginPath();
  ctx.arc(needleX, needleY, 5.5 + pulse * 3, 0, Math.PI * 2);
  ctx.fillStyle = "rgba(56,189,248,0.45)";
  ctx.fill();

  ctx.beginPath();
  ctx.arc(needleX, needleY, 2.8, 0, Math.PI * 2);
  ctx.fillStyle = "#FFFFFF";
  ctx.fill();
}

// ============================================================================
// 8. FULL GAME CONTROLLER & PHYSICS ENGINE (1:1 Port of GamePhysics.kt)
// ============================================================================

class NoMoreIllusionsApp {
  getRemixMode() {
    return TRACK_REMIX_MODES.find(m => m.id === this.prefs.selectedRemixModeId) || TRACK_REMIX_MODES[0];
  }

  setRemixMode(modeId) {
    this.unlockAudioOnFirstGesture();
    this.prefs.selectedRemixModeId = modeId;
    localStorage.setItem("nmi_remix_mode", modeId);
    const mode = this.getRemixMode();
    this.synthModeSwitchChime(mode);
    this.updateLobbyPlayback();
    this.syncUI();
  }

  constructor() {
    this.canvas = document.getElementById("game-canvas");
    this.ctx = this.canvas.getContext("2d");
    this.menuTurntableCanvas = document.getElementById("menu-turntable-canvas");
    this.menuTurntableCtx = this.menuTurntableCanvas ? this.menuTurntableCanvas.getContext("2d") : null;
    this.hudRevealCanvas = document.getElementById("hud-reveal-canvas");
    this.hudRevealCtx = this.hudRevealCanvas ? this.hudRevealCanvas.getContext("2d") : null;
    this.previewCanvas = document.getElementById("customizer-preview-canvas");
    this.previewCtx = this.previewCanvas ? this.previewCanvas.getContext("2d") : null;

    // Persistent Preferences (matching DodgePreferences.kt)
    this.prefs = {
      highScore: parseInt(localStorage.getItem("nmi_high_score") || "0", 10),
      points: parseInt(localStorage.getItem("nmi_points") || "350", 10),
      selectedSkin: localStorage.getItem("nmi_skin") || "skin_cyan",
      selectedTrail: localStorage.getItem("nmi_trail") || "trail_cyan",
      selectedTheme: localStorage.getItem("nmi_theme") || "theme_nebula",
      selectedRemixModeId: localStorage.getItem("nmi_remix_mode") || "ORIGINAL",
      unlockedItems: JSON.parse(localStorage.getItem("nmi_unlocked_items") || '["skin_cyan","trail_cyan","theme_nebula"]'),
      unlockedTracks: JSON.parse(localStorage.getItem("nmi_unlocked_tracks") || "[]"),
      trackBestProgress: JSON.parse(localStorage.getItem("nmi_track_progress") || '{"track_01":0,"track_02":0}'),
      trackMasteryMedals: JSON.parse(localStorage.getItem("nmi_track_medals") || '{"track_01":0,"track_02":0}'),
      lobbyThemeIndex: parseInt(localStorage.getItem("nmi_lobby_idx") || "0", 10),
      musicMuted: localStorage.getItem("nmi_music_muted") === "true",
      sfxMuted: localStorage.getItem("nmi_sfx_muted") === "true",
      seenHowToPlay: localStorage.getItem("nmi_seen_htp") === "true"
    };

    // Bitmaps Registry
    this.images = {};
    this.loadAllBitmaps();

    // Audio Registry
    this.lobbyAudio = new Audio();
    this.lobbyAudio.loop = true;
    this.lobbyAudio.volume = 0.55;
    this.trackAudio = new Audio();
    this.trackAudio.preload = "auto";
    this.demoAudio = new Audio();
    this.demoAudio.preload = "auto";
    this.audioUnlocked = false;
    this.sfxPool = {};
    this.initSfxPool();

    // Screen State Machine
    this.gameState = "MENU"; // MENU, PLAYING, PAUSED, GAME_OVER, TRACK_COMPLETED, HOW_TO_PLAY, SETTINGS, DEMO_PLAYER, CUSTOMIZER
    this.selectedStageIndex = 0;
    this.customizerTab = 0; // 0: SKINS, 1: TRAILS, 2: THEMES

    // Demo Player State
    this.demoTrackIndex = 0;
    this.demoLoopMode = false;

    // Live Customizer Preview Drag State
    this.previewTouchPos = null;
    this.previewHeroPos = { x: 0, y: 0 };
    this.previewTrail = [];

    // Game Simulation State
    this.time = 0;
    this.lastFrameTime = performance.now();
    this.score = 0;
    this.level = 1;
    this.survivalSeconds = 0;
    this.combo = 1;
    this.maxComboRun = 1;
    this.comboTimer = 0;
    this.orbsCollectedRun = 0;
    this.screenShake = 0;
    this.beatPulse = 0;
    this.shieldTimer = 0;
    this.slowMoTimer = 0;
    this.overdriveTimer = 0;
    this.magnetTimer = 0;
    this.focusCharge = 1.0;
    this.isFocusHolding = false;
    this.reviveUsedThisRun = false;
    this.newlyUnlockedTrack = null;
    this.earnedMedalTier = 0;

    // Player & Entities
    this.playerX = window.innerWidth / 2;
    this.playerY = window.innerHeight * 0.65;
    this.targetX = this.playerX;
    this.targetY = this.playerY;
    this.playerRadius = 22;
    this.trailPoints = Array.from({ length: 28 }, () => ({ x: 0, y: 0 }));
    this.trailLength = 0;

    this.stars = [];
    this.motes = [];
    this.enemies = [];
    this.powerUps = [];
    this.shockwaves = [];
    this.floatingTexts = [];
    this.wormholes = [];
    this.orbitalPhenomenon = null;
    this.satellites = [];
    this.telegraphs = [];

    // Spawner & Chart Timers
    this.spawnTimer = 0;
    this.powerUpSpawnTimer = 0;
    this.wormholeSpawnTimer = 0;
    this.phenomenonCooldownTimer = 10;
    this.nextBeatCueIndex = 0;
    this.nextChoreoSectionIndex = 0;
    this.choreoBannerText = "";
    this.choreoBannerSubtext = "";
    this.choreoBannerTimer = 0;
    this.eclipseModeTimer = 0;
    this.constellationRushTimer = 0;
    this.activeTrackDurationSec = 179;
    this.trackElapsedSec = 0;
    this.multiBandState = new MultiBandState();

    this.resize();
    this.initStars();
    this.bindEvents();
    this.syncUI();
    requestAnimationFrame((ts) => this.loop(ts));
  }

  loadAllBitmaps() {
    const assetMap = {
      logo: "assets/nmi_logo.png",
      hero_cyan: "assets/mandala_hero_cyber.png",
      hero_gold: "assets/mandala_hero_solar.png",
      hero_prism: "assets/mandala_hero_prism.png",
      enemy_pulsar: "assets/mandala_enemy_pulsar.png",
      enemy_seeker: "assets/mandala_enemy_seeker.png",
      enemy_wanderer: "assets/mandala_enemy_wanderer.png"
    };
    for (const [k, src] of Object.entries(assetMap)) {
      const img = new Image();
      img.src = src;
      this.images[k] = img;
    }
  }

  initSfxPool() {
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
  }

  synthModeSwitchChime(mode) {
    if (!this.audioCtx || this.prefs.sfxMuted) return;
    try {
      if (this.audioCtx.state === "suspended") this.audioCtx.resume();
      const pitchMult = mode.pitch || mode.speed || 1.0;
      const baseNotes = mode.id === "OVERCLOCK"
        ? [523.25, 659.25, 783.99, 1046.50] // Bright ascending C5-E5-G5-C6
        : mode.id === "SLOWED"
        ? [392.00, 311.13, 261.63] // Deep lush descending G4-Eb4-C4
        : mode.id === "ENDLESS"
        ? [440.00, 554.37, 659.25, 880.00] // Golden A-major shimmer
        : [523.25, 622.25, 783.99]; // Balanced C-minor triad

      baseNotes.forEach((freq, idx) => {
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.type = mode.id === "SLOWED" ? "triangle" : "sine";
        const startT = this.audioCtx.currentTime + idx * 0.045;
        osc.frequency.setValueAtTime(freq * pitchMult, startT);
        gain.gain.setValueAtTime(0.18, startT);
        gain.gain.exponentialRampToValueAtTime(0.001, startT + 0.36);
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);
        osc.start(startT);
        osc.stop(startT + 0.38);
      });
    } catch (e) {}
  }

  unlockAudioOnFirstGesture() {
    if (this.audioUnlocked) return;
    this.audioUnlocked = true;
    this.updateLobbyPlayback();
  }

  updateLobbyPlayback() {
    if (!this.audioUnlocked) return;
    if (this.gameState === "PLAYING" || this.gameState === "DEMO_PLAYER" || this.prefs.musicMuted) {
      this.lobbyAudio.pause();
      return;
    }
    const lobby = (LOBBY_TRACKS && LOBBY_TRACKS[this.prefs.lobbyThemeIndex % LOBBY_TRACKS.length]) || LOBBY_TRACKS[0];
    if (lobby && (!this.lobbyAudio.src || !this.lobbyAudio.src.endsWith(lobby.src))) {
      this.lobbyAudio.src = lobby.src;
    }
    const mode = this.getRemixMode();
    try {
      this.lobbyAudio.preservesPitch = false;
      this.lobbyAudio.webkitPreservesPitch = false;
      this.lobbyAudio.mozPreservesPitch = false;
      this.lobbyAudio.playbackRate = mode.speed || 1.0;
    } catch (e) {}
    this.lobbyAudio.play().catch(() => {});
  }

  cycleLobbyTheme() {
    this.playSfx("click");
    this.prefs.lobbyThemeIndex = (this.prefs.lobbyThemeIndex + 1) % LOBBY_TRACKS.length;
    this.savePrefs();
    const lobby = LOBBY_TRACKS[this.prefs.lobbyThemeIndex];
    if (this.audioUnlocked && !this.prefs.musicMuted && this.gameState === "MENU") {
      this.lobbyAudio.src = lobby.src;
      this.lobbyAudio.play().catch(() => {});
    }
    this.syncUI();
  }

  savePrefs() {
    localStorage.setItem("nmi_high_score", String(this.prefs.highScore));
    localStorage.setItem("nmi_points", String(this.prefs.points));
    localStorage.setItem("nmi_skin", this.prefs.selectedSkin);
    localStorage.setItem("nmi_trail", this.prefs.selectedTrail);
    localStorage.setItem("nmi_theme", this.prefs.selectedTheme);
    localStorage.setItem("nmi_unlocked_items", JSON.stringify(this.prefs.unlockedItems));
    localStorage.setItem("nmi_unlocked_tracks", JSON.stringify(this.prefs.unlockedTracks));
    localStorage.setItem("nmi_track_progress", JSON.stringify(this.prefs.trackBestProgress));
    localStorage.setItem("nmi_track_medals", JSON.stringify(this.prefs.trackMasteryMedals));
    localStorage.setItem("nmi_lobby_idx", String(this.prefs.lobbyThemeIndex));
    localStorage.setItem("nmi_music_muted", String(this.prefs.musicMuted));
    localStorage.setItem("nmi_sfx_muted", String(this.prefs.sfxMuted));
  }

  resize() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    this.W = window.innerWidth;
    this.H = window.innerHeight;
    this.canvas.width = Math.round(this.W * dpr);
    this.canvas.height = Math.round(this.H * dpr);
    this.ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

    if (this.menuTurntableCanvas && this.menuTurntableCtx) {
      this.menuTurntableCanvas.width = Math.round(138 * dpr);
      this.menuTurntableCanvas.height = Math.round(138 * dpr);
      this.menuTurntableCtx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }
    if (this.hudRevealCanvas && this.hudRevealCtx) {
      this.hudRevealCanvas.width = Math.round(76 * dpr);
      this.hudRevealCanvas.height = Math.round(76 * dpr);
      this.hudRevealCtx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }
    if (this.previewCanvas && this.previewCtx) {
      const rect = this.previewCanvas.getBoundingClientRect();
      const pw = rect.width || 320;
      const ph = rect.height || 135;
      this.previewCanvas.width = Math.round(pw * dpr);
      this.previewCanvas.height = Math.round(ph * dpr);
      this.previewCtx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }
  }

  initStars() {
    this.stars = [];
    for (let i = 0; i < 85; i++) {
      const layer = i % 3;
      this.stars.push({
        x: Math.random() * this.W,
        y: Math.random() * this.H,
        speed: layer === 0 ? 18 : layer === 1 ? 42 : 85,
        size: layer === 0 ? 1.4 : layer === 1 ? 2.3 : 3.5,
        alpha: layer === 0 ? 0.35 : layer === 1 ? 0.65 : 0.92,
        twinkleOffset: Math.random() * 6.28
      });
    }
  }

  getHeroBitmap(skinId = this.prefs.selectedSkin) {
    if (skinId === "skin_gold") return this.images.hero_gold;
    if (skinId === "skin_prism") return this.images.hero_prism;
    if (skinId === "skin_emerald") return this.images.hero_cyan;
    if (skinId === "skin_blood") return this.images.hero_gold;
    return this.images.hero_cyan;
  }

  getEnemyBitmaps() {
    return {
      PULSAR: this.images.enemy_pulsar,
      SEEKER: this.images.enemy_seeker,
      WANDERER: this.images.enemy_wanderer
    };
  }

  bindEvents() {
    window.addEventListener("resize", () => this.resize());

    // Interactive Main Menu Turntable Scratch / Flick Controls
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
    const updatePointer = (clientX, clientY) => {
      this.unlockAudioOnFirstGesture();
      if (this.gameState === "PLAYING") {
        this.targetX = Math.max(this.playerRadius, Math.min(this.W - this.playerRadius, clientX));
        // Slight vertical offset on touch so finger doesn't cover core
        const offsetY = clientY - 36;
        this.targetY = Math.max(this.playerRadius, Math.min(this.H - this.playerRadius, offsetY));
      }
    };

    this.canvas.addEventListener("pointerdown", (e) => {
      updatePointer(e.clientX, e.clientY);
    });
    this.canvas.addEventListener("pointermove", (e) => {
      if (e.buttons > 0 || e.pointerType === "touch") {
        updatePointer(e.clientX, e.clientY);
      }
    });

    // Live Customizer Preview Drag
    if (this.previewCanvas) {
      const onPrevMove = (e) => {
        const r = this.previewCanvas.getBoundingClientRect();
        this.previewTouchPos = {
          x: e.clientX - r.left,
          y: e.clientY - r.top
        };
      };
      this.previewCanvas.addEventListener("pointerdown", onPrevMove);
      this.previewCanvas.addEventListener("pointermove", (e) => {
        if (e.buttons > 0 || e.pointerType === "touch") onPrevMove(e);
      });
      this.previewCanvas.addEventListener("pointerup", () => { this.previewTouchPos = null; });
      this.previewCanvas.addEventListener("pointercancel", () => { this.previewTouchPos = null; });
    }

    // Focus Button Hold
    const focusBtn = document.getElementById("btn-focus-hold");
    if (focusBtn) {
      focusBtn.addEventListener("pointerdown", (e) => {
        e.stopPropagation();
        if (this.focusCharge > 0.12) this.isFocusHolding = true;
      });
      window.addEventListener("pointerup", () => { this.isFocusHolding = false; });
      window.addEventListener("pointercancel", () => { this.isFocusHolding = false; });
    }

    // Track Audio Ended -> Track Completed!
    this.trackAudio.addEventListener("ended", () => {
      if (this.gameState === "PLAYING") {
        this.completeStageTrack();
      }
    });

    // Demo Audio Ended
    this.demoAudio.addEventListener("ended", () => {
      if (this.demoLoopMode) {
        this.demoAudio.currentTime = 0;
        this.demoAudio.play().catch(() => {});
      } else {
        this.syncDemoPlayerUI();
      }
    });
  }

  // ==========================================================================
  // STAGE LAUNCH, PAUSE, REVIVE & COMPLETION
  // ==========================================================================
  dismissHowToPlay(startImmediately = false, dontShowAgain = false) {
    this.playSfx("click");
    if (dontShowAgain) {
      this.prefs.seenHowToPlay = true;
      localStorage.setItem("nmi_seen_htp", "true");
    }
    const shouldLaunch = startImmediately || this.pendingStageLaunch;
    this.pendingStageLaunch = false;

    const card = document.getElementById("htp-modal-card");
    if (card) {
      card.classList.add("bounce-out");
      setTimeout(() => {
        card.classList.remove("bounce-out");
        if (shouldLaunch) {
          this.launchStageCountdown();
        } else {
          this.openScreen("MENU");
        }
      }, 250);
    } else {
      if (shouldLaunch) {
        this.launchStageCountdown();
      } else {
        this.openScreen("MENU");
      }
    }
  }

  startStage(stageIndex = this.selectedStageIndex) {
    this.unlockAudioOnFirstGesture();
    this.playSfx("click");
    this.selectedStageIndex = stageIndex;

    // Show centered zoom-bounce How-To-Play pop-up before countdown starts (unless user clicked "Don't Show Again")
    if (!this.prefs.seenHowToPlay) {
      this.pendingStageLaunch = true;
      this.openScreen("HOW_TO_PLAY");
      return;
    }

    this.launchStageCountdown();
  }

  launchStageCountdown() {
    this.gameState = "COUNTDOWN";
    this.lobbyAudio.pause();
    this.trackAudio.pause();
    this.syncUI();

    const countdownEl = document.getElementById("screen-COUNTDOWN");
    const countValEl = document.getElementById("countdown-val");
    if (countdownEl) countdownEl.style.display = "flex";

    let count = 3;
    if (countValEl) countValEl.textContent = String(count);
    this.playSfx("click", 0.7);

    if (this.countdownTimerId) clearInterval(this.countdownTimerId);

    this.countdownTimerId = setInterval(() => {
      count--;
      if (count > 0) {
        if (countValEl) countValEl.textContent = String(count);
        this.playSfx("click", 0.7);
      } else if (count === 0) {
        if (countValEl) countValEl.textContent = "DROP!";
        this.playSfx("orb", 0.85);
      } else {
        clearInterval(this.countdownTimerId);
        if (countdownEl) countdownEl.style.display = "none";
        this.executeStageRun();
      }
    }, 850);
  }

  executeStageRun() {
    const stage = OFFICIAL_TRACKS[this.selectedStageIndex] || OFFICIAL_TRACKS[0];

    this.score = 0;
    this.level = 1;
    this.survivalSeconds = 0;
    this.combo = 1;
    this.maxComboRun = 1;
    this.comboTimer = 0;
    this.orbsCollectedRun = 0;
    this.screenShake = 0;
    this.shieldTimer = 0;
    this.slowMoTimer = 0;
    this.overdriveTimer = 0;
    this.magnetTimer = 0;
    this.focusCharge = 1.0;
    this.isFocusHolding = false;
    this.reviveUsedThisRun = false;
    this.newlyUnlockedTrack = null;
    this.earnedMedalTier = 0;

    this.playerX = this.W / 2;
    this.playerY = this.H * 0.65;
    this.targetX = this.playerX;
    this.targetY = this.playerY;
    this.trailLength = 0;

    this.enemies = [];
    this.powerUps = [];
    this.motes = [];
    this.shockwaves = [];
    this.floatingTexts = [];
    this.wormholes = [];
    this.orbitalPhenomenon = null;
    this.satellites = [];
    this.telegraphs = [];

    this.spawnTimer = 0;
    this.powerUpSpawnTimer = 0;
    this.wormholeSpawnTimer = 0;
    this.phenomenonCooldownTimer = 10;
    this.nextBeatCueIndex = 0;
    this.nextChoreoSectionIndex = 0;
    this.choreoBannerText = "";
    this.choreoBannerSubtext = "";
    this.choreoBannerTimer = 0;
    this.eclipseModeTimer = 0;
    this.constellationRushTimer = 0;
    this.activeTrackDurationSec = stage.durationSec;
    this.trackElapsedSec = 0;

    this.lobbyAudio.pause();
    this.demoAudio.pause();
    this.trackAudio.src = stage.src;
    this.trackAudio.currentTime = 0;
    this.trackAudio.muted = this.prefs.musicMuted;
    const mode = this.getRemixMode();
    try {
      this.trackAudio.preservesPitch = false;
      this.trackAudio.webkitPreservesPitch = false;
      this.trackAudio.mozPreservesPitch = false;
      this.trackAudio.playbackRate = mode.speed || 1.0;
    } catch (e) {}
    this.trackAudio.play().catch(() => {});

    this.gameState = "PLAYING";
    this.syncUI();
  }

  pauseGame() {
    if (this.gameState !== "PLAYING") return;
    this.playSfx("click");
    this.trackAudio.pause();
    this.gameState = "PAUSED";
    this.syncUI();
  }

  resumeGame() {
    if (this.gameState !== "PAUSED") return;
    this.playSfx("click");
    if (!this.prefs.musicMuted) this.trackAudio.play().catch(() => {});
    this.gameState = "PLAYING";
    this.syncUI();
  }

  revivePlayer() {
    if (this.reviveUsedThisRun) return;
    this.playSfx("powerup");
    this.reviveUsedThisRun = true;
    this.enemies = [];
    this.satellites = [];
    this.orbitalPhenomenon = null;
    this.shieldTimer = 5.0;
    this.overdriveTimer = 2.5;
    this.shockwaves.push({
      x: this.playerX,
      y: this.playerY,
      radius: 10,
      maxRadius: Math.max(this.W, this.H) * 0.65,
      color: [56, 189, 248],
      alpha: 1
    });
    this.floatingTexts.push({
      x: this.playerX,
      y: this.playerY - 45,
      text: "SECOND CHANCE SHIELD!",
      color: [56, 189, 248],
      alpha: 1,
      age: 0
    });
    if (!this.prefs.musicMuted) this.trackAudio.play().catch(() => {});
    this.gameState = "PLAYING";
    this.syncUI();
  }

  completeStageTrack() {
    const stage = OFFICIAL_TRACKS[this.selectedStageIndex];
    this.trackAudio.pause();
    this.playSfx("levelup", 0.85);

    const completionBonus = 500;
    const totalEarned = Math.max(50, Math.floor(this.score / 8)) + completionBonus;
    this.prefs.points += totalEarned;
    if (this.score > this.prefs.highScore) this.prefs.highScore = this.score;

    this.prefs.trackBestProgress[stage.id] = 1.0;
    const wasUnlocked = this.prefs.unlockedTracks.includes(stage.id);
    if (!wasUnlocked) {
      this.prefs.unlockedTracks.push(stage.id);
      this.newlyUnlockedTrack = stage;
    } else {
      this.newlyUnlockedTrack = null;
    }

    const mode = this.getRemixMode();
    let stars = 1; // ★1: Clear song
    if (this.maxComboRun >= 8) stars = 2; // ★2: 8x Beat streak
    if (mode.id === "OVERCLOCK" || mode.id === "SLOWED") stars = 3; // ★3: Beat Nightcore/Slowed
    this.earnedMedalTier = stars;
    if (stars > (this.prefs.trackMasteryMedals[stage.id] || 0)) {
      this.prefs.trackMasteryMedals[stage.id] = stars;
    }

    this.savePrefs();
    this.gameState = "TRACK_COMPLETED";
    this.syncUI();
  }

  triggerGameOver() {
    const stage = OFFICIAL_TRACKS[this.selectedStageIndex];
    this.trackAudio.pause();
    this.playSfx("gameover", 0.8);

    const progress = Math.max(0, Math.min(1, this.trackElapsedSec / Math.max(1, this.activeTrackDurationSec)));
    if (progress > (this.prefs.trackBestProgress[stage.id] || 0)) {
      this.prefs.trackBestProgress[stage.id] = progress;
    }

    if (this.score > this.prefs.highScore) this.prefs.highScore = this.score;
    const earned = Math.max(5, Math.floor(this.score / 10));
    this.prefs.points += earned;
    this.savePrefs();

    this.gameState = "GAME_OVER";
    this.syncUI();
  }

  // ==========================================================================
  // PHYSICS STEP (1:1 Port of GamePhysics.kt)
  // ==========================================================================
  stepPhysics(dt) {
    const stage = OFFICIAL_TRACKS[this.selectedStageIndex] || OFFICIAL_TRACKS[0];

    // Sync elapsed time from audio element if playing
    if (!this.trackAudio.paused && this.trackAudio.currentTime > 0) {
      this.trackElapsedSec = this.trackAudio.currentTime;
    } else {
      this.trackElapsedSec += dt;
    }

    if (this.trackElapsedSec >= this.activeTrackDurationSec - 0.15) {
      this.completeStageTrack();
      return;
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

    // Player Spring-Damper Tracking
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

    // 1. Pre-Mapped Beat-Orb Chart Spawner (Confide 115 cues / Number One 95 cues)
    const chart = stage.orbChart;
    while (this.nextBeatCueIndex < chart.length && (chart[this.nextBeatCueIndex].timestampSec || chart[this.nextBeatCueIndex].timeSec) <= this.trackElapsedSec) {
      const cue = chart[this.nextBeatCueIndex];
      this.nextBeatCueIndex++;
      this.spawnChartedOrb(cue);
    }

    // 2. Choreographed Section Director
    const sections = stage.choreography;
    while (this.nextChoreoSectionIndex < sections.length && sections[this.nextChoreoSectionIndex].timeSec <= this.trackElapsedSec) {
      const sec = sections[this.nextChoreoSectionIndex];
      this.nextChoreoSectionIndex++;
      this.choreoBannerText = sec.title;
      this.choreoBannerSubtext = sec.subtitle;
      this.choreoBannerTimer = 3.2;

      if (sec.effectType === "CONSTELLATION_RUSH") {
        this.constellationRushTimer = 12.0;
      } else if (sec.effectType === "ECLIPSE_VORTEX") {
        this.eclipseModeTimer = 10.0;
        if (!this.orbitalPhenomenon) {
          this.spawnOrbitalPhenomenon("BLACK_HOLE");
        }
      } else if (sec.effectType === "SUPERNOVA_BURST") {
        this.constellationRushTimer = 8.0;
        if (!this.orbitalPhenomenon) {
          this.spawnOrbitalPhenomenon("SOLAR_PULSAR");
        }
      } else if (sec.effectType === "OVERDRIVE_DROP") {
        this.overdriveTimer = Math.max(this.overdriveTimer, 4.5);
      }
      this.shockwaves.push({
        x: this.W / 2,
        y: this.H / 2,
        radius: 15,
        maxRadius: Math.max(this.W, this.H) * 0.72,
        color: [250, 204, 21],
        alpha: 1
      });
    }

    // 3. Support Power-Up Spawner (Shield, Slow-Mo, Overdrive, Magnet)
    this.powerUpSpawnTimer += dt;
    if (this.powerUpSpawnTimer >= 8.5) {
      this.powerUpSpawnTimer = 0;
      const supportTypes = ["SHIELD", "SLOW_MO", "OVERDRIVE", "MAGNET"];
      const picked = supportTypes[Math.floor(Math.random() * supportTypes.length)];
      this.spawnTravelingCollectible(picked, Math.random(), 200);
    }

    // 4. Wormhole Pair Spawner
    this.wormholeSpawnTimer += dt;
    if (this.wormholeSpawnTimer >= 16.0 && this.wormholes.length === 0) {
      this.wormholeSpawnTimer = 0;
      const margin = 95;
      const ax = margin + Math.random() * Math.max(60, this.W - margin * 2);
      const ay = margin + Math.random() * Math.max(60, (this.H * 0.45) - margin);
      const bx = margin + Math.random() * Math.max(60, this.W - margin * 2);
      const by = (this.H * 0.55) + Math.random() * Math.max(60, (this.H * 0.35) - margin);
      this.wormholes.push({
        ax, ay, bx, by,
        radius: 36,
        age: 0,
        maxLifetime: 11.5,
        teleportCooldown: 0
      });
    }

    // 5. Orbital Phenomenon Spawner & Gravity Physics
    if (this.orbitalPhenomenon) {
      const op = this.orbitalPhenomenon;
      op.age += worldDt;
      if (op.age >= op.maxLifetime) {
        this.orbitalPhenomenon = null;
        this.satellites = [];
        this.phenomenonCooldownTimer = 14.0;
      } else {
        // Gentle gravitational pull/push on player
        const dx = op.x - this.playerX;
        const dy = op.y - this.playerY;
        const dist = Math.max(40, Math.hypot(dx, dy));
        if (dist < op.radius * 4.5) {
          const pullDir = op.type === "BLACK_HOLE" ? 1 : -0.65;
          const force = (1 - dist / (op.radius * 4.5)) * 115 * pullDir;
          this.playerX += (dx / dist) * force * worldDt;
          this.playerY += (dy / dist) * force * worldDt;
        }
        // Update orbiting satellites
        for (let i = 0; i < this.satellites.length; i++) {
          const sat = this.satellites[i];
          sat.angleRad += sat.angularSpeed * worldDt;
          sat.x = op.x + Math.cos(sat.angleRad) * sat.orbitRadius;
          sat.y = op.y + Math.sin(sat.angleRad) * sat.orbitRadius;
        }
      }
    } else {
      this.phenomenonCooldownTimer -= dt;
      if (this.phenomenonCooldownTimer <= 0 && this.survivalSeconds > 12) {
        this.spawnOrbitalPhenomenon(Math.random() < 0.5 ? "BLACK_HOLE" : "SOLAR_PULSAR");
      }
    }

    // 6. 1:1 Telegraphed Enemy Spawner (Perimeter warning diamonds + laser sightlines!)
    // Step active telegraphs
    for (let i = this.telegraphs.length - 1; i >= 0; i--) {
      const tg = this.telegraphs[i];
      tg.timer -= worldDt;
      if (tg.timer <= 0) {
        // Launch telegraphed enemy into the arena!
        this.enemies.push({
          x: tg.edgeX,
          y: tg.edgeY,
          vx: Math.cos(tg.targetAngle) * tg.speed,
          vy: Math.sin(tg.targetAngle) * tg.speed,
          radius: tg.type === "PULSAR" ? 22 : 18,
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

    // Initiate new telegraphs on the beat (scaled for genuine progressive challenge!)
    const mode = this.getRemixMode();
    const isTrackTwo = this.selectedStageIndex >= 1;
    const trackDiffMult = isTrackTwo ? 1.20 : 1.0;
    const songProgress = Math.max(0, Math.min(1, this.trackElapsedSec / Math.max(1, this.activeTrackDurationSec)));
    const maxEnemies = Math.min(11, Math.round((5 + Math.floor(this.level / 2) + (isTrackTwo ? 1 : 0)) * (mode.speed >= 1.15 ? 1.15 : 1.0)));

    this.spawnTimer += worldDt * trackDiffMult * (1 + songProgress * 0.28) * (mode.speed || 1.0);
    const spawnCooldown = Math.max(0.48, 0.88 - this.level * 0.03);
    const canSpawn = (this.enemies.length + this.telegraphs.length < maxEnemies) &&
                     !this.multiBandState.bassDropOut &&
                     (this.beatPulse > 0.35 || this.spawnTimer >= 1.35);

    if (canSpawn && this.spawnTimer >= spawnCooldown) {
      this.spawnTimer = 0;
      this.initiateEnemyTelegraph();
      // Spawn a 2nd pincer telegraph on chorus / high-energy beats!
      if ((this.constellationRushTimer > 0 || this.multiBandState.rms > 0.68 || this.level >= 4) &&
          (this.enemies.length + this.telegraphs.length < maxEnemies) &&
          Math.random() < 0.48) {
        this.initiateEnemyTelegraph();
      }
    }

    // 7. Update Wormholes & Check Teleportation
    for (let i = this.wormholes.length - 1; i >= 0; i--) {
      const wh = this.wormholes[i];
      wh.age += dt;
      if (wh.teleportCooldown > 0) wh.teleportCooldown = Math.max(0, wh.teleportCooldown - dt);
      if (wh.age >= wh.maxLifetime) {
        this.wormholes.splice(i, 1);
        continue;
      }
      if (wh.teleportCooldown <= 0 && wh.age > 0.6 && wh.age < wh.maxLifetime - 0.6) {
        const distA = Math.hypot(this.playerX - wh.ax, this.playerY - wh.ay);
        const distB = Math.hypot(this.playerX - wh.bx, this.playerY - wh.by);
        if (distA < wh.radius + this.playerRadius * 0.7) {
          this.teleportThroughWormhole(wh, wh.bx, wh.by);
        } else if (distB < wh.radius + this.playerRadius * 0.7) {
          this.teleportThroughWormhole(wh, wh.ax, wh.ay);
        }
      }
    }

    // 8. Update Collectible Orbs & PowerUps (1:1 with Android — active steering required unless Magnet is active!)
    for (let i = this.powerUps.length - 1; i >= 0; i--) {
      const pu = this.powerUps[i];
      pu.age += worldDt;
      if (pu.age >= pu.maxLifetime) {
        this.powerUps.splice(i, 1);
        continue;
      }

      // Gentle sinusoidal drift + velocity
      const speed = Math.max(1, Math.hypot(pu.vx, pu.vy));
      const perpX = -pu.vy / speed;
      const perpY = pu.vx / speed;
      const wFreq = (typeof pu.waveFreq === "number" && isFinite(pu.waveFreq)) ? pu.waveFreq : 2.5;
      const wPhase = (typeof pu.wavePhase === "number" && isFinite(pu.wavePhase)) ? pu.wavePhase : 0;
      const wave = Math.cos(pu.age * wFreq + wPhase) * 25;

      pu.x += (pu.vx + perpX * wave) * worldDt;
      pu.y += (pu.vy + perpY * wave) * worldDt;

      // Soft bounce inside screen margins
      const pad = 28;
      if (pu.x < pad && pu.vx < 0) pu.vx *= -0.85;
      if (pu.x > this.W - pad && pu.vx > 0) pu.vx *= -0.85;
      if (pu.y < pad && pu.vy < 0) pu.vy *= -0.85;
      if (pu.y > this.H - pad && pu.vy > 0) pu.vy *= -0.85;

      const dx = this.playerX - pu.x;
      const dy = this.playerY - pu.y;
      const dist = Math.max(1, Math.hypot(dx, dy));

      // Only pull orbs automatically when Magnet power-up or Tractor Reach is active!
      if (this.magnetTimer > 0 || pu.keyWordEffect === "TRACTOR_REACH") {
        const magnetRange = 290;
        if (dist < magnetRange) {
          const pullStrength = (1 - dist / magnetRange) * 580;
          pu.x += (dx / dist) * pullStrength * dt;
          pu.y += (dy / dist) * pullStrength * dt;
        }
      }

      // Collection Check
      const collectRadius = this.playerRadius + (pu.type === "SCORE_ORB" ? pu.radius * 1.25 : pu.radius * 1.10);
      if (dist <= collectRadius) {
        this.collectPowerUp(pu);
        this.powerUps.splice(i, 1);
      }
    }

    // 9. Update Enemies & Check Collisions
    for (let i = this.enemies.length - 1; i >= 0; i--) {
      const e = this.enemies[i];
      if (e.type === "SEEKER") {
        const dx = this.playerX - e.x;
        const dy = this.playerY - e.y;
        const d = Math.max(1, Math.hypot(dx, dy));
        const curSpeed = Math.hypot(e.vx, e.vy);
        e.vx += (dx / d) * 245 * worldDt;
        e.vy += (dy / d) * 245 * worldDt;
        const newSpeed = Math.max(1, Math.hypot(e.vx, e.vy));
        e.vx = (e.vx / newSpeed) * curSpeed;
        e.vy = (e.vy / newSpeed) * curSpeed;
      } else if (e.type === "WANDERER") {
        e.vx += Math.sin(this.time * 3.8 + e.phase) * 115 * worldDt;
        e.vy += Math.cos(this.time * 3.8 + e.phase) * 115 * worldDt;
      }

      const bassBoost = 1 + this.multiBandState.bass * 0.34;
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
          // Vaporize enemy in Overdrive!
          this.playSfx("orb", 0.75);
          this.score += 120 * this.combo;
          this.spawnBurstMotes(e.x, e.y, [244, 63, 94], 16);
          this.floatingTexts.push({
            x: e.x,
            y: e.y,
            text: `VAPORIZED! +${120 * this.combo}`,
            color: [244, 63, 94],
            alpha: 1,
            age: 0
          });
          this.enemies.splice(i, 1);
        } else if (this.shieldTimer > 0) {
          this.playSfx("shield", 0.8);
          this.shieldTimer = 0;
          this.screenShake = 14;
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
  }

  spawnChartedOrb(cue) {
    const isSupernova = cue.isGoldenCue || (cue.keyWordEffect && cue.keyWordEffect !== "NONE");
    const targetX = (cue.normX || 0.5) * this.W;
    const targetY = (cue.normY || 0.5) * this.H;

    // Pick entry perimeter edge
    const edge = Math.floor(Math.random() * 4);
    let startX, startY;
    if (edge === 0) { startX = targetX + (Math.random() - 0.5) * 120; startY = -35; }
    else if (edge === 1) { startX = targetX + (Math.random() - 0.5) * 120; startY = this.H + 35; }
    else if (edge === 2) { startX = -35; startY = targetY + (Math.random() - 0.5) * 120; }
    else { startX = this.W + 35; startY = targetY + (Math.random() - 0.5) * 120; }

    const speed = isSupernova ? 165 : 195;
    const angle = Math.atan2(targetY - startY, targetX - startX);

    this.powerUps.push({
      x: startX,
      y: startY,
      targetX,
      targetY,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      speed,
      angle,
      radius: isSupernova ? 28 : 22,
      type: "SCORE_ORB",
      keyWordEffect: cue.keyWordEffect || (cue.isGoldenCue ? "CONSTELLATION_STAR" : "NONE"),
      isSupernova,
      sequenceIndex: cue.sequenceIndex || (this.powerUps.length + 1),
      targetBeatSec: cue.timestampSec || this.trackElapsedSec,
      spawnBeatSec: this.trackElapsedSec,
      age: 0,
      maxLifetime: 10.5,
      phase: Math.random() * 6.28,
      waveFreq: 2.4 + Math.random() * 1.2,
      wavePhase: Math.random() * 6.28
    });
  }

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
  }

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

      const targetRatio = Math.max(0.22, Math.min(1, 1 - (pu.age / (pu.maxLifetime * 0.65))));
      const isPerfect = (targetRatio >= 0.25 && targetRatio <= 0.55) || this.beatPulse > 0.45;
      const basePts = isPerfect ? 100 : 45;
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
        text: isPerfect ? `PERFECT! +${pts}` : `GOOD! +${pts}`,
        color: isPerfect ? [74, 222, 128] : [250, 204, 21],
        alpha: 1,
        age: 0
      });
    } else {
      this.playSfx("powerup", 0.8);
      let label = "";
      let col = [56, 189, 248];
      if (pu.type === "SHIELD") {
        this.shieldTimer = 7.0;
        label = "AEGIS SHIELD!";
        col = [56, 189, 248];
      } else if (pu.type === "SLOW_MO") {
        this.slowMoTimer = 5.5;
        label = "CHRONO SHIFT!";
        col = [168, 85, 247];
      } else if (pu.type === "OVERDRIVE") {
        this.overdriveTimer = 5.0;
        label = "OVERDRIVE SURGE!";
        col = [244, 63, 94];
      } else if (pu.type === "MAGNET") {
        this.magnetTimer = 7.0;
        label = "ORB MAGNET!";
        col = [52, 211, 153];
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
      this.floatingTexts.push({
        x: pu.x,
        y: pu.y - 18,
        text: label,
        color: col,
        alpha: 1,
        age: 0
      });
    }
  }

  teleportThroughWormhole(wh, destX, destY) {
    this.playSfx("powerup", 0.75);
    wh.teleportCooldown = 1.6;
    this.playerX = destX;
    this.playerY = destY;
    this.targetX = destX;
    this.targetY = destY;
    this.shieldTimer = Math.max(this.shieldTimer, 1.5);
    this.score += 150 * this.combo;
    this.shockwaves.push({
      x: destX,
      y: destY,
      radius: 10,
      maxRadius: 210,
      color: [168, 85, 247],
      alpha: 1
    });
    this.floatingTexts.push({
      x: destX,
      y: destY - 24,
      text: `WARP JUMP! +${150 * this.combo}`,
      color: [192, 132, 252],
      alpha: 1,
      age: 0
    });
  }

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
  }

  initiateEnemyTelegraph() {
    const edge = Math.floor(Math.random() * 4);
    let edgeX, edgeY;
    if (edge === 0) { edgeX = 36 + Math.random() * (this.W - 72); edgeY = 24; }
    else if (edge === 1) { edgeX = 36 + Math.random() * (this.W - 72); edgeY = this.H - 24; }
    else if (edge === 2) { edgeX = 24; edgeY = 80 + Math.random() * (this.H - 160); }
    else { edgeX = this.W - 24; edgeY = 80 + Math.random() * (this.H - 160); }

    const mode = this.getRemixMode();
    const isTrackTwo = this.selectedStageIndex >= 1;
    const trackDiffMult = isTrackTwo ? 1.18 : 1.0;

    // Target angle aims toward player's current position with tight lead
    const aimAngle = Math.atan2(this.playerY - edgeY, this.playerX - edgeX) + (Math.random() - 0.5) * 0.22;
    const roll = Math.random();
    const type = roll < 0.38 ? "PULSAR" : (roll < 0.76 ? "SEEKER" : "WANDERER");
    const speed = (185 + this.level * 15) * (type === "SEEKER" ? 1.12 : 0.98) * trackDiffMult * (mode.speed || 1.0);

    this.telegraphs.push({
      edgeX,
      edgeY,
      targetAngle: aimAngle,
      speed,
      type,
      timer: 0.80, // Crisp warning before launch
      maxTimer: 0.80
    });
  }

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
  }

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
  }


  // ==========================================================================
  // RENDERING & UI SYNCHRONIZATION
  // ==========================================================================
  loop(timestamp) {
    const dt = Math.min(0.05, Math.max(0.001, (timestamp - this.lastFrameTime) / 1000));
    this.lastFrameTime = timestamp;
    this.time += dt;

    // Update Starfield
    for (let i = 0; i < this.stars.length; i++) {
      const s = this.stars[i];
      s.y += s.speed * dt;
      if (s.y > this.H) {
        s.y = -4;
        s.x = Math.random() * this.W;
      }
    }

    if (this.gameState === "PLAYING") {
      this.stepPhysics(dt);
    } else {
      // Idle gentle beat pulse for menu & background
      this.beatPulse = 0.5 + 0.5 * Math.sin(this.time * 2.1 * Math.PI * 2);
      this.multiBandState.bass = 0.32 + 0.25 * Math.sin(this.time * 1.9);
      this.multiBandState.vocal = 0.35 + 0.30 * Math.cos(this.time * 1.3);
      this.multiBandState.high = 0.28 + 0.22 * Math.sin(this.time * 3.1);
      this.multiBandState.bassDropOut = false;
      this.multiBandState.isClimax = false;
    }

    this.renderGameCanvas();

    if (this.turntableSpinVel) {
      this.turntableAngle = (this.turntableAngle || 0) + this.turntableSpinVel * dt;
      this.turntableSpinVel *= Math.pow(0.08, dt); // smooth vinyl damping
      if (Math.abs(this.turntableSpinVel) < 0.05) this.turntableSpinVel = 0;
    }

    if (this.gameState === "MENU" && this.menuTurntableCtx) {
      const allGold = OFFICIAL_TRACKS.every((t) => (this.prefs.trackMasteryMedals[t.id] || 0) >= 3);
      const effectiveTime = this.time + (this.turntableAngle || 0) * 0.45;
      drawMenuTurntable(this.menuTurntableCtx, this.images.logo, effectiveTime, allGold);
    }

    if (this.gameState === "CUSTOMIZER" && this.previewCtx) {
      this.renderCustomizerPreview(dt);
    }

    if (this.gameState === "DEMO_PLAYER") {
      this.updateDemoPlayerProgress();
    }

    requestAnimationFrame((ts) => this.loop(ts));
  }

  renderGameCanvas() {
    const ctx = this.ctx;
    ctx.save();

    if (this.adrenalineGlitch > 0) {
      this.adrenalineGlitch = Math.max(0, this.adrenalineGlitch - 0.016 * 2.5);
    }

    if (this.screenShake > 0.2 && this.gameState === "PLAYING") {
      const sx = (Math.random() - 0.5) * this.screenShake;
      const sy = (Math.random() - 0.5) * this.screenShake;
      ctx.translate(sx, sy);
    }

    const palette = getThemePalette(this.prefs.selectedTheme);
    const trackProgress = this.gameState === "PLAYING"
      ? Math.max(0, Math.min(1, this.trackElapsedSec / Math.max(1, this.activeTrackDurationSec)))
      : 0;

    drawNebulaBackground(
      ctx,
      this.W,
      this.H,
      palette,
      this.stars,
      this.time,
      this.beatPulse,
      this.multiBandState,
      this.eclipseModeTimer > 0,
      this.constellationRushTimer > 0,
      this.images.logo,
      trackProgress
    );

    if (this.gameState === "PLAYING" || this.gameState === "PAUSED" || this.gameState === "GAME_OVER" || this.gameState === "TRACK_COMPLETED") {
      drawWormholes(ctx, this.wormholes, this.time);
      drawOrbitalPhenomenonAndSatellites(ctx, this.orbitalPhenomenon, this.satellites, this.time, this.beatPulse, this.multiBandState.vocal);
      drawMotes(ctx, this.motes);
      drawPlayerTrail(ctx, this.trailPoints, this.trailLength, this.prefs.selectedTrail);
      drawShockwaves(ctx, this.shockwaves);
      drawPowerUps(ctx, this.powerUps, this.playerX, this.playerY, this.time, this.trackElapsedSec, this.beatPulse, this.constellationRushTimer > 0, this.multiBandState);
      drawEnemyTelegraphs(ctx, this.telegraphs, this.time, this.beatPulse);

      const enemyBmps = this.getEnemyBitmaps();
      for (let i = 0; i < this.enemies.length; i++) {
        drawEnemySprite(ctx, this.enemies[i], enemyBmps, this.time, this.beatPulse);
      }

      drawPlayerSprite(
        ctx,
        this.playerX,
        this.playerY,
        this.playerRadius,
        this.getHeroBitmap(),
        this.prefs.selectedSkin,
        this.shieldTimer,
        this.overdriveTimer,
        this.magnetTimer,
        this.isFocusHolding,
        this.focusCharge,
        this.beatPulse,
        this.time
      );

      drawFloatingTexts(ctx, this.floatingTexts);
    }

    ctx.restore();
  }

  renderCustomizerPreview(dt) {
    const ctx = this.previewCtx;
    const rect = this.previewCanvas.getBoundingClientRect();
    const w = rect.width || 320;
    const h = rect.height || 135;

    const palette = getThemePalette(this.prefs.selectedTheme);
    const bgGrad = ctx.createLinearGradient(0, 0, 0, h);
    bgGrad.addColorStop(0, rgb(palette.bgTop));
    bgGrad.addColorStop(0.5, rgb(palette.bgMid));
    bgGrad.addColorStop(1, rgb(palette.bgBot));
    ctx.fillStyle = bgGrad;
    ctx.fillRect(0, 0, w, h);

    // Organic rotating halo petal arcs inside preview box
    const maxDim = Math.max(w, h);
    for (let i = 0; i < 4; i++) {
      const rawPhase = ((i / 4) + this.time * 0.12) % 1;
      const ringRad = rawPhase * rawPhase * maxDim * 0.52;
      if (ringRad < 12) continue;
      const col = i % 2 === 0 ? palette.nebTint : palette.starTint;
      const fadeAlpha = Math.sin(rawPhase * Math.PI) * 0.24;
      const baseSpin = this.time * (i % 2 === 0 ? 0.45 : -0.38) + i * 0.8;
      for (let p = 0; p < 3; p++) {
        const startA = baseSpin + p * ((Math.PI * 2) / 3);
        ctx.beginPath();
        ctx.arc(w / 2, h / 2, ringRad, startA, startA + 1.35);
        ctx.strokeStyle = rgba(col, fadeAlpha);
        ctx.lineWidth = 1.6;
        ctx.lineCap = "round";
        ctx.stroke();
      }
    }

    // Target position: user drag or smooth figure-8 orbit
    let tx, ty;
    if (this.previewTouchPos) {
      tx = Math.max(28, Math.min(w - 28, this.previewTouchPos.x));
      ty = Math.max(28, Math.min(h - 28, this.previewTouchPos.y));
    } else {
      tx = w * 0.5 + Math.cos(this.time * 2.1) * (w * 0.28);
      ty = h * 0.54 + Math.sin(this.time * 4.2) * (h * 0.20);
    }

    if (this.previewHeroPos.x === 0 && this.previewHeroPos.y === 0) {
      this.previewHeroPos.x = tx;
      this.previewHeroPos.y = ty;
    } else {
      this.previewHeroPos.x += (tx - this.previewHeroPos.x) * 0.22;
      this.previewHeroPos.y += (ty - this.previewHeroPos.y) * 0.22;
    }

    this.previewTrail.unshift({ x: this.previewHeroPos.x, y: this.previewHeroPos.y });
    if (this.previewTrail.length > 22) this.previewTrail.pop();

    drawPlayerTrail(ctx, this.previewTrail, this.previewTrail.length, this.prefs.selectedTrail);

    let skinCol = [56, 189, 248];
    if (this.prefs.selectedSkin === "skin_gold") skinCol = [250, 204, 21];
    else if (this.prefs.selectedSkin === "skin_prism") skinCol = [236, 72, 153];

    const haloGrad = ctx.createRadialGradient(
      this.previewHeroPos.x, this.previewHeroPos.y, 2,
      this.previewHeroPos.x, this.previewHeroPos.y, 44
    );
    haloGrad.addColorStop(0, rgba(skinCol, 0.58));
    haloGrad.addColorStop(1, "rgba(0,0,0,0)");
    ctx.beginPath();
    ctx.arc(this.previewHeroPos.x, this.previewHeroPos.y, 44, 0, Math.PI * 2);
    ctx.fillStyle = haloGrad;
    ctx.fill();

    const bmp = this.getHeroBitmap();
    if (bmp && bmp.complete && bmp.naturalWidth > 0) {
      ctx.save();
      ctx.translate(this.previewHeroPos.x, this.previewHeroPos.y);
      ctx.rotate(((this.time * 45) % 360) * Math.PI / 180);
      ctx.globalCompositeOperation = "screen";
      ctx.drawImage(bmp, -28, -28, 56, 56);
      ctx.restore();
    }
  }

  updateLiveHUD() {
    const stage = OFFICIAL_TRACKS[this.selectedStageIndex] || OFFICIAL_TRACKS[0];
    const progress = Math.max(0, Math.min(1, this.trackElapsedSec / Math.max(1, this.activeTrackDurationSec)));

    const scoreEl = document.getElementById("hud-score-val");
    if (scoreEl) scoreEl.textContent = String(this.score);

    const phaseEl = document.getElementById("hud-phase-val");
    if (phaseEl) phaseEl.textContent = `PHASE ${this.level}`;

    const comboChip = document.getElementById("hud-combo-chip");
    const comboVal = document.getElementById("hud-combo-val");
    if (comboChip && comboVal) {
      if (this.combo > 1 || this.constellationRushTimer > 0) {
        comboChip.style.display = "inline-flex";
        const effMult = this.combo * (this.constellationRushTimer > 0 ? 2 : 1);
        comboVal.textContent = this.constellationRushTimer > 0 ? `★ RUSH x${effMult}` : `COMBO x${this.combo}`;
      } else {
        comboChip.style.display = "none";
      }
    }

    // Power-up status badges
    const badgesEl = document.getElementById("hud-powerup-badges");
    if (badgesEl) {
      let html = "";
      if (this.shieldTimer > 0) html += `<div class="pu-pill" style="border-color:#38BDF8;color:#38BDF8;">🛡 SHIELD ${this.shieldTimer.toFixed(1)}s</div>`;
      if (this.overdriveTimer > 0) html += `<div class="pu-pill" style="border-color:#F43F5E;color:#F43F5E;">⚡ OVERDRIVE ${this.overdriveTimer.toFixed(1)}s</div>`;
      if (this.magnetTimer > 0) html += `<div class="pu-pill" style="border-color:#34D399;color:#34D399;">🧲 MAGNET ${this.magnetTimer.toFixed(1)}s</div>`;
      if (this.slowMoTimer > 0) html += `<div class="pu-pill" style="border-color:#C084FC;color:#C084FC;">⏳ CHRONO ${this.slowMoTimer.toFixed(1)}s</div>`;
      badgesEl.innerHTML = html;
    }

    // Track Reveal Overlay Card
    if (this.hudRevealCtx) {
      drawTrackRevealCanvas(this.hudRevealCtx, this.images.logo, progress, this.beatPulse, this.constellationRushTimer > 0);
    }
    const pctBadge = document.getElementById("hud-reveal-pct");
    if (pctBadge) {
      pctBadge.textContent = `${Math.round(progress * 100)}%`;
      pctBadge.style.color = (this.constellationRushTimer > 0 || progress >= 0.99) ? "#FACC15" : "#38BDF8";
    }
    const trackTitleHud = document.getElementById("hud-track-title");
    if (trackTitleHud) trackTitleHud.textContent = stage.title.toUpperCase();

    const remSec = Math.max(0, Math.round(this.activeTrackDurationSec - this.trackElapsedSec));
    const remMin = Math.floor(remSec / 60);
    const remS = String(remSec % 60).padStart(2, "0");
    const remEl = document.getElementById("hud-track-rem");
    if (remEl) {
      remEl.textContent = progress >= 0.99 ? "UNLOCKED!" : `-${remMin}:${remS} LEFT`;
      remEl.style.color = progress >= 0.99 ? "#4ADE80" : "#94A3B8";
    }

    // Choreographed Section Banner
    const bannerEl = document.getElementById("hud-choreo-banner");
    if (bannerEl) {
      if (this.choreoBannerTimer > 0 && this.choreoBannerText) {
        bannerEl.style.display = "block";
        document.getElementById("hud-choreo-title").textContent = this.choreoBannerText;
        document.getElementById("hud-choreo-sub").textContent = this.choreoBannerSubtext;
      } else {
        bannerEl.style.display = "none";
      }
    }

    // Focus Charge Bar
    const focusFill = document.getElementById("hud-focus-fill");
    const focusBtn = document.getElementById("btn-focus-hold");
    if (focusFill) {
      focusFill.style.width = `${Math.round(this.focusCharge * 100)}%`;
      focusFill.style.background = this.isFocusHolding ? "#FACC15" : "#C084FC";
    }
    if (focusBtn) {
      focusBtn.style.borderColor = this.isFocusHolding ? "#FACC15" : "rgba(192,132,252,0.75)";
      focusBtn.style.color = this.isFocusHolding ? "#FACC15" : "#E9D5FF";
    }
  }

    openLinerNotes(trackIndex = this.selectedStageIndex) {
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
      track_01: "\"Can I confide in you tonight?\nNo more illusions to hide behind...\nThrough the dark void and the frequency light,\nWe break the signal and come alive.\"",
      track_02: "\"Started from zero now we taking flight,\nNumber one frequency burning bright...\nBassline knocking through the midnight air,\nTell them No More Illusions is already there.\""
    };

    if (storyEl) storyEl.textContent = stories[track.id] || stories.track_01;
    if (lyricsEl) lyricsEl.textContent = lyrics[track.id] || lyrics.track_01;

    this.gameState = "LINER_NOTES";
    this.syncUI();
  }

  closeLinerNotes() {
    this.playSfx("click");
    this.openScreen("CUSTOMIZER");
  }

  openScreen(stateName) {
    this.unlockAudioOnFirstGesture();
    this.playSfx("click");
    if (this.gameState === "DEMO_PLAYER" && stateName !== "DEMO_PLAYER") {
      this.demoAudio.pause();
    }
    this.gameState = stateName;
    if (stateName === "CUSTOMIZER") {
      setTimeout(() => this.resize(), 20);
    }
    if (stateName === "DEMO_PLAYER") {
      this.selectDemoTrack(this.demoTrackIndex, false);
    }
    this.updateLobbyPlayback();
    this.syncUI();
  }

  selectStage(index) {
    this.unlockAudioOnFirstGesture();
    this.playSfx("click");
    this.selectedStageIndex = index;
    this.syncUI();
  }

  syncUI() {
    // Show only active screen overlay
    const screens = ["MENU", "PLAYING", "PAUSED", "GAME_OVER", "TRACK_COMPLETED", "HOW_TO_PLAY", "SETTINGS", "DEMO_PLAYER", "CUSTOMIZER", "COUNTDOWN", "LINER_NOTES"];
    for (const s of screens) {
      const el = document.getElementById(`screen-${s}`);
      if (el) el.style.display = (this.gameState === s) ? "flex" : "none";
    }

    // Sync Menu Screen
    const allGold = OFFICIAL_TRACKS.every((t) => (this.prefs.trackMasteryMedals[t.id] || 0) >= 3);
    const ptsEl = document.getElementById("menu-points-val");
    if (ptsEl) ptsEl.textContent = String(this.prefs.points);

    const lobby = LOBBY_TRACKS[this.prefs.lobbyThemeIndex % LOBBY_TRACKS.length];
    const activeRemix = this.getRemixMode();
    const lobbyTextEl = document.getElementById("menu-lobby-text");
    if (lobbyTextEl) {
      lobbyTextEl.textContent = `LOBBY: ${lobby.title.toUpperCase()} (${activeRemix.shortTag}) ↻`;
    }

    const titleMain = document.getElementById("menu-title-main");
    if (titleMain) titleMain.style.color = allGold ? "#FEF08A" : "#F8FAFC";

    const subBanner = document.getElementById("menu-sub-banner");
    if (subBanner) {
      subBanner.style.color = allGold ? "#FACC15" : "#4ADE80";
      subBanner.innerHTML = allGold
        ? `★ 100% ALBUM MASTERY • BEST: <span id="menu-best-score">${this.prefs.highScore}</span>`
        : `INTERACTIVE VISUAL ALBUM • BEST SCORE: <span id="menu-best-score">${this.prefs.highScore}</span>`;
    }

    const activeStage = OFFICIAL_TRACKS[this.selectedStageIndex] || OFFICIAL_TRACKS[0];

    // 1. Track Vinyl Selector Cards (#menu-track-deck-row) — 1:1 with Android MenuScreen.kt
    const trackDeckRow = document.getElementById("menu-track-deck-row");
    if (trackDeckRow) {
      trackDeckRow.innerHTML = OFFICIAL_TRACKS.map((track, idx) => {
        const isSelected = idx === this.selectedStageIndex;
        const stars = this.prefs.trackMasteryMedals[track.id] || 0;
        const starsStr = [1, 2, 3].map(s => `<span style="color:${s <= stars ? "#FACC15" : "#334155"};">★</span>`).join("");
        return `
          <div class="track-card ${isSelected ? "selected" : ""}" onclick="window.app.selectStage(${idx})">
            <div class="track-card-header">
              <span class="track-card-bpm">SONG 0${idx + 1} • ${track.bpm} BPM</span>
              <span class="track-card-stars">${starsStr}</span>
            </div>
            <div class="track-card-title">${track.title.toUpperCase()}</div>
          </div>
        `;
      }).join("");
    }

    // 2. 4-Mode Replayability Selector (#menu-remix-pills & #menu-remix-subtitle) — 1:1 with Android MenuScreen.kt
    const remixSubEl = document.getElementById("menu-remix-subtitle");
    if (remixSubEl) {
      remixSubEl.textContent = activeRemix.subtitle;
      remixSubEl.style.color = activeRemix.color;
    }

    const remixPillsEl = document.getElementById("menu-remix-pills");
    if (remixPillsEl) {
      remixPillsEl.innerHTML = TRACK_REMIX_MODES.map((mode) => {
        const isSelected = mode.id === activeRemix.id;
        return `
          <div class="remix-pill ${isSelected ? "selected" : ""}"
               style="border-color:${isSelected ? mode.color : "rgba(255,255,255,0.12)"};background:${isSelected ? "rgba(255,255,255,0.09)" : "#050911"};"
               onclick="window.app.setRemixMode('${mode.id}')">
            <div class="remix-pill-tag" style="color:${isSelected ? mode.color : "#94A3B8"};">${mode.shortTag}</div>
            <div class="remix-pill-id" style="color:${isSelected ? "#FFFFFF" : "#64748B"};">${mode.displayName}</div>
          </div>
        `;
      }).join("");
    }

    // 3. Console Card Border & Launch Button (#menu-launch-main & #btn-launch-stage)
    const consoleCard = document.getElementById("menu-console-card");
    if (consoleCard) {
      consoleCard.style.borderColor = allGold ? "#FACC15" : activeRemix.color;
    }

    const launchBtn = document.getElementById("btn-launch-stage");
    if (launchBtn) {
      launchBtn.style.borderColor = activeRemix.color;
    }

    const launchMain = document.getElementById("menu-launch-main");
    if (launchMain) {
      launchMain.innerHTML = `▶ &nbsp;PLAY ${activeStage.title.toUpperCase()} (${activeRemix.displayName})`;
    }

    const vaultCountEl = document.getElementById("menu-vault-count");
    if (vaultCountEl) {
      vaultCountEl.textContent = `${this.prefs.unlockedTracks.length}/${OFFICIAL_TRACKS.length} UNLOCKED`;
    }

    // Settings toggles
    const musicToggle = document.getElementById("settings-music-btn");
    if (musicToggle) {
      musicToggle.textContent = this.prefs.musicMuted ? "MUTED" : "ACTIVE";
      musicToggle.className = `toggle-pill ${this.prefs.musicMuted ? "off" : "on"}`;
    }
    const sfxToggle = document.getElementById("settings-sfx-btn");
    if (sfxToggle) {
      sfxToggle.textContent = this.prefs.sfxMuted ? "MUTED" : "ACTIVE";
      sfxToggle.className = `toggle-pill ${this.prefs.sfxMuted ? "off" : "on"}`;
    }

    // Game Over Screen
    if (this.gameState === "GAME_OVER") {
      const goScore = document.getElementById("gameover-score-val");
      if (goScore) goScore.textContent = String(this.score);
      const goBest = document.getElementById("gameover-best-val");
      if (goBest) goBest.textContent = `BEST RECORD: ${this.prefs.highScore}`;
      const reviveBtn = document.getElementById("gameover-revive-btn");
      if (reviveBtn) reviveBtn.style.display = this.reviveUsedThisRun ? "none" : "flex";
    }

    // Track Completed Screen
    if (this.gameState === "TRACK_COMPLETED") {
      const tcTitle = document.getElementById("tc-track-title");
      if (tcTitle) tcTitle.textContent = `"${activeStage.title.toUpperCase()}" MASTERED!`;
      const tcMedal = document.getElementById("tc-medal-badge");
      if (tcMedal) {
        const label = this.earnedMedalTier === 3 ? "🥇 GOLD RECORD MASTER" : this.earnedMedalTier === 2 ? "🥈 SILVER STUDIO CUT" : "🥉 BRONZE STAGE CLEAR";
        tcMedal.textContent = `${label} • MAX COMBO x${this.maxComboRun}`;
      }
      const tcScore = document.getElementById("tc-score-val");
      if (tcScore) tcScore.textContent = String(this.score);
    }

    if (this.gameState === "CUSTOMIZER") {
      this.syncCustomizerUI();
    }
    if (this.gameState === "DEMO_PLAYER") {
      this.syncDemoPlayerUI();
    }
  }

  toggleMusicMute() {
    this.prefs.musicMuted = !this.prefs.musicMuted;
    this.trackAudio.muted = this.prefs.musicMuted;
    this.demoAudio.muted = this.prefs.musicMuted;
    this.savePrefs();
    this.updateLobbyPlayback();
    this.syncUI();
  }

  toggleSfxMute() {
    this.prefs.sfxMuted = !this.prefs.sfxMuted;
    this.savePrefs();
    this.playSfx("click");
    this.syncUI();
  }

  // ==========================================================================
  // STUDIO CUSTOMIZER & ALBUM VAULT
  // ==========================================================================
  setCustomizerTab(tabIdx) {
    this.playSfx("click");
    this.customizerTab = tabIdx;
    this.syncCustomizerUI();
  }

  buyOrEquipItem(itemId, price, category) {
    const isUnlocked = this.prefs.unlockedItems.includes(itemId);
    if (isUnlocked) {
      this.playSfx("click");
      if (category === "skin") this.prefs.selectedSkin = itemId;
      else if (category === "trail") this.prefs.selectedTrail = itemId;
      else if (category === "theme") this.prefs.selectedTheme = itemId;
      this.savePrefs();
      this.syncCustomizerUI();
    } else if (this.prefs.points >= price) {
      this.playSfx("powerup");
      this.prefs.points -= price;
      this.prefs.unlockedItems.push(itemId);
      if (category === "skin") this.prefs.selectedSkin = itemId;
      else if (category === "trail") this.prefs.selectedTrail = itemId;
      else if (category === "theme") this.prefs.selectedTheme = itemId;
      this.savePrefs();
      this.syncCustomizerUI();
    }
  }

  syncCustomizerUI() {
    const ptsEl = document.getElementById("cust-points-val");
    if (ptsEl) ptsEl.textContent = `♪ ${this.prefs.points}`;

    // Unlocked Track Vault Cards
    const vaultListEl = document.getElementById("cust-vault-tracks");
    if (vaultListEl) {
      vaultListEl.innerHTML = OFFICIAL_TRACKS.map((t, idx) => {
        const isUnlocked = this.prefs.unlockedTracks.includes(t.id);
        const bestProg = Math.max(isUnlocked ? 1 : 0, this.prefs.trackBestProgress[t.id] || 0);
        const pct = Math.round(bestProg * 100);
        return `
          <div class="vault-track-row ${isUnlocked ? "unlocked" : "locked"}" onclick="${isUnlocked ? `window.app.openTrackInDemo(${idx})` : ""}">
            <div class="vault-track-icon">${isUnlocked ? "▶" : "🔒"}</div>
            <div style="flex:1;min-width:0;">
              <div class="vault-track-name">${t.title}</div>
              <div class="vault-track-sub" style="color:${isUnlocked ? "#4ADE80" : "#94A3B8"};">
                ${isUnlocked ? "UNLOCKED • TAP TO LISTEN FULL TRACK" : `Complete Stage (${pct}% Revealed) to unlock`}
              </div>
            </div>
            <div class="vault-track-dur">${t.durationLabel}</div>
          </div>
        `;
      }).join("");
    }

    // Tabs
    for (let i = 0; i < 3; i++) {
      const tBtn = document.getElementById(`cust-tab-${i}`);
      if (tBtn) tBtn.className = `cust-tab ${this.customizerTab === i ? "active" : ""}`;
    }

    const itemsByTab = [
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
    ];

    const activeItems = itemsByTab[this.customizerTab] || itemsByTab[0];
    const itemsContainer = document.getElementById("cust-items-list");
    if (itemsContainer) {
      itemsContainer.innerHTML = activeItems.map((it) => {
        const isUnlocked = this.prefs.unlockedItems.includes(it.id);
        const isSelected = (it.cat === "skin" && this.prefs.selectedSkin === it.id) ||
                           (it.cat === "trail" && this.prefs.selectedTrail === it.id) ||
                           (it.cat === "theme" && this.prefs.selectedTheme === it.id);
        const canAfford = this.prefs.points >= it.price;
        const btnLabel = isSelected ? "EQUIPPED" : isUnlocked ? "EQUIP" : `♪ ${it.price}`;
        const btnClass = isSelected ? "cust-btn equipped" : isUnlocked ? "cust-btn equip" : (canAfford ? "cust-btn buy" : "cust-btn locked");
        return `
          <div class="cust-item-row ${isSelected ? "selected" : ""}">
            <div class="cust-swatch" style="background:${it.col};box-shadow:0 0 12px ${it.col};"></div>
            <div style="flex:1;">
              <div class="cust-item-name">${it.name}</div>
              <div class="cust-item-sub">${it.sub}</div>
            </div>
            <button class="${btnClass}" onclick="window.app.buyOrEquipItem('${it.id}', ${it.price}, '${it.cat}')">${btnLabel}</button>
          </div>
        `;
      }).join("");
    }
  }

  // ==========================================================================
  // ALBUM VAULT & STUDIO PLAYER (MusicDemoScreen.kt 1:1 Port)
  // ==========================================================================
  openTrackInDemo(idx) {
    this.demoTrackIndex = idx;
    this.gameState = "DEMO_PLAYER";
    this.lobbyAudio.pause();
    this.selectDemoTrack(idx, true);
    this.syncUI();
  }

  selectDemoTrack(idx, autoPlay = true) {
    this.playSfx("click");
    this.demoTrackIndex = idx;
    const track = OFFICIAL_TRACKS[idx] || OFFICIAL_TRACKS[0];
    const isUnlocked = this.prefs.unlockedTracks.includes(track.id);
    this.demoAudio.pause();
    if (isUnlocked) {
      this.demoAudio.src = track.src;
      this.demoAudio.currentTime = 0;
      if (autoPlay && !this.prefs.musicMuted) {
        this.demoAudio.play().catch(() => {});
      }
    }
    this.syncDemoPlayerUI();
  }

  toggleDemoPlayPause() {
    const track = OFFICIAL_TRACKS[this.demoTrackIndex] || OFFICIAL_TRACKS[0];
    if (!this.prefs.unlockedTracks.includes(track.id)) {
      this.startStage(this.demoTrackIndex);
      return;
    }
    this.playSfx("click");
    if (this.demoAudio.paused) {
      if (!this.demoAudio.src.endsWith(track.src)) this.demoAudio.src = track.src;
      this.demoAudio.play().catch(() => {});
    } else {
      this.demoAudio.pause();
    }
    this.syncDemoPlayerUI();
  }

  seekDemoBy(deltaSec) {
    const track = OFFICIAL_TRACKS[this.demoTrackIndex] || OFFICIAL_TRACKS[0];
    if (!this.prefs.unlockedTracks.includes(track.id)) return;
    this.demoAudio.currentTime = Math.max(0, Math.min(this.demoAudio.duration || track.durationSec, this.demoAudio.currentTime + deltaSec));
    this.updateDemoPlayerProgress();
  }

  toggleDemoLoop() {
    this.playSfx("click");
    this.demoLoopMode = !this.demoLoopMode;
    this.syncDemoPlayerUI();
  }

  updateDemoPlayerProgress() {
    const track = OFFICIAL_TRACKS[this.demoTrackIndex] || OFFICIAL_TRACKS[0];
    const cur = this.demoAudio.currentTime || 0;
    const dur = this.demoAudio.duration || track.durationSec || 179;
    const pct = Math.max(0, Math.min(100, (cur / dur) * 100));
    const fill = document.getElementById("demo-seek-fill");
    if (fill) fill.style.width = `${pct}%`;

    const fmt = (s) => {
      const m = Math.floor(s / 60);
      const sec = String(Math.floor(s % 60)).padStart(2, "0");
      return `${m}:${sec}`;
    };
    const curEl = document.getElementById("demo-time-cur");
    const durEl = document.getElementById("demo-time-dur");
    if (curEl) curEl.textContent = fmt(cur);
    if (durEl) durEl.textContent = fmt(dur);
  }

  syncDemoPlayerUI() {
    const track = OFFICIAL_TRACKS[this.demoTrackIndex] || OFFICIAL_TRACKS[0];
    const isUnlocked = this.prefs.unlockedTracks.includes(track.id);
    const bestProg = Math.max(isUnlocked ? 1 : 0, this.prefs.trackBestProgress[track.id] || 0);

    const titleEl = document.getElementById("demo-active-title");
    if (titleEl) titleEl.textContent = track.title;

    const statusEl = document.getElementById("demo-active-status");
    if (statusEl) {
      statusEl.textContent = isUnlocked ? "★ UNLOCKED IN ALBUM VAULT" : `🔒 LOCKED (${Math.round(bestProg * 100)}% REVEALED)`;
      statusEl.style.color = isUnlocked ? "#4ADE80" : "#FACC15";
    }

    const playBtn = document.getElementById("demo-play-btn");
    if (playBtn) {
      if (!isUnlocked) {
        playBtn.textContent = "PLAY STAGE TO UNLOCK";
        playBtn.style.fontSize = "11px";
      } else {
        playBtn.textContent = this.demoAudio.paused ? "▶" : "❚❚";
        playBtn.style.fontSize = "22px";
      }
    }

    const loopBtn = document.getElementById("demo-loop-btn");
    if (loopBtn) {
      loopBtn.textContent = this.demoLoopMode ? "LOOP: ON" : "LOOP: OFF";
      loopBtn.style.color = this.demoLoopMode ? "#FACC15" : "#94A3B8";
    }

    const listEl = document.getElementById("demo-track-list");
    if (listEl) {
      listEl.innerHTML = OFFICIAL_TRACKS.map((t, idx) => {
        const u = this.prefs.unlockedTracks.includes(t.id);
        const sel = idx === this.demoTrackIndex;
        const p = Math.round(Math.max(u ? 1 : 0, this.prefs.trackBestProgress[t.id] || 0) * 100);
        return `
          <div class="vault-track-row ${sel ? "selected" : ""}" onclick="window.app.selectDemoTrack(${idx}, true)">
            <div class="vault-track-icon" style="color:${u ? "#4ADE80" : "#FACC15"};">${u ? "▶" : "🔒"}</div>
            <div style="flex:1;">
              <div class="vault-track-name">${t.title}</div>
              <div class="vault-track-sub">${u ? `${t.subtitle} • ${t.bpm} BPM` : `${p}% Revealed — Survive stage to unlock`}</div>
            </div>
            <div class="vault-track-dur">${t.durationLabel}</div>
          </div>
        `;
      }).join("");
    }
    this.updateDemoPlayerProgress();
  }
}


window.addEventListener("DOMContentLoaded", () => {
  window.app = new NoMoreIllusionsApp();
});

