import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Let's inspect where how-to-play card is and what styles it has
print('Has screen-HOW_TO_PLAY:', 'id="screen-HOW_TO_PLAY"' in html)
