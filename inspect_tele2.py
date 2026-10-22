import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\app\src\main\java\com\oneshelfstudio\nomoreillusions\GamePhysics.kt', 'r', encoding='utf-8') as f:
    gp = f.read()

idx = gp.find('fun stepAndSpawnTelegraphedEnemies')
print(gp[idx:idx+2500])
