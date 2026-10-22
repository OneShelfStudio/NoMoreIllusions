import sys

sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Let's inspect where how to play and countdown overlay can be added
print('Length of index.html:', len(html))
