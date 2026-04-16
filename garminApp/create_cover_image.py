#!/usr/bin/env python3

from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont


ROOT = Path(__file__).resolve().parent
OUTPUT = ROOT / "store-assets" / "cover-image.png"
ICON = ROOT.parent / "design" / "biciradar-app-icon-source.png"
SCREEN_1 = ROOT.parent / "store" / "final" / "wearos" / "01-dashboard.png"
SCREEN_2 = ROOT.parent / "store" / "final" / "wearos" / "02-station-detail.png"
SCREEN_3 = ROOT.parent / "store" / "final" / "wearos" / "03-monitoring.png"

WIDTH = 1600
HEIGHT = 900


def rounded_mask(size, radius):
    mask = Image.new("L", size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle((0, 0, size[0], size[1]), radius=radius, fill=255)
    return mask


def fit_crop(image, size):
    src_ratio = image.width / image.height
    dst_ratio = size[0] / size[1]

    if src_ratio > dst_ratio:
        new_height = size[1]
        new_width = int(new_height * src_ratio)
    else:
        new_width = size[0]
        new_height = int(new_width / src_ratio)

    resized = image.resize((new_width, new_height), Image.Resampling.LANCZOS)
    left = (new_width - size[0]) // 2
    top = (new_height - size[1]) // 2
    return resized.crop((left, top, left + size[0], top + size[1]))


def paste_card(canvas, image, box, radius=28, shadow_offset=(0, 16), shadow_blur=28):
    x, y, w, h = box
    card = fit_crop(image, (w, h)).convert("RGBA")
    mask = rounded_mask((w, h), radius)

    shadow = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    sx, sy = shadow_offset
    shadow_draw.rounded_rectangle((x + sx, y + sy, x + w + sx, y + h + sy), radius=radius, fill=(14, 24, 38, 70))
    shadow = shadow.filter(ImageFilter.GaussianBlur(shadow_blur))
    canvas.alpha_composite(shadow)

    layer = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    layer.paste(card, (x, y), mask)
    canvas.alpha_composite(layer)


def load_font(size, bold=False):
    candidates = []
    if bold:
        candidates = [
            "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
            "/System/Library/Fonts/SFNS.ttf",
            "/Library/Fonts/Arial Bold.ttf",
        ]
    else:
        candidates = [
            "/System/Library/Fonts/Supplemental/Arial.ttf",
            "/Library/Fonts/Arial.ttf",
        ]

    for path in candidates:
        if Path(path).exists():
            return ImageFont.truetype(path, size=size)
    return ImageFont.load_default()


def main():
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)

    canvas = Image.new("RGBA", (WIDTH, HEIGHT), "#F4F8FC")
    draw = ImageDraw.Draw(canvas)

    draw.rectangle((0, 0, WIDTH, HEIGHT), fill="#F4F8FC")
    draw.ellipse((-160, -120, 520, 520), fill="#DCEEFE")
    draw.ellipse((980, 420, 1660, 1160), fill="#D8F5EB")

    icon = Image.open(ICON).convert("RGBA").resize((160, 160), Image.Resampling.LANCZOS)
    canvas.alpha_composite(icon, (120, 110))

    title_font = load_font(70, bold=True)
    subtitle_font = load_font(28)
    chip_font = load_font(26, bold=True)

    draw.text((310, 120), "BiciRadar", font=title_font, fill="#0F172A")
    draw.text((310, 205), "Bike sharing at a glance on your Garmin", font=subtitle_font, fill="#334155")

    chip_y = 275
    chips = ["Nearest station", "Available bikes", "Distance"]
    chip_x = 310
    for chip in chips:
        bbox = draw.textbbox((0, 0), chip, font=chip_font)
        cw = (bbox[2] - bbox[0]) + 34
        draw.rounded_rectangle((chip_x, chip_y, chip_x + cw, chip_y + 48), radius=24, fill="#FFFFFF")
        draw.text((chip_x + 17, chip_y + 10), chip, font=chip_font, fill="#0F766E")
        chip_x += cw + 14

    s1 = Image.open(SCREEN_1)
    s2 = Image.open(SCREEN_2)
    s3 = Image.open(SCREEN_3)

    paste_card(canvas, s1, (170, 430, 240, 240))
    paste_card(canvas, s2, (470, 380, 300, 300))
    paste_card(canvas, s3, (860, 430, 240, 240))

    draw.rounded_rectangle((1180, 180, 1460, 620), radius=42, fill="#0F172A")
    draw.rounded_rectangle((1205, 220, 1435, 585), radius=30, fill="#111827")
    draw.text((1252, 260), "8", font=load_font(118, bold=True), fill="#22C55E")
    draw.text((1240, 390), "bikes nearby", font=load_font(30, bold=True), fill="#E2E8F0")
    draw.text((1262, 445), "150 m", font=load_font(34), fill="#94A3B8")
    draw.text((1232, 510), "Open app to sync", font=load_font(24), fill="#64748B")

    footer_font = load_font(24)
    draw.text((120, 810), "Companion app required for sync", font=footer_font, fill="#64748B")

    rgb = canvas.convert("RGB")
    rgb.save(OUTPUT, format="PNG", optimize=True)


if __name__ == "__main__":
    main()
