import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'r', encoding='utf-8') as f:
    js = f.read()

# 1. Update telegraph rendering:
# In Android GameRenderer.kt:
# High-contrast Crimson Warning Diamond (◆) on the screen border + Crimson Laser Trajectory Sightline!
new_telegraph_fn = '''function drawEnemyTelegraphs(ctx, telegraphs, time, beatPulse) {
  if (!telegraphs || telegraphs.length === 0) return;

  for (let i = 0; i < telegraphs.length; i++) {
    const tg = telegraphs[i];
    const progress = Math.max(0, Math.min(1, 1 - tg.timer / tg.maxTimer));
    const urgencyPulse = 0.65 + 0.35 * Math.sin(time * 24);
    const warnCol = [255, 30, 66]; // High-contrast Crimson

    // 1. Projected Crimson Trajectory Sightline showing where enemy will lunge
    const lineLen = 220 + beatPulse * 45;
    const endX = tg.edgeX + Math.cos(tg.targetAngle) * lineLen;
    const endY = tg.edgeY + Math.sin(tg.targetAngle) * lineLen;

    const sightGrad = ctx.createLinearGradient(tg.edgeX, tg.edgeY, endX, endY);
    sightGrad.addColorStop(0, rgba(warnCol, 0.75 + 0.25 * urgencyPulse));
    sightGrad.addColorStop(0.5, rgba(warnCol, 0.35));
    sightGrad.addColorStop(1, "rgba(0,0,0,0)");

    ctx.save();
    ctx.setLineDash([8, 8]);
    ctx.beginPath();
    ctx.moveTo(tg.edgeX, tg.edgeY);
    ctx.lineTo(endX, endY);
    ctx.strokeStyle = sightGrad;
    ctx.lineWidth = 2.5;
    ctx.stroke();
    ctx.restore();

    // 2. High-Contrast Crimson Warning Diamond (◆) on screen perimeter
    const dSize = 15 + urgencyPulse * 4;
    ctx.save();
    ctx.translate(tg.edgeX, tg.edgeY);
    ctx.rotate(Math.PI / 4);
    ctx.fillStyle = rgba(warnCol, 0.95);
    ctx.fillRect(-dSize / 2, -dSize / 2, dSize, dSize);
    ctx.strokeStyle = "#FFFFFF";
    ctx.lineWidth = 1.8;
    ctx.strokeRect(-dSize / 2, -dSize / 2, dSize, dSize);
    ctx.restore();

    // 3. Countdown ring contracting onto the diamond
    ctx.beginPath();
    ctx.arc(tg.edgeX, tg.edgeY, (1 - progress) * 28 + 12, 0, Math.PI * 2);
    ctx.strokeStyle = rgba([255, 183, 3], 0.85);
    ctx.lineWidth = 2;
    ctx.stroke();
  }
}'''

old_tele_start = js.find('function drawEnemyTelegraphs(')
old_tele_end = js.find('function drawEnemySprite(', old_tele_start)
js = js[:old_tele_start] + new_telegraph_fn + '\n\n' + js[old_tele_end:]

# 2. Add telegraph management into NoMoreIllusionsApp:
# In constructor: this.telegraphs = [];
# In startStage: this.telegraphs = [];
# Replace spawnEnemy with stepAndSpawnTelegraphedEnemies:
js = js.replace(
    'this.satellites = [];\n\n    // Spawner & Chart Timers',
    'this.satellites = [];\n    this.telegraphs = [];\n\n    // Spawner & Chart Timers'
)
js = js.replace(
    'this.satellites = [];\n\n    this.spawnTimer = 0;',
    'this.satellites = [];\n    this.telegraphs = [];\n\n    this.spawnTimer = 0;'
)

# In stepPhysics:
old_spawn_enemy_block = '''// 6. Enemy Spawner
    const spawnInterval = Math.max(0.45, 1.35 - this.level * 0.06 - this.multiBandState.bass * 0.22);
    this.spawnTimer += worldDt;
    if (this.spawnTimer >= spawnInterval && this.enemies.length < 18) {
      this.spawnTimer = 0;
      this.spawnEnemy();
    }'''

new_spawn_enemy_block = '''// 6. 1:1 Telegraphed Enemy Spawner (Perimeter warning diamonds + laser sightlines!)
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

    // Initiate new telegraphs on the beat (never blindside the player!)
    const maxEnemies = Math.min(6, 3 + Math.floor(this.level / 3));
    this.spawnTimer += worldDt;
    const canSpawn = (this.enemies.length + this.telegraphs.length < maxEnemies) &&
                     !this.multiBandState.bassDropOut &&
                     (this.beatPulse > 0.40 || this.spawnTimer >= 2.0);

    if (canSpawn && this.spawnTimer >= 1.1) {
      this.spawnTimer = 0;
      this.initiateEnemyTelegraph();
    }'''

js = js.replace(old_spawn_enemy_block, new_spawn_enemy_block)

# Add initiateEnemyTelegraph method
new_tele_method = '''  initiateEnemyTelegraph() {
    const edge = Math.floor(Math.random() * 4);
    let edgeX, edgeY;
    if (edge === 0) { edgeX = 36 + Math.random() * (this.W - 72); edgeY = 24; }
    else if (edge === 1) { edgeX = 36 + Math.random() * (this.W - 72); edgeY = this.H - 24; }
    else if (edge === 2) { edgeX = 24; edgeY = 80 + Math.random() * (this.H - 160); }
    else { edgeX = this.W - 24; edgeY = 80 + Math.random() * (this.H - 160); }

    // Target angle aims toward player's current general position with slight variation
    const aimAngle = Math.atan2(this.playerY - edgeY, this.playerX - edgeX) + (Math.random() - 0.5) * 0.28;
    const roll = Math.random();
    const type = roll < 0.40 ? "PULSAR" : (roll < 0.72 ? "SEEKER" : "WANDERER");
    const speed = (155 + this.level * 12) * (type === "SEEKER" ? 1.08 : 0.95);

    this.telegraphs.push({
      edgeX,
      edgeY,
      targetAngle: aimAngle,
      speed,
      type,
      timer: 0.92, // 2-beat warning before launch
      maxTimer: 0.92
    });
  }'''

js = js.replace('spawnEnemy() {', new_tele_method + '\n\n  spawnEnemy() {')

# In renderGameCanvas():
js = js.replace(
    'drawEnemyTelegraphs(ctx, this.enemies, this.W, this.H);',
    'drawEnemyTelegraphs(ctx, this.telegraphs, this.time, this.beatPulse);'
)

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'w', encoding='utf-8') as f:
    f.write(js)

print('Updated telegraphed enemy spawning!')
