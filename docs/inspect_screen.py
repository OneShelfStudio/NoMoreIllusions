import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\app\src\main\java\com\oneshelfstudio\nomoreillusions\GameScreen.kt', 'r', encoding='utf-8') as f:
    gs = f.read()

# Let's inspect how first-time tutorial or how to play is shown in Android GameScreen.kt
print('Search tutorial/countdown/how to play in GameScreen.kt:')
for line in gs.split('\n'):
    if 'countdown' in line.lower() or 'tutorial' in line.lower() or 'howtoplay' in line.lower() or 'guide' in line.lower() or 'instruction' in line.lower():
        print(line)
