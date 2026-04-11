#!/usr/bin/env python3

import argparse
import math
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path


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


def run(cmd: list[str]) -> None:
    subprocess.run(cmd, check=True)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Resample an image to an exact canvas. By default, refuse aspect-ratio mismatches."
    )
    parser.add_argument("input", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("width", type=int)
    parser.add_argument("height", type=int)
    parser.add_argument(
        "--mode",
        choices=("exact", "pad"),
        default="exact",
        help="exact: only resample when aspect already matches. pad: fit and then pad to the canvas.",
    )
    parser.add_argument(
        "--pad-color",
        default="000000",
        help="Hex RGB pad color used only with --mode pad.",
    )
    parser.add_argument(
        "--tolerance",
        type=float,
        default=0.01,
        help="Allowed aspect-ratio delta before exact mode refuses to proceed.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    src = args.input
    dst = args.output
    target_w = args.width
    target_h = args.height

    if not src.is_file():
        print(f"Input file not found: {src}", file=sys.stderr)
        return 1

    src_w, src_h = read_dimensions(src)
    src_ratio = src_w / src_h
    target_ratio = target_w / target_h

    dst.parent.mkdir(parents=True, exist_ok=True)

    if args.mode == "exact":
        if abs(src_ratio - target_ratio) > args.tolerance:
            print(
                "Aspect ratio mismatch. "
                f"Source is {src_w}x{src_h}, target is {target_w}x{target_h}. "
                "Use a matching source image or rerun with --mode pad if you explicitly want letterboxing.",
                file=sys.stderr,
            )
            return 1
        run(
            [
                "sips",
                "--resampleHeightWidth",
                str(target_h),
                str(target_w),
                str(src),
                "--out",
                str(dst),
            ]
        )
        print(dst)
        return 0

    scale = min(target_w / src_w, target_h / src_h)
    resized_w = max(1, int(math.floor(src_w * scale)))
    resized_h = max(1, int(math.floor(src_h * scale)))

    with tempfile.TemporaryDirectory(prefix="fit-image-to-canvas-") as temp_dir:
        temp_path = Path(temp_dir) / src.name
        shutil.copy2(src, temp_path)
        run(
            [
                "sips",
                "--resampleHeightWidth",
                str(resized_h),
                str(resized_w),
                str(temp_path),
                "--out",
                str(temp_path),
            ]
        )
        run(
            [
                "sips",
                "--padToHeightWidth",
                str(target_h),
                str(target_w),
                "--padColor",
                args.pad_color,
                str(temp_path),
                "--out",
                str(dst),
            ]
        )

    print(dst)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
