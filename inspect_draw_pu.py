import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Let's inspect drawPowerUps in engine.js
idx = js.find('function drawPowerUps(')
idx_end = js.find('function drawEnemyTelegraphs(', idx)
print('Current drawPowerUps in engine.js length:', idx_end - idx)
print(js[idx:idx+600])
