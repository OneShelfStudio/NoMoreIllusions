import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

# Read engine.js
with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'r', encoding='utf-8') as f:
    js = f.read()

# 1. Update spawnChartedOrb & cue parsing in engine.js
# In buildConfideOrbChart & buildNumberOneOrbChart, make sure the cues have timestampSec, normX, normY, keyWordEffect, isGoldenCue, and powerUpType!
# And in stepPhysics:
# check cue:
# while (this.nextBeatCueIndex < chart.length && (chart[this.nextBeatCueIndex].timestampSec || chart[this.nextBeatCueIndex].timeSec) <= this.trackElapsedSec)

js = js.replace(
    'while (this.nextBeatCueIndex < chart.length && chart[this.nextBeatCueIndex].timeSec <= this.trackElapsedSec)',
    'while (this.nextBeatCueIndex < chart.length && (chart[this.nextBeatCueIndex].timestampSec || chart[this.nextBeatCueIndex].timeSec) <= this.trackElapsedSec)'
)

# 2. Update spawnChartedOrb implementation:
# Instead of random edges, spawn from perimeter with trajectory toward target, matching Android stepNebulousBeatOrbs!
old_spawn = '''spawnChartedOrb(cue) {
    this.spawnTravelingCollectible("SCORE_ORB", cue.laneRatio, cue.speed * (cue.intensity > 1.1 ? 1.15 : 1.0));
  }'''

new_spawn = '''spawnChartedOrb(cue) {
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
      phase: Math.random() * 6.28
    });
  }'''

if old_spawn in js:
    js = js.replace(old_spawn, new_spawn)
    print('Replaced spawnChartedOrb successfully!')
else:
    print('WARNING: old_spawn not matched verbatim, performing regex replace...')
    js = re.sub(r'spawnChartedOrb\(cue\)\s*\{[\s\S]*?\}\s*spawnTravelingCollectible', new_spawn + '\n\n  spawnTravelingCollectible', js)

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'w', encoding='utf-8') as f:
    f.write(js)

print('Updated engine.js spawn logic!')
