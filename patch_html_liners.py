import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# 1. Add Liner Notes Modal HTML
liner_notes_modal = """
  <!-- ======================================================================
       10. ARTIST LINER NOTES & PRODUCTION VAULT
       ====================================================================== -->
  <div id="screen-LINER_NOTES" class="screen-overlay modal-backdrop">
    <div class="glass-modal-card" style="max-width:440px;">
      <div class="modal-header-row">
        <div>
          <div id="liner-track-title" class="modal-title" style="color:#FACC15;font-size:18px;">CONFIDE</div>
          <div id="liner-track-meta" style="font-size:10px;color:#94A3B8;font-weight:700;">124 BPM • C MINOR • 432Hz ANALOG MASTER</div>
        </div>
        <button class="btn-close-pill" onclick="window.app.closeLinerNotes()">CLOSE</button>
      </div>

      <!-- Studio Production Story Card -->
      <div style="background:rgba(10,7,24,0.85);border:1.2px solid rgba(250,204,21,0.35);border-radius:16px;padding:12px 14px;">
        <div style="font-size:10px;font-weight:900;letter-spacing:1.4px;color:#FACC15;margin-bottom:4px;">PRODUCTION STORY &amp; INSPIRATION</div>
        <div id="liner-track-story" style="font-size:11.5px;color:#CBD5E1;line-height:1.55;">
          Written during late-night studio sessions surrounded by analog synthesizers and tape machines. 'Confide' explores the vulnerable threshold between holding up emotional armor and letting someone see what lies beneath.
        </div>
      </div>

      <!-- Lyric Excerpt Card -->
      <div style="background:rgba(10,7,24,0.85);border:1.2px solid rgba(56,189,248,0.35);border-radius:16px;padding:12px 14px;">
        <div style="font-size:10px;font-weight:900;letter-spacing:1.4px;color:#38BDF8;margin-bottom:4px;">LYRIC &amp; CADENCE BREAKDOWN</div>
        <div id="liner-track-lyrics" style="font-size:12px;color:#E0F2FE;font-style:italic;line-height:1.6;white-space:pre-line;">
          "Can I confide in you tonight?
          No more illusions to hide behind...
          Through the dark void and the frequency light,
          We break the signal and come alive."
        </div>
      </div>

      <!-- Sonic Specs Badge Row -->
      <div style="display:flex;gap:8px;flex-wrap:wrap;">
        <div style="flex:1;min-width:90px;background:rgba(15,23,42,0.8);border:1px solid rgba(148,163,184,0.25);border-radius:10px;padding:6px;text-align:center;">
          <div style="font-size:8px;font-weight:800;color:#94A3B8;">SYNTH ENGINES</div>
          <div style="font-size:10.5px;font-weight:900;color:#38BDF8;">ANALOG JUNO + MOOG</div>
        </div>
        <div style="flex:1;min-width:90px;background:rgba(15,23,42,0.8);border:1px solid rgba(148,163,184,0.25);border-radius:10px;padding:6px;text-align:center;">
          <div style="font-size:8px;font-weight:800;color:#94A3B8;">MASTERING</div>
          <div style="font-size:10.5px;font-weight:900;color:#FACC15;">-14 LUFS EBU R128</div>
        </div>
        <div style="flex:1;min-width:90px;background:rgba(15,23,42,0.8);border:1px solid rgba(148,163,184,0.25);border-radius:10px;padding:6px;text-align:center;">
          <div style="font-size:8px;font-weight:800;color:#94A3B8;">VOCAL CADENCE</div>
          <div style="font-size:10.5px;font-weight:900;color:#4ADE80;">TRUE 432Hz RESONANCE</div>
        </div>
      </div>

      <button class="btn-action-primary" onclick="window.app.closeLinerNotes()">RETURN TO VAULT</button>
    </div>
  </div>
"""

# Insert liner_notes_modal before <script src="multi_band_data.js">
html = html.replace('<script src="multi_band_data.js">', liner_notes_modal + '\n  <script src="multi_band_data.js">')

# 2. Add "📖 ARTIST LINER NOTES" button in Studio Customizer & Album Vault deck
old_vault_header = """<div style="display:flex;align-items:center;justify-content:space-between;margin-top:2px;">
        <span style="font-size:10px;font-weight:900;letter-spacing:1.4px;color:#FACC15;">ALBUM TRACK VAULT</span>
        <span style="font-size:10px;font-weight:900;color:#38BDF8;cursor:pointer;" onclick="window.app.openScreen('DEMO_PLAYER')">OPEN STUDIO DECK →</span>
      </div>"""

new_vault_header = """<div style="display:flex;align-items:center;justify-content:space-between;margin-top:2px;">
        <span style="font-size:10px;font-weight:900;letter-spacing:1.4px;color:#FACC15;">ALBUM TRACK VAULT</span>
        <div style="display:flex;gap:10px;">
          <span style="font-size:10px;font-weight:900;color:#FACC15;cursor:pointer;" onclick="window.app.openLinerNotes()">📖 LINER NOTES</span>
          <span style="font-size:10px;font-weight:900;color:#38BDF8;cursor:pointer;" onclick="window.app.openScreen('DEMO_PLAYER')">STUDIO DECK →</span>
        </div>
      </div>"""

html = html.replace(old_vault_header, new_vault_header)

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\index.html', 'w', encoding='utf-8') as f:
    f.write(html)

print('Updated index.html with Artist Liner Notes modal and navigation!')
