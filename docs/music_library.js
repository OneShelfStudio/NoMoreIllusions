// ============================================================================
// NO MORE ILLUSIONS — MUSIC LIBRARY, BEAT CHARTS & MULTI-BAND ENVELOPES
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
  const bassDropOut = isT1
    ? (sec <= 7.2 || (sec >= 62.0 && sec <= 69.2) || (sec >= 124.0 && sec <= 131.2) || sec >= 207.8 || (sec > 8.0 && bass < 0.08))
    : (sec <= 6.5 || (sec >= 51.0 && sec <= 54.4) || (sec >= 105.0 && sec <= 108.9) || sec >= 152.5 || (sec > 8.0 && bass < 0.08));
  return { rms, bass, vocal, high, bassDropOut, swellDelta, isClimax: rms > 0.82 };
}

function buildConfideOrbChart() {
  const beatSec = 60 / 124;
  // ONLY 6 Rare Supernova Orbs across the entire 3:39 track (aligned to Ethan's exact EXTRA JUICE / SLIGHT EXTRA JUICE moments)
  const specialMoments = [
    [14.52, "CONSTELLATION_STAR"], // 0:15 Chorus 1 Drop
    [45.97, "MAGNETAR_WAR"],       // 0:46 Pre-Chorus Swell
    [75.48, "CONSTELLATION_STAR"], // 1:15 Chorus 2 Drop
    [137.42, "CONSTELLATION_STAR"],// 2:17 Chorus 3 Drop
    [169.35, "BINARY_HEART"],      // 2:49 Vocal Battle Swell
    [183.87, "CONSTELLATION_STAR"] // 3:03 Ending Breakdown Climax
  ];
  const cues = [];
  const used = new Set();
  let beatIdx = 3, seq = 1;
  while (beatIdx * beatSec <= 214.0) {
    const t = beatIdx * beatSec;
    const inIntro = t <= 7.0;
    const inBreakdown = (t >= 62.0 && t <= 69.0) || (t >= 124.0 && t <= 131.0);
    const inRiser = (t >= 69.1 && t <= 75.3) || (t >= 131.1 && t <= 137.3);
    const inChorus = (t >= 14.5 && t <= 31.0) || (t >= 75.5 && t <= 93.0) || (t >= 137.5 && t <= 155.0) || (t >= 183.5 && t <= 201.0);
    const inOutro = t >= 207.8;

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

    // Choreographed 4-beat sequential phrase patterns (1 -> 2 -> 3 -> 4) so consecutive stationary notes
    // sit within a comfortable, musical swipe path across the screen!
    const phraseIdx = Math.floor((seq - 1) / 4);
    const stepInPhrase = (seq - 1) % 4; // 0, 1, 2, 3

    let nx, ny;
    if (inRiser) {
      // Ascending staircase zig-zag right up to the drop
      const rp = Math.max(0, Math.min(1, (t < 100 ? (t - 69) : (t - 131)) / 6.3));
      nx = (stepInPhrase % 2 === 0) ? 0.34 : 0.66;
      ny = Math.max(0.26, Math.min(0.74, 0.72 - rp * 0.44));
    } else if (inBreakdown || inOutro || inIntro) {
      // Gentle centered diamond / arc
      const arcAngle = stepInPhrase * (Math.PI * 0.5) + phraseIdx * 0.6;
      nx = Math.max(0.28, Math.min(0.72, 0.50 + 0.16 * Math.cos(arcAngle)));
      ny = Math.max(0.32, Math.min(0.68, 0.48 + 0.12 * Math.sin(arcAngle)));
    } else {
      // Smooth 4-node musical phrases: sweep Left->Right, Right->Left, Diagonal Up, or Arc Down
      const patternType = phraseIdx % 4;
      const prog = stepInPhrase / 3.0; // 0.0, 0.333, 0.667, 1.0
      if (patternType === 0) {
        // Left-to-Right melodic wave
        nx = 0.24 + prog * 0.52;
        ny = 0.36 + 0.12 * Math.sin(prog * Math.PI);
      } else if (patternType === 1) {
        // Right-to-Left lower response wave
        nx = 0.76 - prog * 0.52;
        ny = 0.64 - 0.12 * Math.sin(prog * Math.PI);
      } else if (patternType === 2) {
        // Ascending diagonal cascade
        nx = 0.28 + prog * 0.44;
        ny = 0.66 - prog * 0.30;
      } else {
        // Descending S-curve
        nx = 0.50 + 0.24 * Math.sin((prog - 0.5) * Math.PI);
        ny = 0.32 + prog * 0.34;
      }
    }
    const puType = effect !== "NONE"
      ? (effect === "MAGNETAR_WAR" ? "BLAST" : effect === "ECLIPSE_DARK" ? "SHIELD" : effect === "TRACTOR_REACH" ? "MAGNET" : "SCORE_ORB")
      : (seq % 16 === 4 ? "BLAST" : seq % 16 === 8 ? "SHIELD" : seq % 16 === 12 ? "SLOW_MO" : seq % 16 === 0 ? "MAGNET" : "SCORE_ORB");
    cues.push({
      sequenceIndex: seq,
      timestampSec: quantizeToBeat(anchor ? anchor[0] : t, beatSec),
      normX: nx,
      normY: ny,
      powerUpType: puType,
      keyWordEffect: effect,
      isGoldenCue: effect === "CONSTELLATION_STAR"
    });
    seq++;
    // Higher beat density for linear Piano-Tiles / Osu! succession feel:
    // Every 1 beat in risers, every 2 beats in choruses/verses, every 4 beats in quiet intros/breakdowns
    beatIdx += inRiser ? 1 : (inChorus ? 2 : ((inBreakdown || inOutro || inIntro) ? 4 : 2));
  }
  return cues;
}

