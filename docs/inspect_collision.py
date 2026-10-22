import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Let's inspect where enemy collision check is in stepPhysics
idx = js.find('// Collision with Player')
print('Enemy collision in stepPhysics:')
print(js[idx:idx+1200])
