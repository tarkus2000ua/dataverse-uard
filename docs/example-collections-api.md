# Example institution collections for the UaRD layout

These are proposed names from the design reference, not verified institutional
participation. Intended for the local development instance. No collections have
been created by preparing these files. Names can be edited before running.

Source data: `customization/homepage/content/example-collections.json`.
Runner: `scripts/customization/example-collections.sh` (Bash in WSL).

| Alias | Collection name |
|---|---|
| dntb | Державна науково-технічна бібліотека України |
| vntu | Вінницький національний технічний університет |
| knu | Київський національний університет імені Тараса Шевченка |
| nasu | Національна академія наук України |
| nure | Харківський національний університет радіоелектроніки |

## Run in the WSL repository

Use an API token from your Dataverse account (API Token tab), with permission to
create children, publish them, and edit the parent's featured list. Enter a real
contact email you control for these development collections; no institutional
contact addresses are fabricated. The parent must be published before publishing
its children. The local root is normally already published.

```bash
export SERVER_URL=http://localhost:8080
export PARENT=:root
read -rsp 'Dataverse API token: ' API_TOKEN; printf '\n'
export API_TOKEN
read -rp 'Contact email for development collections: ' CONTACT_EMAIL
export CONTACT_EMAIL

# 1. Create five draft child collections; review them in the UI.
bash scripts/customization/example-collections.sh create

# 2. Publish those collections so anonymous visitors can see them.
bash scripts/customization/example-collections.sh publish

# 3. Append missing aliases to the root featured list, preserving existing entries.
bash scripts/customization/example-collections.sh feature

# 4. Read back the configured featured aliases (no token required).
bash scripts/customization/example-collections.sh list
unset API_TOKEN CONTACT_EMAIL
```

Create/publish are intentional one-time operations, not a synchronization tool.
The runner stops at the first HTTP error. If it fails halfway, inspect the
response and existing collections before resuming; creating an existing alias
or publishing an already-published collection can fail. It never deletes or
overwrites collections. Aliases are instance-wide and must be unique.
Feature mode skips aliases already in the list. New entries follow the JSON
order; existing entries retain their order. It does not reorder existing items.

## The underlying API calls

Create one child with JSON containing required `name`, `alias`, and contacts:

```bash
curl --fail-with-body -H "X-Dataverse-key: $API_TOKEN" \
  -H 'Content-Type: application/json' -X POST \
  --data-binary @collection.json "$SERVER_URL/api/dataverses/$PARENT"
```

Example `collection.json` (replace the contact before using):

```json
{
  "name": "Державна науково-технічна бібліотека України",
  "alias": "dntb",
  "dataverseType": "ORGANIZATIONS_INSTITUTIONS",
  "dataverseContacts": [{"contactEmail": "REPLACE_WITH_YOUR_CONTACT_EMAIL"}]
}
```

Publish and feature:

```bash
curl --fail-with-body -H "X-Dataverse-key: $API_TOKEN" -X POST \
  "$SERVER_URL/api/dataverses/dntb/actions/:publish"

# Run after all five are published; errors if an alias is already featured.
curl --fail-with-body -H "X-Dataverse-key: $API_TOKEN" \
  -H 'Content-Type: application/json' -X POST \
  --data-binary '["dntb","vntu","knu","nasu","nure"]' \
  "$SERVER_URL/api/dataverses/$PARENT/featured"
```

Verified against repository `doc/sphinx-guides/source/api/native-api.rst` and
`src/main/java/edu/harvard/iq/dataverse/api/Dataverses.java` for 6.12. `/featured`
is the featured **collections** list, distinct from the richer `/featuredItems` API.
Only published children or linked collections are eligible.

## Homepage integration

The native Dataverse page already renders a featured-collections carousel. The
custom HTML homepage does not automatically inherit that JSF component. Step 4
will read `/api/dataverses/:root/featured`, resolve each alias through
`/api/dataverses/{alias}`, and render an accessible horizontal card strip.
This reuses the native curation model and API; no custom endpoint is needed.

Institutional logos and bilingual editorial labels remain separate follow-ups.
Dataverse stores a single collection name; switching locale does not translate
that name automatically. Do not generate official logos from the screenshot.
