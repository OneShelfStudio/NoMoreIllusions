import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\app\src\main\java\com\oneshelfstudio\nomoreillusions\GameScreen.kt', 'r', encoding='utf-8') as f:
    gs = f.read()

idx = gs.find('TouchControlHintBanner')
print(gs[idx-200:idx+600])
