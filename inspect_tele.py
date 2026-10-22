import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\app\src\main\java\com\oneshelfstudio\nomoreillusions\GamePhysics.kt', 'r', encoding='utf-8') as f:
    gp = f.read()

# Let's search for telegraph in GamePhysics.kt
idx = gp.find('telegraph')
if idx != -1:
    print('Found telegraph in GamePhysics.kt:')
    print(gp[idx-100:idx+600])
else:
    print('telegraph not found directly')
