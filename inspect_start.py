import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'r', encoding='utf-8') as f:
    orig = f.read()

# Let's inspect how startStage is structured
idx = orig.find('startStage(stageIndex = this.selectedStageIndex) {')
idx_end = orig.find('pauseGame()', idx)
print('startStage code:')
print(orig[idx:idx+800])
