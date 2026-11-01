# GIT_STRATEGY.md

## Remote model
Recommended:
```text
upstream -> https://github.com/IQSS/dataverse.git
origin   -> organization/private Dataverse repository
```

If the current local clone still has IQSS as `origin`:

```bash
git remote rename origin upstream
git remote add origin git@github.com:YOUR-ORG/dataverse-custom.git
git remote -v
```

## Maintained release branches
Use versioned customization branches:

```text
custom/6.12
custom/6.13
custom/6.14
```

Avoid one eternal `custom` branch.

## Feature branches
Examples:
```text
feature/custom-homepage
feature/header-redesign
feature/footer-redesign
feature/ukrainian-ui
feature/homepage-react
```

Create from:
```bash
git switch custom/6.12
git switch -c feature/header-redesign
```

## Commit strategy
Prefer small semantic commits.

Good:
```text
Add custom institutional homepage
Add Ukrainian navigation labels
Restructure global navbar markup
Preserve account menu in custom header layout
Add branding bind mount for local development
```

Avoid:
```text
UI changes
fixes
final changes
```

## Upgrade flow
When moving to a new Dataverse release:

```text
custom/6.12
   |
upgrade/6.13
   |
custom/6.13
```

Suggested:
```bash
git fetch upstream --tags
git switch custom/6.12
git switch -c upgrade/6.13
git merge v6.13
```

For an internal institutional fork, merge-based upgrades are usually easier to audit than rebasing shared history.

## Deployment/version tags
Use your own deployment tags, e.g.:

```text
org-dataverse-6.12.0
org-dataverse-6.12.1
org-dataverse-6.12.2
```

## Track upstream modifications
Maintain:
```text
docs/upstream-modifications.md
```

Example:
```text
src/main/webapp/dataverse_header.xhtml
Reason:
- institutional navigation redesign
- original JSF auth/permission behavior preserved
```

## Current publication and exclusions (2026-11-01)

Actual origin: `git@github.com:tarkus2000ua/dataverse-uard.git`; upstream remains
`https://github.com/IQSS/dataverse.git`. Use `codex/` for new feature branch names.
Current branch `codex/login-local-guide` contains the preceding ORCID, Ukrainian
collection terminology, guide and logo commits. The user authorized publishing
the necessary current changes to this branch; this does not authorize remote
application deployment or unrelated future pushes.

Use explicit paths or an isolated index to exclude the staged dummy
`customization/branding/custom-header.html`. Keep design inputs under
`customization/homepage/assets/` and `dataverse.zip` outside this publication.
The installed Ukrainian bundle and FRD recovery helpers/tests are maintained
source, unlike database/volume backups and generated local archives. Never
force-push shared history to reconcile unexpected remote changes.