function buildNumberOneOrbChart() {
  const beatSec = 60 / 88;
  // ONLY 5 Rare Supernova Orbs across the entire 2:46 track (aligned to Ethan's exact EXTRA JUICE / SLIGHT EXTRA JUICE moments)
  const specialMoments = [
    [44.32, "MAGNETAR_WAR"],       // 0:44 Pre-Chorus 1 Swell
    [54.55, "CONSTELLATION_STAR"], // 0:54 Chorus 1 Drop
    [92.05, "PRISM_SHATTER"],      // 1:32 Rap Flow Double-Time
    [109.09, "CONSTELLATION_STAR"],// 1:49 Chorus 2 Drop
    [129.55, "CONSTELLATION_STAR"] // 2:09 Ending Breakdown Climax
  ];
  const cues = [];
  const used = new Set();
  let beatIdx = 2, seq = 1;
  while (beatIdx * beatSec <= 162.0) {
    const t = beatIdx * beatSec;
    const isIntro = t <= 6.5;
    const isHush = (t >= 51.0 && t <= 54.3) || (t >= 105.0 && t <= 108.8);
    const isRap = t >= 91.5 && t <= 98.5;
    const isChorus = (t >= 54.5 && t <= 76.0) || (t >= 109.0 && t <= 129.0);
    const isClimax = t >= 129.0 && t <= 152.0;
    const isOutro = t >= 152.5;

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

    const phraseIdx = Math.floor((seq - 1) / 4);
    const stepInPhrase = (seq - 1) % 4;

    let nx, ny;
    if (isRap) {
      // Snappy double-time 4-note zig-zag ladder
      nx = (stepInPhrase % 2 === 0) ? 0.30 : 0.70;
      ny = Math.max(0.28, Math.min(0.72, 0.34 + (stepInPhrase * 0.11)));
    } else if (isHush || isOutro || isIntro) {
      const arcAngle = stepInPhrase * (Math.PI * 0.5) + phraseIdx * 0.5;
      nx = Math.max(0.28, Math.min(0.72, 0.50 + 0.15 * Math.cos(arcAngle)));
      ny = Math.max(0.32, Math.min(0.68, 0.48 + 0.11 * Math.sin(arcAngle)));
    } else {
      const patternType = phraseIdx % 4;
      const prog = stepInPhrase / 3.0;
      if (patternType === 0) {
        nx = 0.24 + prog * 0.52;
        ny = 0.38 + 0.12 * Math.sin(prog * Math.PI);
      } else if (patternType === 1) {
        nx = 0.76 - prog * 0.52;
        ny = 0.62 - 0.12 * Math.sin(prog * Math.PI);
      } else if (patternType === 2) {
        nx = 0.28 + prog * 0.44;
        ny = 0.64 - prog * 0.28;
      } else {
        nx = 0.50 + 0.24 * Math.cos((prog - 0.5) * Math.PI);
        ny = 0.32 + prog * 0.34;
      }
    }
    const puType = effect !== "NONE"
      ? (effect === "MAGNETAR_WAR" ? "BLAST" : effect === "ECLIPSE_DARK" ? "SHIELD" : effect === "TRACTOR_REACH" ? "MAGNET" : "SCORE_ORB")
      : (seq % 16 === 4 ? "BLAST" : seq % 16 === 8 ? "SHIELD" : seq % 16 === 12 ? "SLOW_MO" : seq % 16 === 0 ? "MAGNET" : "SCORE_ORB");
    cues.push({
      sequenceIndex: seq,
      timestampSec: quantizeToBeat(anchor ? anchor[0] : t, beatSec),
      normX: nx,
      normY: ny,
      powerUpType: puType,
      keyWordEffect: effect,
      isGoldenCue: effect === "CONSTELLATION_STAR"
    });
    seq++;
    // 88 BPM has 0.68s per beat — step every 1 beat in rap/climax/chorus, every 2 beats in verses, 3 in hush/outro
    beatIdx += (isRap || isClimax || isChorus) ? 1 : ((isHush || isOutro || isIntro) ? 3 : 2);
  }
  return cues;
}


