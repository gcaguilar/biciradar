#!/usr/bin/env python3
"""
Genera iconos PNG para BiciRadar Garmin desde un SVG base.

Uso:
    python3 generate_icons.py

Requiere:
    pip install cairosvg
"""
import cairosvg
import os

SIZES = [30, 40, 50, 60, 70, 90, 100, 110, 140, 150, 180, 210, 220]

SVG_TEMPLATE = '''<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 100 100">
  <rect width="100" height="100" fill="#1976D2" rx="15"/>
  <circle cx="50" cy="35" r="20" fill="white"/>
  <rect x="30" y="55" width="40" height="8" fill="white" rx="4"/>
  <circle cx="35" cy="75" r="12" fill="none" stroke="white" stroke-width="4"/>
  <circle cx="65" cy="75" r="12" fill="none" stroke="white" stroke-width="4"/>
  <rect x="20" y="33" width="15" height="5" fill="white" rx="2"/>
  <rect x="65" y="33" width="15" height="5" fill="white" rx="2"/>
</svg>'''

def generate_icons():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    drawables_dir = os.path.join(base_dir, 'resources', 'drawables')

    for size in SIZES:
        size_dir = os.path.join(drawables_dir, f'{size}x{size}')
        os.makedirs(size_dir, exist_ok=True)

        svg_content = SVG_TEMPLATE.format(size=size)
        output_path = os.path.join(size_dir, 'ic_launcher.png')

        cairosvg.svg2png(
            bytestring=svg_content.encode('utf-8'),
            write_to=output_path,
            output_width=size,
            output_height=size
        )
        print(f'Generated: {output_path}')

    # Also copy largest to default location
    default_dir = os.path.join(drawables_dir, '100x100')
    default_path = os.path.join(default_dir, 'ic_launcher.png')
    os.makedirs(default_dir, exist_ok=True)

    svg_content = SVG_TEMPLATE.format(size=100)
    cairosvg.svg2png(
        bytestring=svg_content.encode('utf-8'),
        write_to=default_path,
        output_width=100,
        output_height=100
    )
    print(f'Default: {default_path}')

if __name__ == '__main__':
    generate_icons()
