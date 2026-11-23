# Video guides (Dataverse 6.12)

Local review URL: http://localhost:8080/video-guides.xhtml.
The native User Guide header dropdown contains Written guide and Video guides
(Текстовий довідник and Відеоінструкції in Ukrainian). Both open in a new tab.
The written guide retains its existing launch restriction, configured URL and
official-guide fallback; the video item opens /video-guides.xhtml on this site.

The page uses the native Dataverse template, authentication, support dialog and
locale selector. Native English selects the English editorial fragment;
legacy `ua`, standard `uk` and the fallback select Ukrainian.
The three recordings show the Ukrainian interface in both page languages.
Each now has Ukrainian and English narration. The native Dataverse locale selects
the audio language: `en` uses `-en.mp4`; Ukrainian `ua`/`uk` and the fallback use
the original `.mp4` filenames. Embedded and direct links select the same file.
No version query parameters are used.

## Files

- `src/main/webapp/dataverse_header.xhtml`: native Bootstrap User Guide dropdown.
- `src/main/webapp/video-guides.xhtml`: public Facelets wrapper and metadata.
- `src/main/webapp/uard/content/video-guides.{uk,en}.xhtml`: editorial text,
  ordering, titles and durations.
- `src/main/webapp/uard/components/video-guide.xhtml`: shared native HTML video
  player with an accessible title, controls, inline playback and a direct MP4 link.
- `src/main/webapp/resources/videos/uard/`: MP4 recordings and JPEG posters.
- `scripts/customization/build-video-guides.py`: reproducible 16:9 cropping,
  H.264 encoding and poster generation from the original MKVs.
- `src/main/webapp/WEB-INF/web.xml`: explicit MP4 MIME mapping (video/mp4).
- `customization/branding/custom-stylesheet.css`: scoped responsive layout.
- `org.videoGuides.*` and `org.header.guides.*` in the packaged English fallback
  and external English/Ukrainian bundles: player/menu UI labels, separate from
  the editorial content.

Players use `preload="none"` and do not autoplay. The browser loads the poster
before playback; the large recordings are requested when the user plays them.
Desktop has a description beside each wide player; tablet/mobile stacks them.
Players have square corners and no custom background color.
Each section supports a direct fragment link:
`#orcid-registration`, `#account-details`, `#email-confirmation`.

## Recordings and preparation

The supplied sources are in the user's Windows Downloads/Telegram Desktop folder.
They are read directly through /mnt/c in WSL; the repository stays in WSL.

| Source recording | Ukrainian MP4 | English MP4 | Duration |
| --- | --- | --- | --- |
| 1. Реєстрація через ORCID, без API.mkv | orcid-registration.mp4 | orcid-registration-en.mp4 | 2:19 |
| 2. Зміна даних в акаунті.mkv | account-details.mp4 | account-details-en.mp4 | 0:44 |
| 3. Підтвердження адреси.mkv | email-confirmation.mp4 | email-confirmation-en.mp4 | 0:53 |

The original recordings contain 1280×720, 30 fps H.264 High/yuv420p video.
The current derivatives use `crop=1056:594:112:110`: remove 110 pixels
from the top, 112 from each side and 16 from the bottom. This removes the
browser tabs, address bar and bookmarks while keeping exactly 16:9.
The image is not stretched or resized.

Cropping requires re-encoding. The builder uses H.264/libx264, slow preset,
CRF 18, yuv420p, two-second keyframes and MP4 fast-start. The originals declare
empty AAC tracks and have no audio packets; the crop builder produces silent
intermediates. The published derivatives add their language-specific narration.
The original MKVs are unchanged and are not copied into the application.

Rebuild all three MP4s and their matching JPEG posters with FFmpeg installed:

```bash
python3 scripts/customization/build-video-guides.py \
  --source-dir "/path/to/original-mkvs"
```

Use `--ffmpeg /path/to/ffmpeg` if the executable is not on PATH. The source
filenames are listed above. The builder prepares every output in a temporary
directory before replacing the published assets. Rebuilding crops overwrites the
Ukrainian narrated files with silent intermediates; remux narration afterwards.
Posters use the cropped recording at one second. The six narrated MP4s total
about 28.4 MiB.
No third-party video hosting, external embeds or database settings are required.

