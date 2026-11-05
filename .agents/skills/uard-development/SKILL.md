---
name: uard-development
description: Handle implementation tasks in the UaRD Dataverse repository with automatic feature-branch setup, preservation of local work, and the maintained custom/6.12 workflow. Apply to code, UI, configuration-file, and maintained documentation changes in this project; analysis-only requests require no Git mutations.
---

# UaRD development workflow

Handle Git setup as part of an ordinary implementation request. Do not require the
user to provide a special prompt, create a branch, or repeat an already authorized
step. Keep implementation, validation and Git completion within the user's scope.

## Project context

Use the existing WSL repository `/home/andrey/projects/dataverse`, accessible from
Windows as `\\wsl$\Ubuntu\home\andrey\projects\dataverse`. Run Git/build commands
inside WSL. Read the repository's AGENTS.md, GIT_STRATEGY.md and current handoff;
use relevant feature documentation as needed. If the repository or release policy
has changed, inspect the actual state and follow the current user/project rules.

The maintained baseline is `custom/6.12`; ordinary task branches use `codex/<task>`.
Origin is the UaRD fork; upstream is IQSS Dataverse. Preserve the original release
tag and `develop` tracking `upstream/develop`. Read-only questions/reviews remain
read-only. An explicitly requested existing branch takes precedence.

## Start or continue implementation

Before editing, inspect status including the index and untracked files, current
branch/tracking, and branch differences. Record the unrelated staged entries and
file state needed to verify preservation. Pay particular attention to the existing
staged unused `customization/branding/custom-header.html`, untracked original
`customization/homepage/assets/`, and `dataverse.zip`; verify whether they still
exist rather than assuming an old snapshot is current.

- Continue on an existing unmerged task branch when the user's request belongs to
  that task. Inspect its commits and pending changes to establish scope.
- For a new independent implementation, create a descriptive `codex/<task>` branch
  explicitly from the current maintained baseline before editing. Choose the name
  yourself; reuse a matching branch only after checking its contents.
- Refresh origin refs when needed. Fast-forward the baseline from origin when the
  working/index state permits it, preserving unrelated local files. If history has
  diverged, inspect it; do not reset or force-push to resolve the difference.
- A task that needs unfinished work may branch from that feature deliberately.
  State the dependency and integrate the prerequisite first when authorized.
- Local staged/untracked work alone is not a reason to ask the user to clean the
  checkout. It may remain in place when a safe switch preserves it. If a switch
  would conflict with unrelated work, do independent preparation and ask only for
  the unresolved choice; never silently stash, discard, commit or move that work.

Avoid creating a new branch for every follow-up or stacking independent tasks on
an unrelated feature just because it happens to be checked out. Do not edit the
maintained baseline directly for ordinary feature work.

## Implement and validate

Complete the requested change and relevant checks. Follow the project's FRD versus
mounted-branding workflow. Keep native JSF behavior and the upstream modification
record intact. No database/volume reset, container recreation or remote deployment
is implied by branch setup or code validation.

Before committing, inspect the staged diff and select only task paths. With
unrelated files already staged, use `git commit --only -- <task-paths>` or an
isolated index so those files retain their original staged state. Avoid blanket
staging. Verify the final task diff and preservation of unrelated work.

## Finish within the requested Git scope

An implementation request authorizes automatic local branch setup and validation;
it does not by itself authorize commits, publication, integration or branch deletion.
Honor explicit Git-completion instructions in the current conversation without
asking again. Do not carry a one-time publication approval into future tasks.

When the user requests the complete workflow, perform its authorized steps:
checkpoint task files, validate, integrate accepted commits into `custom/6.12`,
publish, verify the live remote tip, then retire task branches whose commits are
contained in the published baseline. Prefer fast-forward integration when possible;
inspect divergence rather than rewriting shared history. For dependent branches,
retain the commits and prerequisite relationships through integration.

Otherwise leave the result reviewable on its task branch and report its branch,
validation and commit/publication state concisely. Update handoff references when
branch lifecycle changes would make them misleading. Code publication is separate
from application deployment; deployment tags require an actual confirmed deployment.
