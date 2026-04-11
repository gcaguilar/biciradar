#!/usr/bin/env python3

import argparse
import subprocess
import sys
from pathlib import Path


IPHONE_SIZES = {
    (1260, 2736),
    (1290, 2796),
    (1320, 2868),
    (1242, 2688),
    (1284, 2778),
    (1179, 2556),
    (1206, 2622),
    (1170, 2532),
    (1125, 2436),
    (1080, 2340),
    (1242, 2208),
    (750, 1334),
    (640, 1096),
    (640, 1136),
    (640, 920),
    (640, 960),
}

APPLE_WATCH_SIZES = {
    (422, 514),
    (410, 502),
    (416, 496),
    (396, 484),
    (368, 448),
    (312, 390),
}


def with_landscape(size_set: set[tuple[int, int]]) -> set[tuple[int, int]]:
    output = set(size_set)
    for width, height in list(size_set):
        output.add((height, width))
    return output


IPHONE_SIZES = with_landscape(IPHONE_SIZES)
APPLE_WATCH_SIZES = with_landscape(APPLE_WATCH_SIZES)


def read_dimensions(path: Path) -> tuple[int, int]:
    result = subprocess.run(
        ["sips", "-g", "pixelWidth", "-g", "pixelHeight", str(path)],
        capture_output=True,
        text=True,
        check=True,
    )
    width = None
    height = None
    for line in result.stdout.splitlines():
        if "pixelWidth:" in line:
            width = int(line.split(":", 1)[1].strip())
        if "pixelHeight:" in line:
            height = int(line.split(":", 1)[1].strip())
    if width is None or height is None:
        raise RuntimeError(f"Unable to read dimensions for {path}")
    return width, height


def ratio_close(width: int, height: int, expected_w: int, expected_h: int, tolerance: float = 0.03) -> bool:
    return abs((width / height) - (expected_w / expected_h)) <= tolerance


def expand_inputs(inputs: list[str]) -> list[Path]:
    paths: list[Path] = []
    for raw in inputs:
        path = Path(raw)
        if path.is_dir():
            paths.extend(sorted(p for p in path.rglob("*") if p.suffix.lower() in {".png", ".jpg", ".jpeg"}))
        elif path.is_file():
            paths.append(path)
    return paths


def validate(platform: str, width: int, height: int) -> tuple[list[str], list[str]]:
    errors: list[str] = []
    warnings: list[str] = []

    if platform == "android-phone":
        if not (320 <= width <= 3840 and 320 <= height <= 3840):
            errors.append("Google Play phone screenshots must stay within 320px and 3840px on each side.")
        if max(width, height) > 2 * min(width, height):
            errors.append("Google Play phone screenshots cannot have a longest side more than 2x the shortest side.")
        if ratio_close(width, height, 9, 16) or ratio_close(width, height, 16, 9):
            if min(width, height) < 1080:
                warnings.append("Valid for upload, but below the 1080px recommendation used for featuring formats.")
        else:
            warnings.append("Valid bounds may still pass, but 9:16 portrait or 16:9 landscape is the recommended Play canvas.")
        return errors, warnings

    if platform == "wearos":
        if width != height:
            errors.append("Wear OS screenshots must be square (1:1 aspect ratio).")
        if width < 384 or height < 384:
            errors.append("Wear OS screenshots must be at least 384x384.")
        return errors, warnings

    if platform == "iphone":
        if (width, height) not in IPHONE_SIZES:
            errors.append("Size is not one of Apple's accepted iPhone screenshot dimensions.")
        return errors, warnings

    if platform == "apple-watch":
        if (width, height) not in APPLE_WATCH_SIZES:
            errors.append("Size is not one of Apple's accepted Apple Watch screenshot dimensions.")
        return errors, warnings

    raise ValueError(f"Unsupported platform: {platform}")


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate screenshots against store-size rules.")
    parser.add_argument(
        "--platform",
        required=True,
        choices=("android-phone", "wearos", "iphone", "apple-watch"),
    )
    parser.add_argument("inputs", nargs="+")
    args = parser.parse_args()

    files = expand_inputs(args.inputs)
    if not files:
        print("No screenshot files found.", file=sys.stderr)
        return 1

    exit_code = 0
    for path in files:
        width, height = read_dimensions(path)
        errors, warnings = validate(args.platform, width, height)
        status = "PASS"
        if errors:
            status = "FAIL"
            exit_code = 1
        elif warnings:
            status = "WARN"
        print(f"{status} {path} {width}x{height}")
        for error in errors:
            print(f"  error: {error}")
        for warning in warnings:
            print(f"  warning: {warning}")

    return exit_code


if __name__ == "__main__":
    raise SystemExit(main())
