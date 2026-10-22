import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\app\src\main\java\com\oneshelfstudio\nomoreillusions\ArcadeComponents.kt', 'r', encoding='utf-8') as f:
    ac = f.read()

idx = ac.find('TouchControlHintBanner')
print(ac[idx:idx+1500])
