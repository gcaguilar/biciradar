#!/usr/bin/env python3
"""Rebuild the Android adaptive-icon monochrome layer.

Why this script exists
----------------------
The Android Studio Image Asset wizard generated
``androidApp/src/main/res/mipmap-*/ic_launcher_monochrome.webp`` from a full
app-icon screenshot, so the monochrome layer ended up being an opaque
background plate with the logo painted on top of it.

Android uses only the alpha channel of the monochrome layer as a mask and
recolours it with the Material You theme colour. Because the whole plate was
opaque, themed icons rendered as a solid block of the theme colour that
covered the entire icon.

This script derives a correct monochrome layer from each density's
``ic_launcher_foreground.webp``: the foreground logo is kept, the white plate
background becomes transparent, and the plate's rounded-corner drop shadow is
clipped away.

Requirements
------------
* macOS (uses ``sips`` to decode WebP)
* ``cwebp`` from the ``webp`` Homebrew formula (``brew install webp``)
* Python 3

Usage
-----
    python3 tooling/project/generate_launcher_monochrome.py
"""

from __future__ import annotations

import argparse
import shutil
import struct
import subprocess
import sys
import tempfile
import zlib
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
ANDROID_RES = REPO_ROOT / "androidApp" / "src" / "main" / "res"

# (resource directory, foreground pixels) for each launcher density.
DENSITIES = [
    ("mipmap-mdpi", 108),
    ("mipmap-hdpi", 162),
    ("mipmap-xhdpi", 216),
    ("mipmap-xxhdpi", 324),
    ("mipmap-xxxhdpi", 432),
]

# A pixel is treated as logo (opaque) once its largest channel deviation from
# white passes THRESHOLD, ramping up to fully opaque at THRESHOLD + SOFT.
# The soft ramp keeps the anti-aliased stroke edges smooth.
THRESHOLD = 40
SOFT = 90

# Radius, as a fraction of the foreground plate size, of the corner regions
# whose drop shadow is discarded.
CORNER_RADIUS_FRACTION = 0.10

# The colour of the monochrome drawable is ignored by Android, which only uses
# its alpha channel as a mask. A solid black keeps the asset readable if it is
# ever previewed without a tint.
MASK_COLOR = (0, 0, 0)


def read_png(path: Path) -> tuple[int, int, int, bytes]:
    """Decode an 8-bit truecolour PNG (with or without alpha)."""
    data = path.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"{path} is not a PNG file")

    pos = 8
    idat = bytearray()
    width = height = channels = None
    while pos < len(data):
        length = struct.unpack(">I", data[pos : pos + 4])[0]
        chunk_type = data[pos + 4 : pos + 8]
        chunk = data[pos + 8 : pos + 8 + length]
        pos += 12 + length
        if chunk_type == b"IHDR":
            width, height, bit_depth, color_type, _, _, interlace = struct.unpack(
                ">IIBBBBB", chunk
            )
            if bit_depth != 8 or interlace != 0:
                raise ValueError(f"{path}: unsupported PNG format")
            channels = {0: 1, 2: 3, 4: 2, 6: 4}[color_type]
        elif chunk_type == b"IDAT":
            idat += chunk
        elif chunk_type == b"IEND":
            break

    if width is None or channels is None:
        raise ValueError(f"{path}: missing IHDR chunk")

    raw = zlib.decompress(bytes(idat))
    stride = width * channels
    pixels = bytearray(width * height * channels)
    previous = bytearray(stride)
    offset = 0
    for y in range(height):
        filter_type = raw[offset]
        offset += 1
        line = bytearray(raw[offset : offset + stride])
        offset += stride
        if filter_type == 1:
            for i in range(channels, stride):
                line[i] = (line[i] + line[i - channels]) & 0xFF
        elif filter_type == 2:
            for i in range(stride):
                line[i] = (line[i] + previous[i]) & 0xFF
        elif filter_type == 3:
            for i in range(stride):
                left = line[i - channels] if i >= channels else 0
                line[i] = (line[i] + ((left + previous[i]) >> 1)) & 0xFF
        elif filter_type == 4:
            for i in range(stride):
                left = line[i - channels] if i >= channels else 0
                up = previous[i]
                up_left = previous[i - channels] if i >= channels else 0
                estimate = left + up - up_left
                dist_left = abs(estimate - left)
                dist_up = abs(estimate - up)
                dist_up_left = abs(estimate - up_left)
                if dist_left <= dist_up and dist_left <= dist_up_left:
                    predictor = left
                elif dist_up <= dist_up_left:
                    predictor = up
                else:
                    predictor = up_left
                line[i] = (line[i] + predictor) & 0xFF
        elif filter_type != 0:
            raise ValueError(f"{path}: unsupported PNG filter {filter_type}")
        pixels[y * stride : (y + 1) * stride] = line
        previous = line

    return width, height, channels, bytes(pixels)


