# Video guides (Dataverse 6.12)

Local review URL: http://localhost:8080/video-guides.xhtml.
The native User Guide header dropdown contains Written guide and Video guides
(Текстовий довідник and Відеоінструкції in Ukrainian). Both open in a new tab.
The written guide retains its existing launch restriction, configured URL and
official-guide fallback; the video item opens /video-guides.xhtml on this site.

The page uses the native Dataverse template, authentication, support dialog and
locale selector. Native English selects the English editorial fragment;
legacy `ua`, standard `uk` and the fallback select Ukrainian.
The three silent recordings show the Ukrainian interface in both page languages.

## Files

- `src/main/webapp/dataverse_header.xhtml`: native Bootstrap User Guide dropdown.
- `src/main/webapp/video-guides.xhtml`: public Facelets wrapper and metadata.
- `src/main/webapp/uard/content/video-guides.{uk,en}.xhtml`: editorial text,
  ordering, titles and durations.
- `src/main/webapp/uard/components/video-guide.xhtml`: shared native HTML video
  player with an accessible title, controls, inline playback and a direct MP4 link.
- `src/main/webapp/resources/videos/uard/`: MP4 recordings and JPEG posters.
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

| Source recording | Public MP4 filename | Duration | Size (bytes) |
| --- | --- | --- | --- |
| 1. Реєстрація через ORCID, без API.mkv | orcid-registration.mp4 | 2:19 | 43,535,952 |
| 2. Зміна даних в акаунті.mkv | account-details.mp4 | 0:44 | 13,807,198 |
| 3. Підтвердження адреси.mkv | email-confirmation.mp4 | 0:53 | 16,561,957 |

The inputs contain 1280×720, 30 fps H.264 High/yuv420p video. They declare
AAC-LC stereo tracks but contain no audio packets. The MP4s are silent, just like
the originals. They are remuxed without re-encoding, preserving every video packet.
MP4 fast-start metadata is moved before the media data for web playback.
The original MKVs are unchanged and are not copied into the application.

With FFmpeg installed, reproduce each MP4 using the matching source/output names:

```bash
ffmpeg -i "/path/to/source.mkv" -map 0:v:0 -map 0:a:0 \
  -c copy -map_metadata -1 -movflags +faststart \
  src/main/webapp/resources/videos/uard/orcid-registration.mp4
ffmpeg -ss 1 -i "/path/to/source.mkv" -frames:v 1 -q:v 3 \
  src/main/webapp/resources/videos/uard/orcid-registration.jpg
```

Use account-details and email-confirmation for the other two outputs. The posters
are frames at one second from their corresponding recordings. The MP4s total
about 70.5 MiB; account for them when packaging the application.
No third-party video hosting, external embeds or database settings are required.

## Deployment

Run `./scripts/dev/dev-frd.sh` after application/content/resource changes.
Mounted stylesheet edits update on refresh. Remote deployment preserves the
active installation's files and translation overrides.

For remote deployment, include the native header change, wrapper,
both fragments, component, MP4 MIME mapping and all six media assets in the
maintained application artifact.
Merge the scoped CSS, four org.videoGuides.* entries and two org.header.guides.*
entries into the target's existing
stylesheet and active language bundles, preserving remote-only changes and the
server's working ua/uk filename. Use its normal redeploy/bundle-cache reload
workflow. No new navbar setting is required.

## Local verification (2026-11-19)

Required FRD deployment and XML/whitespace checks passed. All three MP4s fully
decode, match the original compressed video-stream SHA-256 hashes, put the moov
index before media data, and return HTTP 206 with video/mp4 for byte ranges.
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
