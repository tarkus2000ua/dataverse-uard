# Local Ukrainian User Guide

Target: Dataverse 6.12. Local integration and direct chapter URLs: 2026-11-01.

## Public URLs

- /guide/ — introduction and table of contents.
- /guide/account.html — account management.
- /guide/find-use-data.html — finding and using data.
- /guide/dataverse-management.html — collection management.
- /guide/dataset-management.html — dataset and file management.
- /guide/tabular-data-ingest.html — tabular data ingest.
- /guide/appendix.html — appendix.

Heading links use each chapter's existing IDs, for example:
http://localhost:8080/guide/account.html#api-token
http://localhost:8080/guide/dataset-management.html#publish-dataset

Every address returns a complete page with the sidebar, title and footer.
Chapter navigation, heading navigation, direct entry, refresh, browser
Back/Forward and opening links in new tabs use ordinary browser behavior.
The guide works without JavaScript. /guide/welcome.html and /guide/index.html
also remain usable complete introduction pages.

## Native header

The supported local setting is:

    :NavbarGuidesUrl=http://localhost:8080/guide/

Use the trailing slash in the menu setting: JSF can append ;jsessionid on first
visits, and the default directory redirect can otherwise mis-encode that URL.
Typing /guide in the address bar still redirects normally to /guide/.

The real native header is unchanged. Its User Guide item retains its own
target="_blank", rel="noopener", translated label and session behavior. The
separate :NavbarAboutUrl remains http://localhost:8080/system.xhtml, and the
bilingual footer article remains /about.xhtml.

The setting is stored in the local database. No uard2 configuration or deployment
is included in this local task.

## Editable sources and generated output

- customization/guide/index.html is the shared page template. The builder fills
  its guideContent and guideTitle placeholders for each output page.
- The seven other HTML files are the supplied editorial chapter fragments.
- customization/guide/pics/ contains the supplied translated JPGs.
- scripts/customization/build-guide.py builds complete public pages and copies
  referenced local images into src/main/webapp/guide/.

Edit source files and rebuild; do not edit generated public pages directly.
A folder-local .gitattributes normalizes source HTML to LF in Git; existing
working-copy content is retained, and the builder produces consistent output
from either Windows or Linux source line endings.
The builder validates chapter destinations, heading IDs, duplicate IDs and local
image paths/availability. --check detects stale generated pages or images.
It converts the supplied fragments' original HTMX navigation attributes to normal
href links, sets the current sidebar item and generates descriptive page titles.

The generated guide has no JavaScript runtime dependency. Styling reuses the
existing /resources/bs/css/bootstrap.min.css. Original chapter wording and the
explicit Dataverse 6.6 source attribution are preserved; this is not a content
revision claiming that every instruction was reviewed for 6.12.

The guide is Ukrainian in either application UI locale and does not introduce a
separate language preference or change Dataverse's native session locale.

The supplied deploy_guide_ua.md is retained as historical reference, with a
notice pointing to this maintained procedure. Its older HelpUrl/custom-header
steps are not used by the 6.12 integration.

## Build and local deployment

From /home/andrey/projects/dataverse:

    python3 scripts/customization/build-guide.py
    python3 scripts/customization/build-guide.py --check
    ./scripts/dev/dev-frd.sh

Configure the native hook after the guide is available, using the existing
local development admin API policy:

    curl --fail-with-body -X PUT \
      --data-binary 'http://localhost:8080/guide/' \
      http://localhost:8080/api/admin/settings/:NavbarGuidesUrl

Do not use a local localhost URL as another server's menu destination. External
branding hooks do not host these application resources. Guide changes require
the builder and fast redeploy. The builder does not delete unrelated files.

## Routing and upgrades

One additional url-mapping in src/main/webapp/WEB-INF/pretty-config.xml forwards
/guide/ to /guide/index.html; all prior mappings remain. Chapter .html files are
normal static application resources, so no Java or per-chapter route is needed.

Keep the mapping, guide builder and generated pages/images together during
upgrades. Do not replace native header, authentication or permission logic.
Settings and file contents are separate deployment concerns.

## Translated diagrams

| Source file | Guide location | Replaced illustration |
| --- | --- | --- |
| pics/1.jpg | Collection management | Dataverse-Diagram.png |
| pics/2.jpg | Dataset introduction | DatasetDiagram.png |
| pics/3.jpg | Computational workflow | computational-workflow-diagram.png |
| pics/4.jpg | Dataset versions | data_publishing_version_workflow.png |

The original JPG bytes and filenames are retained. Only four image elements in
the source chapters changed, with descriptive Ukrainian alt text and intrinsic
dimensions. Other illustrations retain the supplied external documentation URLs.

## Verification and rollback

Verify the native new-tab menu, direct chapter/heading URLs, refresh, Back/Forward,
the sidebar's active item, JavaScript-disabled navigation, local images and
mobile layouts. The original local :NavbarGuidesUrl setting was absent; deleting
that single local setting restores the default official guide destination.

No data import, schema update, database/volume recreation or authentication/locale
configuration change is needed. Restore application files through the normal
existing workflow if rolling back. No uard2 operation or push is part of this task.

## Verified direct-URL behavior (2026-11-01)

Local fast redeploy succeeded. All seven chapter URLs return HTTP 200 with one
complete sidebar, footer and content area, a matching active item and a chapter
title. Rendered chapter text matches the supplied source fragments.

Actual browser checks cover the introductory account.html#api-token link,
refresh at that address, changing chapters, Back/Forward, and a same-chapter
dataset-management.html#publish-dataset link followed by refresh. Direct entry
works with JavaScript disabled, including navigation and translated images.
All seven chapters fit a 375px viewport without horizontal overflow. The native
header still opens the guide in a new tab with noopener. No JavaScript page
errors occurred; desktop/mobile screenshots were inspected.

The seven editorial source fragments and supplied JPG bytes are unchanged by
the URL fix. Original staging and unrelated dirty/untracked work are preserved.
No uard2 changes or push are included.
