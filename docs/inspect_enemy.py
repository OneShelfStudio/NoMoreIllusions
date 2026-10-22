import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'r', encoding='utf-8') as f:
    orig = f.read()

# Let's inspect stepPhysics and how telegraphs are created/updated
idx = orig.find('spawnEnemy()')
print('spawnEnemy in engine.js:')
print(orig[idx:idx+900])
