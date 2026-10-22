import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Let's inspect initSfxPool in engine.js
idx = js.find('initSfxPool() {')
idx_end = js.find('unlockAudioOnFirstGesture()', idx)
print('Current initSfxPool:')
print(js[idx:idx_end])
