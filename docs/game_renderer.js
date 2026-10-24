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

      const orbCount = 8;
      for (let rIdx = 0; rIdx < orbCount; rIdx++) {
        const angle = (rIdx * (360 / orbCount) + time * 42) * Math.PI / 180;
        const orbDist = op.radius * (1.45 + 0.35 * Math.sin(time * 5 + rIdx) + vocalEnvelope * 0.35);
        ctx.beginPath();
        ctx.arc(op.x + Math.cos(angle) * orbDist, op.y + Math.sin(angle) * orbDist, rIdx % 2 === 0 ? 4.2 : 2.8, 0, Math.PI * 2);
        ctx.fillStyle = rgba([253, 224, 71], 0.82);
        ctx.fill();
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

    // 3. Smooth Crimson/Amber Radial Warning Beacon Orb on screen perimeter (No sharp square/diamond lines!)
    const bSize = 11 + urgencyPulse * 4;
    ctx.beginPath();
    ctx.arc(tg.edgeX, tg.edgeY, bSize * 0.85, 0, Math.PI * 2);
    ctx.fillStyle = rgba(warnCol, 0.92);
    ctx.fill();
    ctx.strokeStyle = "#FFFFFF";
    ctx.lineWidth = 1.8;
    ctx.stroke();

    // 4. Countdown ring contracting onto the beacon
    ctx.beginPath();
    ctx.arc(tg.edgeX, tg.edgeY, (1 - progress) * 26 + 11, 0, Math.PI * 2);
    ctx.strokeStyle = rgba([255, 183, 3], 0.88);
    ctx.lineWidth = 2.2;
    ctx.stroke();
  }
}