def write_png(path: Path, width: int, height: int, rgba: bytes) -> None:
    raw = bytearray()
    stride = width * 4
    for y in range(height):
        raw.append(0)
        raw += rgba[y * stride : (y + 1) * stride]

    def chunk(chunk_type: bytes, payload: bytes) -> bytes:
        crc = zlib.crc32(chunk_type + payload) & 0xFFFFFFFF
        return struct.pack(">I", len(payload)) + chunk_type + payload + struct.pack(">I", crc)

    path.write_bytes(
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
        + chunk(b"IDAT", zlib.compress(bytes(raw), 9))
        + chunk(b"IEND", b"")
    )


def plate_bounds(width: int, height: int, channels: int, pixels: bytes) -> tuple[int, int, int, int]:
    min_x, min_y = width, height
    max_x = max_y = -1
    for y in range(height):
        base = y * width * channels
        for x in range(width):
            if pixels[base + x * channels + 3] > 200:
                min_x = min(min_x, x)
                max_x = max(max_x, x)
                min_y = min(min_y, y)
                max_y = max(max_y, y)
    return min_x, min_y, max_x, max_y


def build_mask(foreground: Path, destination: Path) -> None:
    width, height, channels, pixels = read_png(foreground)
    min_x, min_y, max_x, max_y = plate_bounds(width, height, channels, pixels)
    corner_radius = CORNER_RADIUS_FRACTION * max(max_x - min_x + 1, max_y - min_y + 1)
    corner_radius_squared = corner_radius * corner_radius
    corners = ((min_x, min_y), (max_x, min_y), (min_x, max_y), (max_x, max_y))

    mask = bytearray(width * height * 4)
    for index in range(width * height):
        base = index * channels
        if channels == 4:
            red, green, blue, alpha = (
                pixels[base],
                pixels[base + 1],
                pixels[base + 2],
                pixels[base + 3],
            )
        elif channels == 3:
            red, green, blue = pixels[base], pixels[base + 1], pixels[base + 2]
            alpha = 255
        else:
            red = green = blue = pixels[base]
            alpha = 255

        deviation = 0
        if alpha >= 8:
            deviation = max(255 - red, 255 - green, 255 - blue)

        x = index % width
        y = index // width
        for corner_x, corner_y in corners:
            if (x - corner_x) ** 2 + (y - corner_y) ** 2 < corner_radius_squared:
                deviation = 0
                break

        if deviation <= THRESHOLD:
            out_alpha = 0
        elif deviation >= THRESHOLD + SOFT:
            out_alpha = 255
        else:
            out_alpha = int(255 * (deviation - THRESHOLD) / SOFT)

        mask[index * 4] = MASK_COLOR[0]
        mask[index * 4 + 1] = MASK_COLOR[1]
        mask[index * 4 + 2] = MASK_COLOR[2]
        mask[index * 4 + 3] = out_alpha

    write_png(destination, width, height, mask)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--res",
        type=Path,
        default=ANDROID_RES,
        help="Android res directory (defaults to androidApp/src/main/res)",
    )
    arguments = parser.parse_args()

    if shutil.which("sips") is None:
        print("sips not found; this script must run on macOS.", file=sys.stderr)
        return 1
    if shutil.which("cwebp") is None:
        print("cwebp not found; install it with 'brew install webp'.", file=sys.stderr)
        return 1

    with tempfile.TemporaryDirectory() as temporary_directory:
        temporary = Path(temporary_directory)
        for directory, size in DENSITIES:
            foreground = arguments.res / directory / "ic_launcher_foreground.webp"
            monochrome = arguments.res / directory / "ic_launcher_monochrome.webp"
            if not foreground.exists():
                print(f"missing foreground: {foreground}", file=sys.stderr)
                return 1

            decoded = temporary / f"{directory}.png"
            mask = temporary / f"{directory}-monochrome.png"
            subprocess.run(
                ["sips", "-s", "format", "png", str(foreground), "--out", str(decoded)],
                check=True,
                stdout=subprocess.DEVNULL,
            )
            build_mask(decoded, mask)
            subprocess.run(
                ["cwebp", "-quiet", "-lossless", str(mask), "-o", str(monochrome)],
                check=True,
            )
            print(f"{directory}: wrote {monochrome.relative_to(REPO_ROOT)} ({size}px)")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