const OFFICIAL_TRACKS = [
  {
    id: "track_01",
    index: 1,
    title: "Confide",
    subtitle: "Official Single • 124 BPM",
    bpm: 124,
    durationSec: 219,
    durationLabel: "3:39",
    unlockCost: 0,
    src: "assets/track_01.mp3",
    audioSrc: "assets/track_01.mp3",
    artKey: "track01Art",
    artSrc: "assets/track_01.png",
    sections: [
      { t: 0, timeSec: 0, title: "AMBIENT INTRO", subtitle: "SYNTH RESONANCE", effectType: "STANDARD", maxEnemies: 0, enemySpeedMult: 0.70, clearEnemiesOnEnter: true, allowAnomalies: false, juiceTier: "NONE" },
      { t: 7.25, timeSec: 7.25, title: "BASS & GUITAR SWELL", subtitle: "SUB-BASS DRIFT", effectType: "WARP_PULSE", maxEnemies: 1, enemySpeedMult: 0.72, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 14.5, timeSec: 14.5, title: "CHORUS I DROP", subtitle: "SUPERNOVA BURST", effectType: "CONSTELLATION_RUSH", maxEnemies: 2, enemySpeedMult: 0.85, clearEnemiesOnEnter: false, allowAnomalies: true, juiceTier: "EXTRA_JUICE" },
      { t: 31.0, timeSec: 31.0, title: "VERSE ONE FLOW", subtitle: "124 BPM SYNTH GROOVE", effectType: "STANDARD", maxEnemies: 1, enemySpeedMult: 0.75, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 45.5, timeSec: 45.5, title: "PRE-CHORUS SURGE", subtitle: "HARMONIC RISER", effectType: "SPEED_PORTAL", maxEnemies: 2, enemySpeedMult: 0.82, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SLIGHT_EXTRA_JUICE" },
      { t: 62.0, timeSec: 62.0, title: "HOOK VOCAL FLOAT", subtitle: "ZERO-BASS PURITY", effectType: "ECLIPSE_VORTEX", maxEnemies: 0, enemySpeedMult: 0.65, clearEnemiesOnEnter: true, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 69.25, timeSec: 69.25, title: "PRE-DROP RISER", subtitle: "ASCENDING STAIRCASE", effectType: "SPEED_PORTAL", maxEnemies: 1, enemySpeedMult: 0.80, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SLIGHT_EXTRA_JUICE" },
      { t: 75.5, timeSec: 75.5, title: "CHORUS II SUPERNOVA", subtitle: "FULL AUDIO OVERDRIVE", effectType: "CONSTELLATION_RUSH", maxEnemies: 3, enemySpeedMult: 0.90, clearEnemiesOnEnter: false, allowAnomalies: true, juiceTier: "EXTRA_JUICE" },
      { t: 93.0, timeSec: 93.0, title: "VERSE TWO CRUISE", subtitle: "KICK & VOCAL LOCK", effectType: "STANDARD", maxEnemies: 1, enemySpeedMult: 0.76, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 108.0, timeSec: 108.0, title: "PRE-CHORUS II SURGE", subtitle: "SUB-BASS EXPANSION", effectType: "WARP_PULSE", maxEnemies: 2, enemySpeedMult: 0.84, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SLIGHT_EXTRA_JUICE" },
      { t: 124.0, timeSec: 124.0, title: "HOOK VOCAL FLOAT II", subtitle: "ATMOSPHERIC VOID", effectType: "ECLIPSE_VORTEX", maxEnemies: 0, enemySpeedMult: 0.65, clearEnemiesOnEnter: true, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 131.25, timeSec: 131.25, title: "PRE-DROP RISER II", subtitle: "ENERGY CHARGE", effectType: "SPEED_PORTAL", maxEnemies: 1, enemySpeedMult: 0.82, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SLIGHT_EXTRA_JUICE" },
      { t: 137.5, timeSec: 137.5, title: "CHORUS III OVERDRIVE", subtitle: "MAXIMUM RESONANCE", effectType: "CONSTELLATION_RUSH", maxEnemies: 3, enemySpeedMult: 0.94, clearEnemiesOnEnter: false, allowAnomalies: true, juiceTier: "EXTRA_JUICE" },
      { t: 155.0, timeSec: 155.0, title: "BRIDGE BREATHER", subtitle: "WARM CHORDS", effectType: "STANDARD", maxEnemies: 1, enemySpeedMult: 0.72, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 169.0, timeSec: 169.0, title: "VOCAL BATTLE SWELL", subtitle: "DUAL HARMONIES", effectType: "WARP_PULSE", maxEnemies: 2, enemySpeedMult: 0.88, clearEnemiesOnEnter: false, allowAnomalies: true, juiceTier: "SLIGHT_EXTRA_JUICE" },
      { t: 183.5, timeSec: 183.5, title: "ENDING BREAKDOWN", subtitle: "24K GOLD OVERDRIVE", effectType: "SUPERNOVA_BURST", maxEnemies: 4, enemySpeedMult: 0.96, clearEnemiesOnEnter: false, allowAnomalies: true, juiceTier: "EXTRA_JUICE" },
      { t: 201.0, timeSec: 201.0, title: "808 HEARTBEAT PULSE", subtitle: "SUB-BASS SHOCKWAVES", effectType: "WARP_PULSE", maxEnemies: 2, enemySpeedMult: 0.82, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SLIGHT_EXTRA_JUICE" },
      { t: 207.8, timeSec: 207.8, title: "VICTORY OUTRO", subtitle: "VELVET DECAY", effectType: "ECLIPSE_VORTEX", maxEnemies: 0, enemySpeedMult: 0.60, clearEnemiesOnEnter: true, allowAnomalies: false, juiceTier: "SECTION_SHIFT" }
    ],
    choreography: [
      { timeSec: 0, title: "AMBIENT INTRO", subtitle: "SYNTH RESONANCE", effectType: "STANDARD" },
      { timeSec: 14.5, title: "CHORUS I DROP", subtitle: "SUPERNOVA BURST", effectType: "CONSTELLATION_RUSH" },
      { timeSec: 62.0, title: "HOOK VOCAL FLOAT", subtitle: "ZERO-BASS PURITY", effectType: "ECLIPSE_VORTEX" },
      { timeSec: 75.5, title: "CHORUS II SUPERNOVA", subtitle: "FULL AUDIO OVERDRIVE", effectType: "CONSTELLATION_RUSH" },
      { timeSec: 124.0, title: "HOOK VOCAL FLOAT II", subtitle: "ATMOSPHERIC VOID", effectType: "ECLIPSE_VORTEX" },
      { timeSec: 137.5, title: "CHORUS III OVERDRIVE", subtitle: "MAXIMUM RESONANCE", effectType: "CONSTELLATION_RUSH" },
      { timeSec: 183.5, title: "ENDING BREAKDOWN", subtitle: "24K GOLD OVERDRIVE", effectType: "SUPERNOVA_BURST" },
      { timeSec: 207.8, title: "VICTORY OUTRO", subtitle: "VELVET DECAY", effectType: "ECLIPSE_VORTEX" }
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
      { t: 0, timeSec: 0, title: "WARM CHORD INTRO", subtitle: "WARM CHORD BED", effectType: "STANDARD", maxEnemies: 0, enemySpeedMult: 0.70, clearEnemiesOnEnter: true, allowAnomalies: false, juiceTier: "NONE" },
      { t: 6.5, timeSec: 6.5, title: "SUB-BASS WARMTH", subtitle: "808 PULSE", effectType: "WARP_PULSE", maxEnemies: 0, enemySpeedMult: 0.72, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 9.75, timeSec: 9.75, title: "HI-HAT SHIMMER", subtitle: "HI-HAT ROLL", effectType: "STANDARD", maxEnemies: 1, enemySpeedMult: 0.74, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 22.0, timeSec: 22.0, title: "VERSE ONE GROOVE", subtitle: "VOCAL FLOW", effectType: "STANDARD", maxEnemies: 1, enemySpeedMult: 0.76, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 44.0, timeSec: 44.0, title: "PRE-CHORUS SWELL", subtitle: "RISER WAVE", effectType: "SPEED_PORTAL", maxEnemies: 2, enemySpeedMult: 0.84, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SLIGHT_EXTRA_JUICE" },
      { t: 51.0, timeSec: 51.0, title: "PRE-DROP HUSH", subtitle: "ATMOSPHERIC HOLD", effectType: "ECLIPSE_VORTEX", maxEnemies: 0, enemySpeedMult: 0.65, clearEnemiesOnEnter: true, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 54.5, timeSec: 54.5, title: "CHORUS I DROP", subtitle: "SUPERNOVA CADENCE", effectType: "CONSTELLATION_RUSH", maxEnemies: 3, enemySpeedMult: 0.90, clearEnemiesOnEnter: false, allowAnomalies: true, juiceTier: "EXTRA_JUICE" },
      { t: 76.0, timeSec: 76.0, title: "VERSE TWO POCKET", subtitle: "RHYTHMIC POCKET", effectType: "STANDARD", maxEnemies: 1, enemySpeedMult: 0.76, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 91.5, timeSec: 91.5, title: "RAP DOUBLE-TIME", subtitle: "DOUBLE-TIME RUSH", effectType: "OVERDRIVE_DROP", maxEnemies: 2, enemySpeedMult: 0.88, clearEnemiesOnEnter: false, allowAnomalies: true, juiceTier: "SLIGHT_EXTRA_JUICE" },
      { t: 101.0, timeSec: 101.0, title: "PRE-CHORUS II BUILD", subtitle: "CRESCENDO SWEEP", effectType: "SPEED_PORTAL", maxEnemies: 2, enemySpeedMult: 0.82, clearEnemiesOnEnter: false, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 105.0, timeSec: 105.0, title: "VOCAL BREATHER", subtitle: "ZERO-BASS HUSH", effectType: "ECLIPSE_VORTEX", maxEnemies: 0, enemySpeedMult: 0.65, clearEnemiesOnEnter: true, allowAnomalies: false, juiceTier: "SECTION_SHIFT" },
      { t: 109.0, timeSec: 109.0, title: "CHORUS II DROP", subtitle: "MAXIMUM IMPACT", effectType: "CONSTELLATION_RUSH", maxEnemies: 3, enemySpeedMult: 0.94, clearEnemiesOnEnter: false, allowAnomalies: true, juiceTier: "EXTRA_JUICE" },
      { t: 129.0, timeSec: 129.0, title: "ENDING BREAKDOWN", subtitle: "ALL INSTRUMENTS PEAK", effectType: "SUPERNOVA_BURST", maxEnemies: 4, enemySpeedMult: 0.98, clearEnemiesOnEnter: false, allowAnomalies: true, juiceTier: "EXTRA_JUICE" },
      { t: 152.5, timeSec: 152.5, title: "VELVET OUTRO", subtitle: "SUBLIME RESOLUTION", effectType: "ECLIPSE_VORTEX", maxEnemies: 0, enemySpeedMult: 0.60, clearEnemiesOnEnter: true, allowAnomalies: false, juiceTier: "SECTION_SHIFT" }
    ],
    choreography: [
      { timeSec: 0, title: "WARM CHORD INTRO", subtitle: "WARM CHORD BED", effectType: "STANDARD" },
      { timeSec: 54.5, title: "CHORUS I DROP", subtitle: "SUPERNOVA CADENCE", effectType: "CONSTELLATION_RUSH" },
      { timeSec: 91.5, title: "RAP DOUBLE-TIME", subtitle: "DOUBLE-TIME RUSH", effectType: "OVERDRIVE_DROP" },
      { timeSec: 109.0, title: "CHORUS II DROP", subtitle: "MAXIMUM IMPACT", effectType: "CONSTELLATION_RUSH" },
      { timeSec: 129.0, title: "ENDING BREAKDOWN", subtitle: "ALL INSTRUMENTS PEAK", effectType: "SUPERNOVA_BURST" },
      { timeSec: 152.5, title: "VELVET OUTRO", subtitle: "SUBLIME RESOLUTION", effectType: "ECLIPSE_VORTEX" }
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