function drawEnemySprite(ctx, e, enemyBitmaps, time, beatPulse) {
  // Smooth Curved Energy Crescents & Orbital Threat Rings (Zero jagged polygon wireframes!)
  const speed = Math.max(1, Math.hypot(e.vx, e.vy));
  const dirX = e.vx / speed;
  const dirY = e.vy / speed;
  const heading = Math.atan2(dirY, dirX);
  const r = e.radius;

  // Shared crimson danger halo
  const halo = ctx.createRadialGradient(e.x, e.y, 2, e.x, e.y, r * 2.1);
  halo.addColorStop(0, "rgba(255,30,66,0.48)");
  halo.addColorStop(0.55, "rgba(136,19,55,0.22)");
  halo.addColorStop(1, "rgba(0,0,0,0)");
  ctx.beginPath();
  ctx.arc(e.x, e.y, r * 2.1, 0, Math.PI * 2);
  ctx.fillStyle = halo;
  ctx.fill();

  // Dark spherical plasma core body
  ctx.beginPath();
  ctx.arc(e.x, e.y, r * 0.94, 0, Math.PI * 2);
  ctx.fillStyle = "#19040C";
  ctx.fill();

  if (e.type === "PULSAR") {
    for (let b = 0; b < 4; b++) {
      const startA = -time * 3.8 + b * (Math.PI / 2);
      ctx.beginPath();
      ctx.arc(e.x, e.y, r * (1.14 + 0.12 * beatPulse), startA, startA + 0.95);
      ctx.strokeStyle = "#FB7185";
      ctx.lineWidth = 3.0;
      ctx.lineCap = "round";
      ctx.stroke();
    }
  } else if (e.type === "SEEKER") {
    ctx.beginPath();
    ctx.arc(e.x, e.y, r * 1.16, heading - 1.25, heading + 1.25);
    ctx.strokeStyle = "#FF1E42";
    ctx.lineWidth = 3.4;
    ctx.lineCap = "round";
    ctx.stroke();
    const blink = 0.55 + 0.45 * beatPulse;
    for (const s of [-2.15, 2.15]) {
      ctx.beginPath();
      ctx.arc(e.x + Math.cos(heading + s) * r * 1.16, e.y + Math.sin(heading + s) * r * 1.16, r * 0.16, 0, Math.PI * 2);
      ctx.fillStyle = `rgba(251,191,36,${blink})`;
      ctx.fill();
    }
  } else {
    for (let ring = 0; ring < 2; ring++) {
      const rRad = r * (ring === 0 ? 1.08 : 0.82);
      const spin = (ring === 0 ? 1 : -1) * time * 1.9;
      for (let seg = 0; seg < 2; seg++) {
        const sa = spin + seg * Math.PI;
        ctx.beginPath();
        ctx.arc(e.x, e.y, rRad, sa, sa + 1.85);
        ctx.strokeStyle = ring === 0 ? "#FB7185" : "#C084FC";
        ctx.lineWidth = 2.4;
        ctx.lineCap = "round";
        ctx.stroke();
      }
    }
    for (let n = 0; n < 3; n++) {
      const a = time * 2.6 + n * (Math.PI * 2 / 3) + (e.phase || 0);
      ctx.beginPath();
      ctx.arc(e.x + Math.cos(a) * r * 1.38, e.y + Math.sin(a) * r * 1.38, r * 0.16, 0, Math.PI * 2);
      ctx.fillStyle = "#F43F5E";
      ctx.fill();
    }
  }

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

  // Smooth Rotating Harmonic Crescent Waves + Orbiting Gold Pearls around the Hero (Replaces 12 straight stick lines!)
  const haloArcR = R * (1.34 + 0.14 * beatPulse);
  for (let arcIdx = 0; arcIdx < 3; arcIdx++) {
    const startA = time * 1.65 + arcIdx * ((Math.PI * 2) / 3);
    ctx.beginPath();
    ctx.arc(playerX, playerY, haloArcR, startA, startA + 1.18 + beatPulse * 0.25);
    ctx.strokeStyle = rgba(arcIdx % 2 === 0 ? [250, 204, 21] : skinCol, 0.85);
    ctx.lineWidth = 2.8;
    ctx.lineCap = "round";
    ctx.stroke();
  }
  for (let i = 0; i < 6; i++) {
    const a = i * (Math.PI / 3) - time * 1.5;
    const orbR = R * (1.52 + (i % 2 === 0 ? 0.18 * beatPulse : 0.06));
    ctx.beginPath();
    ctx.arc(playerX + Math.cos(a) * orbR, playerY + Math.sin(a) * orbR, i % 2 === 0 ? 3.0 : 2.2, 0, Math.PI * 2);
    ctx.fillStyle = rgba(i % 2 === 0 ? [250, 204, 21] : skinCol, 0.92);
    ctx.fill();
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
  const cx = w * 0.5;
  const cy = h * 0.5;
  const maxR = Math.hypot(cx, cy);
  const irisR = Math.max(2, maxR * clampedProgress);

  // 1. Dimmed locked silhouette underneath + soft concentric vinyl rings (Zero boxy grid lines!)
  ctx.fillStyle = "rgba(6, 8, 18, 0.92)";
  ctx.fillRect(0, 0, w, h);
  if (logoImg && logoImg.complete && logoImg.naturalWidth > 0) {
    ctx.save();
    ctx.globalAlpha = 0.22;
    ctx.drawImage(logoImg, 0, 0, w, h);
    ctx.restore();
  }
  for (let g = 1; g <= 3; g++) {
    ctx.beginPath();
    ctx.arc(cx, cy, Math.min(w, h) * 0.15 * g, 0, Math.PI * 2);
    ctx.strokeStyle = "rgba(56, 189, 248, 0.14)";
    ctx.lineWidth = 1.0;
    ctx.stroke();
  }

  // 2. Smooth Radial Iris Reveal expanding from center
  if (clampedProgress > 0.01 && logoImg && logoImg.complete && logoImg.naturalWidth > 0) {
    ctx.save();
    ctx.beginPath();
    ctx.arc(cx, cy, irisR, 0, Math.PI * 2);
    ctx.clip();
    ctx.globalAlpha = 0.98;
    ctx.drawImage(logoImg, 0, 0, w, h);
    ctx.restore();

    if (clampedProgress < 0.99) {
      ctx.beginPath();
      ctx.arc(cx, cy, irisR, 0, Math.PI * 2);
      ctx.strokeStyle = rgba([74, 222, 128], 0.78 + beatPulse * 0.2);
      ctx.lineWidth = 2.0;
      ctx.stroke();
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

  // 1. Smooth Concentric Harmonic Wave Rings & Orbiting Aurora Crescents (Replaces 36 straight stick lines!)
  for (let rIdx = 0; rIdx < 3; rIdx++) {
    const waveR = vinylRadius * (1.05 + rIdx * 0.075 + pulse * 0.025);
    const baseSpin = time * (rIdx % 2 === 0 ? 0.85 : -0.65) + rIdx * 1.1;
    const ringCol = isGoldMode
      ? [250, 204, 21]
      : (rIdx === 0 ? [56, 189, 248] : rIdx === 1 ? [168, 85, 247] : [250, 204, 21]);
    for (let seg = 0; seg < 3; seg++) {
      const sa = baseSpin + seg * ((Math.PI * 2) / 3);
      ctx.beginPath();
      ctx.arc(cx, cy, waveR, sa, sa + 1.35);
      ctx.strokeStyle = rgba(ringCol, 0.68 - rIdx * 0.16);
      ctx.lineWidth = 3.0 - rIdx * 0.6;
      ctx.lineCap = "round";
      ctx.stroke();
    }
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

