// ============================================================================
// NO MORE ILLUSIONS — HERO, ENEMY, TELEGRAPH & TURNTABLE RENDERER
// ============================================================================

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
  // 1:1 with Android GameRenderer.kt — ZERO tails. Crisp geometric hazards only.
  const speed = Math.max(1, Math.hypot(e.vx, e.vy));
  const dirX = e.vx / speed;
  const dirY = e.vy / speed;
  const heading = Math.atan2(dirY, dirX);
  const r = e.radius;

  // Shared crimson danger halo
  const halo = ctx.createRadialGradient(e.x, e.y, 2, e.x, e.y, r * 2.0);
  halo.addColorStop(0, "rgba(255,30,66,0.42)");
  halo.addColorStop(1, "rgba(0,0,0,0)");
  ctx.beginPath();
  ctx.arc(e.x, e.y, r * 2.0, 0, Math.PI * 2);
  ctx.fillStyle = halo;
  ctx.fill();

  ctx.save();
  ctx.translate(e.x, e.y);

  if (e.type === "PULSAR") {
    // CRIMSON SAWBLADE: 8-point spinning cybernetic buzzsaw
    ctx.rotate(time * 4.2 + (e.phase || 0));
    const teeth = 8;
    ctx.beginPath();
    for (let i = 0; i < teeth * 2; i++) {
      const a = i * (Math.PI / teeth);
      const rr = i % 2 === 0 ? r * (1.32 + beatPulse * 0.12) : r * 0.86;
      const a2 = a + (i % 2 === 0 ? 0.12 : 0);
      if (i === 0) ctx.moveTo(Math.cos(a2) * rr, Math.sin(a2) * rr);
      else ctx.lineTo(Math.cos(a2) * rr, Math.sin(a2) * rr);
    }
    ctx.closePath();
    ctx.fillStyle = "#7F1D1D";
    ctx.fill();
    ctx.strokeStyle = "#FB7185";
    ctx.lineWidth = 2.2;
    ctx.stroke();
    ctx.beginPath();
    ctx.arc(0, 0, r * 0.62, 0, Math.PI * 2);
    ctx.fillStyle = "#1A0508";
    ctx.fill();
    ctx.strokeStyle = "#F43F5E";
    ctx.lineWidth = 1.6;
    ctx.stroke();
    for (let b = 0; b < 4; b++) {
      const a = b * (Math.PI / 2);
      ctx.beginPath();
      ctx.arc(Math.cos(a) * r * 0.38, Math.sin(a) * r * 0.38, r * 0.09, 0, Math.PI * 2);
      ctx.fillStyle = "#FDA4AF";
      ctx.fill();
    }
  } else if (e.type === "SEEKER") {
    // DELTA INTERCEPTOR: angular twin-wing stealth fighter with amber wingtip beacons
    ctx.rotate(heading);
    ctx.beginPath();
    ctx.moveTo(r * 1.45, 0);
    ctx.lineTo(-r * 0.85, r * 1.15);
    ctx.lineTo(-r * 0.45, 0);
    ctx.lineTo(-r * 0.85, -r * 1.15);
    ctx.closePath();
    ctx.fillStyle = "#3F0A12";
    ctx.fill();
    ctx.strokeStyle = "#FF1E42";
    ctx.lineWidth = 2.2;
    ctx.stroke();
    // Center spine
    ctx.beginPath();
    ctx.moveTo(r * 1.2, 0);
    ctx.lineTo(-r * 0.35, 0);
    ctx.strokeStyle = "rgba(254,205,211,0.85)";
    ctx.lineWidth = 1.4;
    ctx.stroke();
    // Cockpit
    ctx.beginPath();
    ctx.ellipse(r * 0.35, 0, r * 0.32, r * 0.18, 0, 0, Math.PI * 2);
    ctx.fillStyle = "#FF1E42";
    ctx.fill();
    // Amber wingtip beacons (blink on beat)
    const blink = 0.55 + 0.45 * beatPulse;
    for (const s of [-1, 1]) {
      ctx.beginPath();
      ctx.arc(-r * 0.8, s * r * 1.08, r * 0.14, 0, Math.PI * 2);
      ctx.fillStyle = `rgba(251,191,36,${blink})`;
      ctx.fill();
    }
  } else {
    // VOID OCTAHEDRON: counter-rotating double-square prism + 3 orbiting hazard nodes
    for (let layer = 0; layer < 2; layer++) {
      ctx.save();
      ctx.rotate((layer === 0 ? 1 : -1) * time * 1.6 + layer * Math.PI / 4);
      const s = r * (layer === 0 ? 1.05 : 0.82);
      ctx.beginPath();
      ctx.rect(-s, -s, s * 2, s * 2);
      ctx.fillStyle = layer === 0 ? "rgba(76,5,25,0.92)" : "rgba(136,19,55,0.85)";
      ctx.fill();
      ctx.strokeStyle = layer === 0 ? "#FB7185" : "#FDA4AF";
      ctx.lineWidth = 1.8;
      ctx.stroke();
      ctx.restore();
    }
    for (let n = 0; n < 3; n++) {
      const a = time * 2.4 + n * (Math.PI * 2 / 3) + (e.phase || 0);
      ctx.beginPath();
      ctx.arc(Math.cos(a) * r * 1.55, Math.sin(a) * r * 1.55, r * 0.17, 0, Math.PI * 2);
      ctx.fillStyle = "#F43F5E";
      ctx.fill();
      ctx.strokeStyle = "#FFFFFF";
      ctx.lineWidth = 1;
      ctx.stroke();
    }
  }
  ctx.restore();

  // Glowing sentient core eye locked on direction of travel
  const eyeX = e.x + dirX * (r * 0.18);
  const eyeY = e.y + dirY * (r * 0.18);
  ctx.beginPath();
  ctx.arc(eyeX, eyeY, r * 0.24, 0, Math.PI * 2);
  ctx.fillStyle = "#FFFFFF";
  ctx.fill();
  ctx.beginPath();
  ctx.arc(eyeX + dirX * 2, eyeY + dirY * 2, r * 0.12, 0, Math.PI * 2);
  ctx.fillStyle = "#FF1E42";
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
  let skinCol;
  switch (selectedSkinId) {
    case "skin_gold": skinCol = [250, 204, 21]; break;
    case "skin_prism": skinCol = [236, 72, 153]; break;
    case "skin_emerald": skinCol = [16, 185, 129]; break;
    case "skin_blood": skinCol = [244, 63, 94]; break;
    default: skinCol = [56, 189, 248];
  }
  const R = playerRadius;

  if (shieldTimer > 0) {
    ctx.beginPath();
    ctx.arc(playerX, playerY, R * 1.95 + 4 * Math.sin(time * 8), 0, Math.PI * 2);
    ctx.strokeStyle = rgba([56, 189, 248], 0.85);
    ctx.lineWidth = 3.5;
    ctx.stroke();
    ctx.beginPath();
    ctx.arc(playerX, playerY, R * 1.95, 0, Math.PI * 2);
    ctx.fillStyle = "rgba(56,189,248,0.10)";
    ctx.fill();
  }
  if (overdriveTimer > 0) {
    ctx.beginPath();
    ctx.arc(playerX, playerY, R * 2.25 + 7 * Math.sin(time * 14), 0, Math.PI * 2);
    ctx.strokeStyle = rgba([249, 115, 22], 0.9);
    ctx.lineWidth = 5;
    ctx.stroke();
  }
  if (magnetTimer > 0) {
    ctx.beginPath();
    ctx.arc(playerX, playerY, R * 2.7 + 6 * Math.cos(time * 10), 0, Math.PI * 2);
    ctx.strokeStyle = rgba([16, 185, 129], 0.55);
    ctx.lineWidth = 2.5;
    ctx.setLineDash([6, 6]);
    ctx.stroke();
    ctx.setLineDash([]);
  }
  if (isFocusHolding && focusCharge > 0.02) {
    ctx.beginPath();
    ctx.arc(playerX, playerY, R * 2.4, -Math.PI / 2, -Math.PI / 2 + focusCharge * Math.PI * 2);
    ctx.strokeStyle = rgba([168, 85, 247], 0.9);
    ctx.lineWidth = 4;
    ctx.lineCap = "round";
    ctx.stroke();
  }

  const pScale = 1 + beatPulse * 0.12;

  // Soft skin halo
  const halo = ctx.createRadialGradient(playerX, playerY, 2, playerX, playerY, R * 2.4 * pScale);
  halo.addColorStop(0, rgba(skinCol, 0.5));
  halo.addColorStop(1, "rgba(0,0,0,0)");
  ctx.beginPath();
  ctx.arc(playerX, playerY, R * 2.4 * pScale, 0, Math.PI * 2);
  ctx.fillStyle = halo;
  ctx.fill();

  // 12-Bar Radial Turntable Equalizer Crown
  for (let i = 0; i < 12; i++) {
    const a = i * (Math.PI / 6) + time * 0.8;
    const wave = 0.35 + 0.65 * Math.abs(Math.sin(time * 6 + i * 0.9)) * (0.6 + beatPulse * 0.4);
    const r0 = R * 1.12;
    const r1 = r0 + R * (0.18 + wave * 0.42);
    ctx.beginPath();
    ctx.moveTo(playerX + Math.cos(a) * r0, playerY + Math.sin(a) * r0);
    ctx.lineTo(playerX + Math.cos(a) * r1, playerY + Math.sin(a) * r1);
    ctx.strokeStyle = rgba(i % 2 === 0 ? skinCol : [250, 204, 21], 0.92);
    ctx.lineWidth = 3;
    ctx.lineCap = "round";
    ctx.stroke();
  }

  // Obsidian-and-Gold Vinyl Platter
  ctx.beginPath();
  ctx.arc(playerX, playerY, R * pScale, 0, Math.PI * 2);
  ctx.fillStyle = "#07060F";
  ctx.fill();
  ctx.strokeStyle = "#FACC15";
  ctx.lineWidth = 2.2;
  ctx.stroke();
  for (let g = 1; g <= 3; g++) {
    ctx.beginPath();
    ctx.arc(playerX, playerY, R * pScale * (0.45 + g * 0.15), 0, Math.PI * 2);
    ctx.strokeStyle = "rgba(255,255,255,0.12)";
    ctx.lineWidth = 1;
    ctx.stroke();
  }
  // Spinning holographic sheen
  const spin = time * 3.2;
  ctx.beginPath();
  ctx.arc(playerX, playerY, R * pScale * 0.78, spin, spin + 0.9);
  ctx.strokeStyle = rgba(skinCol, 0.75);
  ctx.lineWidth = 3;
  ctx.lineCap = "round";
  ctx.stroke();
  ctx.beginPath();
  ctx.arc(playerX, playerY, R * pScale * 0.78, spin + Math.PI, spin + Math.PI + 0.9);
  ctx.stroke();

  // Mandala core label (skin bitmap) clipped to center
  const labelR = R * pScale * 0.48;
  if (playerBitmap && playerBitmap.complete && playerBitmap.naturalWidth > 0) {
    ctx.save();
    ctx.beginPath();
    ctx.arc(playerX, playerY, labelR, 0, Math.PI * 2);
    ctx.clip();
    ctx.translate(playerX, playerY);
    ctx.rotate(time * 1.4);
    ctx.drawImage(playerBitmap, -labelR * 1.25, -labelR * 1.25, labelR * 2.5, labelR * 2.5);
    ctx.restore();
  } else {
    ctx.beginPath();
    ctx.arc(playerX, playerY, labelR, 0, Math.PI * 2);
    ctx.fillStyle = rgba(skinCol, 0.95);
    ctx.fill();
  }
  ctx.beginPath();
  ctx.arc(playerX, playerY, labelR, 0, Math.PI * 2);
  ctx.strokeStyle = rgba(skinCol, 1);
  ctx.lineWidth = 1.6;
  ctx.stroke();
  // Spindle
  ctx.beginPath();
  ctx.arc(playerX, playerY, Math.max(2.5, R * 0.12), 0, Math.PI * 2);
  ctx.fillStyle = "#FFFFFF";
  ctx.fill();
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

