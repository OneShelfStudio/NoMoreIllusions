import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'r', encoding='utf-8') as f:
    orig = f.read()

# 1. Extract buildConfideOrbChart & buildNumberOneOrbChart
idx_confide = orig.find('function buildConfideOrbChart')
idx_no1 = orig.find('function buildNumberOneOrbChart')
idx_after_no1 = orig.find('const OFFICIAL_TRACKS =')
build_confide = orig[idx_confide:idx_no1].strip()
build_no1 = orig[idx_no1:idx_after_no1].strip()

# 2. Extract renderers
idx_sec5 = orig.find('// 5. GAME RENDERER')
idx_class = orig.find('class NoMoreIllusionsApp')
renderers = orig[idx_sec5:idx_class].strip()

# 3. Extract NoMoreIllusionsApp class
idx_dom = orig.find('window.addEventListener("DOMContentLoaded"')
if idx_dom == -1:
    idx_dom = orig.find("window.addEventListener('DOMContentLoaded'")
class_code = orig[idx_class:idx_dom].strip()

# Let's inspect class_code adjustments:
# Change new MultiBandState() to default state object or class definition
# Ensure OFFICIAL_TRACKS is an Array!
header = """// ============================================================================
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
      return {
        c1: isFocus ? "#080501" : "#050300",
        c2: isFocus ? "#1A1103" : "#120B02",
        c3: isFocus ? "#0E0902" : "#080501",
        bgTop: [8, 5, 1], bgMid: [26, 17, 3], bgBot: [14, 9, 2],
        nebTint: [250, 204, 21], starTint: [56, 189, 248]
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

  // 4. Aurora Silk Ribbons (3 waves with Synaptic Nodes)
  for (let rib = 0; rib < 3; rib++) {
    ctx.beginPath();
    const ribbonY = centerY - H * 0.18 + rib * (H * 0.12);
    const waveFreq = 0.0035 + rib * 0.001;
    const waveAmp = (22 + rib * 14) * (1 + bass * 0.45);
    const speed = time * (1.2 + rib * 0.5);

    ctx.moveTo(0, ribbonY + Math.sin(speed) * waveAmp);
    for (let x = 0; x <= W; x += 16) {
      const y = ribbonY + Math.sin(x * waveFreq + speed) * waveAmp;
      ctx.lineTo(x, y);
    }
    const ribAlpha = (0.08 + energy * 0.18) * (rib === 1 ? 1.2 : 0.8);
    ctx.strokeStyle = rgba(rib % 2 === 0 ? palette.nebTint : palette.starTint, ribAlpha);
    ctx.lineWidth = 3.5 + high * 3;
    ctx.stroke();
  }

  // 5. Signature Concentric Squircle Tunnel
  const ringCount = isEclipseActive ? 5 : 9;
  const ringSpeed = multiBand.bassDropOut ? 0.04 : (0.07 + bass * 0.12 + high * 0.06);
  for (let i = 0; i < ringCount; i++) {
    const rawPhase = ((i / ringCount) + time * ringSpeed) % 1;
    const curvedPhase = rawPhase * rawPhase;
    const boxHalf = curvedPhase * maxDim * (0.64 + bass * 0.06);
    if (boxHalf < 14) continue;

    const rotRad = (Math.sin(time * (0.50 + vocal * 0.45) + i * 0.52) * (9 + high * 16)) * Math.PI / 180;
    const isEven = i % 2 === 0;
    const baseRingColor = isConstellationActive
      ? (isEven ? [250, 204, 21] : [56, 189, 248])
      : multiBand.bassDropOut
      ? [56, 189, 248]
      : (isEven ? palette.nebTint : palette.starTint);

    const fadeAlpha = Math.max(0, Math.min(1, Math.sin(rawPhase * Math.PI)));
    const finalAlpha = Math.max(0, Math.min(0.36, fadeAlpha * (0.05 + energy * 0.20 + swellBoost * 0.14)));
    const strokeW = 1.1 + rawPhase * (1.6 + bass * 2.6);

    ctx.save();
    ctx.translate(centerX, centerY);
    ctx.rotate(rotRad);
    ctx.strokeStyle = rgba(baseRingColor, finalAlpha);
    ctx.lineWidth = strokeW;
    roundRectPath(ctx, -boxHalf, -boxHalf, boxHalf * 2, boxHalf * 2, boxHalf * 0.38);
    ctx.stroke();
    ctx.restore();
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
"""

tracks_def = """
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

const LOBBY_TRACKS = [
  { id: "VELVET", title: "Velvet Synth", bpm: 92, src: "assets/lobby_velvet.mp3" },
  { id: "MIDNIGHT", title: "Midnight Pulse", bpm: 105, src: "assets/lobby_midnight.mp3" },
  { id: "CRYSTAL", title: "Crystal Mirage", bpm: 118, src: "assets/lobby_crystal.mp3" }
];
"""

footer = """
window.addEventListener("DOMContentLoaded", () => {
  window.app = new NoMoreIllusionsApp();
});
"""

full_code = f"{header}\n\n{build_confide}\n\n{build_no1}\n\n{tracks_def}\n\n{renderers}\n\n{class_code}\n\n{footer}\n"

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'w', encoding='utf-8') as f:
    f.write(full_code)

print(f"Generated clean engine.js! Total length: {len(full_code)} characters")
