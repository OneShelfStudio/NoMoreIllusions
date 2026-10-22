import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'r', encoding='utf-8') as f:
    js = f.read()

# 1. In prefs, add seenHowToPlay:
# seenHowToPlay: localStorage.getItem("nmi_seen_htp") === "true",
js = js.replace(
    'sfxMuted: localStorage.getItem("nmi_sfx_muted") === "true"',
    'sfxMuted: localStorage.getItem("nmi_sfx_muted") === "true",\n      seenHowToPlay: localStorage.getItem("nmi_seen_htp") === "true"'
)

# 2. Add dismissHowToPlay and launchCountdown methods:
new_flow_methods = '''  dismissHowToPlay(startImmediately = false) {
    this.playSfx("click");
    this.prefs.seenHowToPlay = true;
    localStorage.setItem("nmi_seen_htp", "true");
    if (startImmediately) {
      this.launchStageCountdown();
    } else {
      this.openScreen("MENU");
    }
  }

  startStage(stageIndex = this.selectedStageIndex) {
    this.unlockAudioOnFirstGesture();
    this.playSfx("click");
    this.selectedStageIndex = stageIndex;

    // Check if player has seen the How-To-Play guide
    if (!this.prefs.seenHowToPlay) {
      this.openScreen("HOW_TO_PLAY");
      return;
    }

    this.launchStageCountdown();
  }

  launchStageCountdown() {
    this.gameState = "COUNTDOWN";
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
        if (countValEl) countValEl.textContent = "GO!";
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
    this.trackAudio.play().catch(() => {});

    this.gameState = "PLAYING";
    this.syncUI();
  }'''

# Replace startStage definition
old_start_stage = js.find('startStage(stageIndex = this.selectedStageIndex) {')
old_start_stage_end = js.find('pauseGame()', old_start_stage)

js = js[:old_start_stage] + new_flow_methods + '\n\n  ' + js[old_start_stage_end:]

# Update screens list in syncUI to include COUNTDOWN if needed:
js = js.replace(
    'const screens = ["MENU", "PLAYING", "PAUSED", "GAME_OVER", "TRACK_COMPLETED", "HOW_TO_PLAY", "SETTINGS", "DEMO_PLAYER", "CUSTOMIZER"];',
    'const screens = ["MENU", "PLAYING", "PAUSED", "GAME_OVER", "TRACK_COMPLETED", "HOW_TO_PLAY", "SETTINGS", "DEMO_PLAYER", "CUSTOMIZER", "COUNTDOWN"];'
)

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'w', encoding='utf-8') as f:
    f.write(js)

print('Updated engine.js with first-time tutorial flow & 3-2-1 countdown!')
