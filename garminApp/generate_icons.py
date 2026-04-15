#!/usr/bin/env python3
"""
Generate Garmin Connect IQ launcher icons from the Android launcher icon.

Usage:
    python3 generate_icons.py

Requirements:
    pip install pillow
"""

from pathlib import Path

from PIL import Image


SIZES = [30, 40, 50, 60, 70, 90, 100, 110, 140, 150, 180, 210, 220]
ROOT = Path(__file__).resolve().parent
SOURCE_ICON = ROOT.parent / "androidApp" / "src" / "main" / "res" / "mipmap-xxxhdpi" / "ic_launcher.webp"
DRAWABLES_DIR = ROOT / "resources" / "drawables"


def generate_icons() -> None:
    if not SOURCE_ICON.exists():
        raise FileNotFoundError(f"Android launcher icon not found: {SOURCE_ICON}")

    with Image.open(SOURCE_ICON) as image:
        source = image.convert("RGBA")

        for size in SIZES:
            size_dir = DRAWABLES_DIR / f"{size}x{size}"
            size_dir.mkdir(parents=True, exist_ok=True)

            resized = source.resize((size, size), Image.Resampling.LANCZOS)
            output_path = size_dir / "ic_launcher.png"
            resized.save(output_path, format="PNG")
            print(f"Generated {output_path.relative_to(ROOT)}")


if __name__ == "__main__":
    generate_icons()
