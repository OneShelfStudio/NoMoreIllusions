import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\app\src\main\java\com\oneshelfstudio\nomoreillusions\GameScreen.kt', 'r', encoding='utf-8') as f:
    gs = f.read()

idx = gs.find('onHowToPlay')
print(gs[idx-400:idx+600])
