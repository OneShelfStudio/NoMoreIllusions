import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

# Read engine.js
with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\engine.js', 'r', encoding='utf-8') as f:
    engine_js = f.read()

# Read index.html
with open(r'C:\Users\ryanb\OneDrive\Documents\One Shelf Studio\App Projects\No More Ilusions\docs\index.html', 'r', encoding='utf-8') as f:
    index_html = f.read()

print('Original engine_js length:', len(engine_js))
print('Original index_html length:', len(index_html))