## Narration revision (2026-11-22)

The six cue scripts are maintained in `customization/guide/narration/` as
`{video-slug}.{uk,en}.json`. They follow the written account guide and the
recorded email-confirmation workflow. OpenAI `gpt-4o-mini-tts`, voice `marin`,
produced one continuous take per video and language with a calm female voice.

The approved revision places phrase boundaries inside measured pauses, preserving
every original PCM sample without fades, time stretching or pitch shifts.
Loudness uses a fixed gain only; the earlier dynamic normalization was removed.
Final files copy the cropped H.264 stream unchanged and add AAC audio at 48 kHz.
All six passed complete decode and identical compressed-video-stream checks.
The repair reused existing speech synthesis and made no further API requests.

The local FRD deployment and an anonymous native English → Ukrainian locale
round trip passed with the expected three video URLs and player language in
each locale. The original Ukrainian links and matching posters are retained.
Narration files, the shared player and the `org.videoGuides.audioLanguage`
entry in packaged/external bundles must be deployed together. API credentials,
raw takes and local review files are not application deployment inputs.

All six corrected versions were deployed to uard2 on November 22 at 19:51 UTC.
The server locale round trip and public page/video checks passed; Cloudflare
served the replacements at their stable URLs. See the
[narrated-video deployment record](remote-deployment.md#narrated-video-guides-on-uard2-2026-11-22)
for the rollback archive, updated WAR and preservation audit. The source
revision includes the six narrated MP4s, posters, cue scripts and player changes.

## Deployment

Run `./scripts/dev/dev-frd.sh` after application/content/resource changes.
Mounted stylesheet edits update on refresh. Remote deployment preserves the
active installation's files and translation overrides.

For remote deployment, include the native header change, wrapper,
both fragments, component, MP4 MIME mapping and all nine media assets in the
maintained application artifact.
Merge the scoped CSS, four org.videoGuides.* entries and two org.header.guides.*
entries into the target's existing
stylesheet and active language bundles, preserving remote-only changes and the
server's working ua/uk filename. Use its normal redeploy/bundle-cache reload
workflow. No new navbar setting is required.

## Initial publication checks (2026-11-19)

Required FRD deployment and XML/whitespace checks passed. The initial uncropped
MP4s fully decoded, matched the original compressed video-stream SHA-256 hashes,
put the moov index before media data, and returned HTTP 206 with video/mp4 for
byte ranges.
All three embedded players decoded and advanced playback in the browser. Native
Ukrainian/English switching selects matching metadata, articles and player labels.
Responsive checks at 320, 375, 768 and 1440 px found no horizontal overflow;
mobile/tablet stack the descriptions and players. These browser checks preceded
the User Guide dropdown integration. The header update uses the existing native
Bootstrap dropdown behavior; no additional browser checks were run.
Unrelated staged work is preserved.

## Confirmed uard2 deployment

Deployed on 2026-11-19 at 13:34 Europe/Kyiv from commit
`e5be463b4c7e84ba8899bceef3a3a89d112ae217`. The public page is
https://uard2.dntb.gov.ua/video-guides.xhtml.

The server-specific header, translations and stylesheet were merged incrementally.
The Payara service restarted successfully; public page/media checks and the
complete application/language-file preservation audit passed. The release holds
rollback backups and an updated WAR. See [the deployment record](remote-deployment.md#confirmed-uard2-video-guides-deployment-2026-11-19)
for exact paths, artifact checksum, verification and rollback.

## Crop revision (2026-11-19)

All three videos and posters now contain webpage content only, at
1056×594 (16:9). Sample frames include registration, account editing and email
confirmation. Complete decode and MP4 fast-start checks passed for all three
outputs. Browser checks were not repeated. The crops and posters were installed
on uard2 on November 19 at 13:52 Europe/Kyiv, with the original player URLs
restored by 13:59. The final origin audit confirms only six media files changed.
During the November 19 check, Cloudflare cached the earlier files for up to four
hours. The November 22 narration deployment confirmed that the public URLs now
serve the replacements. See the
[cropped-media deployment record](remote-deployment.md#cropped-video-replacement-on-uard2-2026-11-19).
