import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Add CSS for First-Time Interactive Tutorial Modal & 3-2-1 Countdown Overlay
extra_css = """
    /* First-Time Mission Briefing & How-To-Play Modal */
    #screen-HOW_TO_PLAY {
      z-index: 25;
    }
    .guide-card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid rgba(56, 189, 248, 0.25);
      padding-bottom: 10px;
    }
    .guide-title {
      font-size: 17px;
      font-weight: 900;
      letter-spacing: 1.6px;
      color: #38BDF8;
    }
    .guide-item {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      background: rgba(10, 7, 24, 0.75);
      border: 1px solid rgba(148, 163, 184, 0.22);
      border-radius: 14px;
      padding: 10px 12px;
    }
    .guide-icon-badge {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      flex-shrink: 0;
    }
    .guide-text-title {
      font-size: 12px;
      font-weight: 900;
      letter-spacing: 0.8px;
    }
    .guide-text-body {
      font-size: 11px;
      color: #CBD5E1;
      line-height: 1.45;
      margin-top: 2px;
    }

    /* 3-2-1 Audio-Reactive Stage Countdown Overlay */
    #screen-COUNTDOWN {
      position: fixed;
      inset: 0;
      z-index: 20;
      display: none;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: rgba(4, 2, 11, 0.72);
      backdrop-filter: blur(8px);
      -webkit-backdrop-filter: blur(8px);
      pointer-events: none;
    }
    .countdown-number {
      font-size: 96px;
      font-weight: 900;
      color: #F8FAFC;
      text-shadow: 0 0 32px rgba(56, 189, 248, 0.8);
      animation: countPulse 1s ease-in-out infinite;
    }
    .countdown-label {
      font-size: 13px;
      font-weight: 900;
      letter-spacing: 3px;
      color: #FACC15;
      margin-top: 6px;
    }
    @keyframes countPulse {
      0% { transform: scale(0.7); opacity: 0.4; }
      50% { transform: scale(1.15); opacity: 1; }
      100% { transform: scale(0.95); opacity: 0.85; }
    }
"""

html = html.replace('</style>', extra_css + '\n  </style>')

# Replace screen-HOW_TO_PLAY with high-impact visual mission briefing:
old_htp_start = html.find('id="screen-HOW_TO_PLAY"')
old_htp_end = html.find('<!-- ======================================================================\n       9. SETTINGS MODAL', old_htp_start)

new_htp_html = """id="screen-HOW_TO_PLAY" class="screen-overlay modal-backdrop">
    <div class="glass-modal-card" style="max-width:440px;">
      <div class="guide-card-header">
        <div>
          <div class="guide-title">MISSION BRIEFING</div>
          <div style="font-size:10px;color:#94A3B8;font-weight:700;">AUDIO-REACTIVE ALBUM SURVIVAL GUIDE</div>
        </div>
        <button class="btn-close-pill" onclick="window.app.dismissHowToPlay()">DISMISS</button>
      </div>

      <!-- 1. ORBS (WHAT TO COLLECT) -->
      <div class="guide-item" style="border-color:rgba(250,204,21,0.45);">
        <div class="guide-icon-badge" style="background:rgba(250,204,21,0.18);border:1.5px solid #FACC15;color:#FACC15;">★</div>
        <div>
          <div class="guide-text-title" style="color:#FACC15;">COLLECT BEAT ORBS (GOLDEN &amp; CYAN)</div>
          <div class="guide-text-body">
            Orbs travel smoothly across the arena on the beat with comet trails. A <b>harmonic guidance beam</b> points directly from your hero core to the next orb! Collect them when the ring locks in for <b>PERFECT! +100</b> combos.
          </div>
        </div>
      </div>

      <!-- 2. ENEMIES (WHAT TO DODGE) -->
      <div class="guide-item" style="border-color:rgba(255,30,66,0.45);">
        <div class="guide-icon-badge" style="background:rgba(255,30,66,0.18);border:1.5px solid #FF1E42;color:#FF1E42;">◆</div>
        <div>
          <div class="guide-text-title" style="color:#FF1E42;">DODGE ENEMIES (CRIMSON TELEGRAPHS)</div>
          <div class="guide-text-body">
            Enemies <b>never spawn unfairly</b>. Look for <b>Crimson Warning Diamonds (◆)</b> and laser sightlines along the border 2 beats before an enemy lunges. Dodge their articulated tail cascades!
          </div>
        </div>
      </div>

      <!-- 3. UNLOCK FULL TRACKS -->
      <div class="guide-item" style="border-color:rgba(74,222,128,0.45);">
        <div class="guide-icon-badge" style="background:rgba(74,222,128,0.18);border:1.5px solid #4ADE80;color:#4ADE80;">♪</div>
        <div>
          <div class="guide-text-title" style="color:#4ADE80;">SURVIVE TO UNLOCK THE ALBUM</div>
          <div class="guide-text-body">
            Survive until the end of <b>Confide</b> or <b>Number One</b> to unlock the 432Hz master track in your permanent Album Vault deck.
          </div>
        </div>
      </div>

      <!-- 4. CONTROLS -->
      <div class="guide-item" style="border-color:rgba(192,132,252,0.45);">
        <div class="guide-icon-badge" style="background:rgba(192,132,252,0.18);border:1.5px solid #C084FC;color:#C084FC;">⚡</div>
        <div>
          <div class="guide-text-title" style="color:#C084FC;">CONTROLS &amp; POWER-UPS</div>
          <div class="guide-text-body">
            Glide your finger or mouse anywhere to steer. Jump through <b>Wormhole Portals</b> to warp safely. Hold <b>FOCUS</b> to dilate time. Grab <b>Overdrive (crimson)</b> to vaporize enemies!
          </div>
        </div>
      </div>

      <div style="display:flex;gap:10px;margin-top:6px;">
        <button class="btn-action-primary" onclick="window.app.dismissHowToPlay(true)" style="height:46px;">
          ✓  I'M READY — START STAGE
        </button>
      </div>
    </div>
  </div>

  <!-- Countdown Overlay -->
  <div id="screen-COUNTDOWN">
    <div id="countdown-val" class="countdown-number">3</div>
    <div class="countdown-label">GET READY TO GROOVE</div>
  </div>

  """

html = html[:old_htp_start] + new_htp_html + '\n  ' + html[old_htp_end:]

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\index.html', 'w', encoding='utf-8') as f:
    f.write(html)

print('Updated index.html with First-Time Mission Briefing & 3-2-1 Countdown!')
