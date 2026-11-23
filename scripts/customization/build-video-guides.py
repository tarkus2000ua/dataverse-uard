#!/usr/bin/env python3
"""Prepare 16:9 webpage-only MP4 guides and posters from the original MKVs."""

import argparse
from pathlib import Path
import os
import subprocess
import tempfile


REPOSITORY = Path(__file__).resolve().parents[2]
CROP = "1056:594:112:110"
RECORDINGS = (
    ("orcid-registration", "1. Реєстрація через ORCID, без API.mkv"),
    ("account-details", "2. Зміна даних в акаунті.mkv"),
    ("email-confirmation", "3. Підтвердження адреси.mkv"),
)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-dir", type=Path, required=True)
    parser.add_argument("--ffmpeg", default="ffmpeg")
    parser.add_argument("--output-dir", type=Path,
                        default=REPOSITORY / "src/main/webapp/resources/videos/uard")
    arguments = parser.parse_args()
    for _, filename in RECORDINGS:
        if not (arguments.source_dir / filename).is_file():
            parser.error(f"Missing original recording: {filename}")
    arguments.output_dir.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix=".video-guides-", dir=arguments.output_dir.parent) as temporary:
        staging = Path(temporary)
        for slug, filename in RECORDINGS:
            print(f"Cropping {slug} to 1056x594 (16:9)...", flush=True)
            video = staging / f"{slug}.mp4"
            subprocess.run([
                arguments.ffmpeg, "-hide_banner", "-loglevel", "error", "-nostdin",
                "-i", str(arguments.source_dir / filename), "-map", "0:v:0",
                "-vf", f"crop={CROP}", "-c:v", "libx264", "-preset", "slow",
                "-crf", "18", "-pix_fmt", "yuv420p", "-threads", "4", "-g", "60",
                "-an", "-map_metadata", "-1", "-movflags", "+faststart", str(video),
            ], check=True)
            subprocess.run([
                arguments.ffmpeg, "-hide_banner", "-loglevel", "error", "-nostdin",
                "-ss", "1", "-i", str(video), "-frames:v", "1", "-q:v", "2",
                str(staging / f"{slug}.jpg"),
            ], check=True)
            print(f"Prepared {slug}: {video.stat().st_size:,} bytes", flush=True)
        # Publish only after all recordings and posters have been prepared.
        for path in sorted(staging.iterdir()):
            os.replace(path, arguments.output_dir / path.name)
    print(f"Updated all three videos and posters in {arguments.output_dir}", flush=True)


if __name__ == "__main__":
    main()
