#!/usr/bin/env python3
import os
from PIL import Image, ImageDraw

SRC = os.path.join(os.path.dirname(__file__), "..", "icon.png")
RES = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")
ADAPTIVE_SIZE = 1080

img = Image.open(SRC).convert("RGB")

def center_crop_square(im, size):
    w, h = im.size
    s = min(w, h)
    left = (w - s) // 2
    top = (h - s) // 2
    return im.crop((left, top, left + s, top + s))

def rounded_mask(size):
    mask = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(mask)
    d.ellipse((0, 0, size, size), fill=255)
    return mask

square = center_crop_square(img, ADAPTIVE_SIZE)

densities = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

for density, px in densities.items():
    d = os.path.join(RES, "mipmap-" + density)
    os.makedirs(d, exist_ok=True)
    resized = square.resize((px, px), Image.LANCZOS)
    resized.save(os.path.join(d, "ic_launcher.png"))

    rounded = resized.copy()
    rounded.putalpha(rounded_mask(px))
    bg = Image.new("RGB", (px, px), (0, 0, 0))
    bg.paste(rounded, (0, 0), rounded)
    bg.save(os.path.join(d, "ic_launcher_round.png"))

drawable = os.path.join(RES, "drawable")
os.makedirs(drawable, exist_ok=True)
square.save(os.path.join(drawable, "ic_launcher_adaptive.png"))

print("icons generated")